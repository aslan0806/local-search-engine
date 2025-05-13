package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.russian.RussianMorphology;
import org.springframework.stereotype.Service;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.utils.StopWords;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LemmaService {

    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    private RussianMorphology morphology;

    private final Map<String, String> lemmaCache = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        try {
            morphology = new RussianMorphology();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка инициализации RussianMorphology", e);
        }
    }

    public Map<String, Integer> lemmatize(String text) {
        Map<String, Integer> lemmas = new HashMap<>();

        String[] words = text.toLowerCase()
                .replaceAll("[^а-яА-Я\\s]", " ")
                .trim()
                .split("\\s+");

        for (String word : words) {
            if (word.isBlank() || isStopWord(word)) continue;

            String lemma = getLemma(word);
            if (lemma != null) {
                lemmas.put(lemma, lemmas.getOrDefault(lemma, 0) + 1);
            }
        }

        return lemmas;
    }

    private String getLemma(String word) {
        return lemmaCache.computeIfAbsent(word, w -> {
            List<String> normalForms = morphology.getNormalForms(w);
            return normalForms.isEmpty() ? null : normalForms.get(0);
        });
    }

    private boolean isStopWord(String word) {
        List<String> morphInfos = morphology.getMorphInfo(word);
        return morphInfos.stream().anyMatch(info ->
                info.contains("СОЮЗ") || info.contains("ПРЕДЛ") || info.contains("МЕЖД")
        ) || StopWords.RUSSIAN_STOP_WORDS.contains(word);
    }

    public void saveLemmas(SiteEntity site, Page page, Map<String, Integer> lemmas) {
        lemmas.forEach((lemmaText, count) -> {
            Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaText, site)
                    .orElseGet(() -> {
                        Lemma newLemma = new Lemma();
                        newLemma.setLemma(lemmaText);
                        newLemma.setFrequency(0);
                        newLemma.setSite(site);
                        return newLemma;
                    });

            lemma.setFrequency(lemma.getFrequency() + 1);
            lemmaRepository.save(lemma);

            PageIndex pageIndex = new PageIndex();
            pageIndex.setLemma(lemma);
            pageIndex.setPage(page);
            pageIndex.setRank(count);
            indexRepository.save(pageIndex);
        });
    }
}