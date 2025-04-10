package searchengine.services;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LemmaService {

    private final RussianLuceneMorphology morphology = new RussianLuceneMorphology();
    private final Set<String> excludedParts = Set.of("МЕЖД", "СОЮЗ", "ПРЕДЛ", "ЧАСТ", "МС");

    public Map<String, Integer> getLemmaMap(String text) {
        Map<String, Integer> lemmaMap = new HashMap<>();

        String[] words = text.toLowerCase().replaceAll("[^а-яА-Я\\s]", " ").split("\\s+");
        for (String word : words) {
            if (word.isBlank()) continue;
            List<String> morphInfos = morphology.getMorphInfo(word);
            if (morphInfos.stream().anyMatch(this::isExcluded)) continue;

            List<String> normalForms = morphology.getNormalForms(word);
            String lemma = normalForms.get(0);
            lemmaMap.put(lemma, lemmaMap.getOrDefault(lemma, 0) + 1);
        }

        return lemmaMap;
    }

    private boolean isExcluded(String morph) {
        return excludedParts.stream().anyMatch(morph::contains);
    }
}