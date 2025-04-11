package searchengine.services;

import org.apache.lucene.morphology.russian.RussianMorphology;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LemmaService {

    private final RussianMorphology morphology;

    public LemmaService() throws IOException {
        this.morphology = new RussianMorphology();
    }

    public Map<String, Integer> lemmatize(String text) {
        Map<String, Integer> lemmas = new HashMap<>();
        String[] words = text.toLowerCase().replaceAll("[^а-яА-Я\\s]", " ").split("\\s+");

        for (String word : words) {
            if (word.isBlank()) continue;
            if (isServiceWord(word)) continue;

            List<String> normalForms = morphology.getNormalForms(word);
            if (!normalForms.isEmpty()) {
                String lemma = normalForms.get(0);
                lemmas.put(lemma, lemmas.getOrDefault(lemma, 0) + 1);
            }
        }

        return lemmas;
    }

    private boolean isServiceWord(String word) {
        List<String> morphInfos = morphology.getMorphInfo(word);
        return morphInfos.stream().anyMatch(info ->
                info.contains("СОЮЗ") || info.contains("ПРЕДЛ") || info.contains("МЕЖД"));
    }
}