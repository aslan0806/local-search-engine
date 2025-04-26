package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.russian.RussianMorphology;
import org.springframework.stereotype.Service;
import searchengine.utils.StopWords;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LemmaService {

    private RussianMorphology morphology;

    // 🚀 Безопасный кеш для многопоточности
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
            if (word.isBlank()) continue;
            if (isStopWord(word)) continue;

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
}