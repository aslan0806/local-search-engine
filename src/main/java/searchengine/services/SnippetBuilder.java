package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.russian.RussianMorphology;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SnippetBuilder {

    private final RussianMorphology morphology;

    public SnippetBuilder() throws IOException {
        this.morphology = new RussianMorphology();
    }

    public String build(String content, Set<String> queryLemmas) {
        if (content == null || content.isEmpty()) return "";

        // Разбиваем на предложения
        String[] sentences = content.split("(?<=[.!?])\\s*");

        for (String sentence : sentences) {
            Set<String> sentenceLemmas = lemmatize(sentence);

            for (String lemma : queryLemmas) {
                if (sentenceLemmas.contains(lemma)) {
                    return highlight(sentence, queryLemmas);
                }
            }
        }

        // если нет подходящих предложений, возвращаем первые 200 символов
        return content.length() > 200 ? content.substring(0, 200) + "..." : content;
    }

    private Set<String> lemmatize(String text) {
        Set<String> result = new HashSet<>();
        String[] words = text.toLowerCase().replaceAll("[^а-яА-Я\\s]", " ").split("\\s+");

        for (String word : words) {
            if (word.isBlank()) continue;
            if (isServiceWord(word)) continue;
            List<String> lemmas = morphology.getNormalForms(word);
            if (!lemmas.isEmpty()) {
                result.add(lemmas.get(0));
            }
        }

        return result;
    }

    private boolean isServiceWord(String word) {
        List<String> morphInfos = morphology.getMorphInfo(word);
        return morphInfos.stream().anyMatch(info ->
                info.contains("СОЮЗ") || info.contains("ПРЕДЛ") || info.contains("МЕЖД"));
    }

    private String highlight(String sentence, Set<String> queryLemmas) {
        StringBuilder highlighted = new StringBuilder();

        String[] words = sentence.split("\\s+");
        for (String word : words) {
            String cleaned = word.replaceAll("[^а-яА-Я]", "").toLowerCase();
            List<String> lemmas = morphology.getNormalForms(cleaned);
            if (!lemmas.isEmpty() && queryLemmas.contains(lemmas.get(0))) {
                highlighted.append("<b>").append(word).append("</b>").append(" ");
            } else {
                highlighted.append(word).append(" ");
            }
        }

        return highlighted.toString().trim();
    }
}