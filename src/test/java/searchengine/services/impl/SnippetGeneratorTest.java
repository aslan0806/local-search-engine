package searchengine.services.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnippetGeneratorTest {

    private final SearchServiceImpl snippetGenerator = new SearchServiceImpl(
            null, null, null, null // нам здесь репозитории не нужны
    );

    @Test
    void makeSnippet_shouldHighlightLemmas() {
        String text = "Это тестовый текст для проверки выделения слов.";
        String lemma = "проверка";

        String snippet = snippetGenerator.makeSnippet(text, java.util.List.of(lemma));

        assertTrue(snippet.contains("<b>проверка</b>"));
        assertTrue(snippet.length() <= 150 || snippet.endsWith("..."));
    }

    @Test
    void makeSnippet_noMatches_shouldReturnBeginning() {
        String text = "Это какой-то текст без совпадений.";
        String snippet = snippetGenerator.makeSnippet(text, java.util.List.of("несуществующее"));

        assertFalse(snippet.contains("<b>"));
        assertTrue(snippet.startsWith("Это"));
    }
}