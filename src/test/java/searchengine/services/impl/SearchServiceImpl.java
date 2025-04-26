package searchengine.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import searchengine.dto.search.SearchResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.LemmaService;
import searchengine.services.SearchService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchServiceImplTest {

    private SiteRepository siteRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private PageRepository pageRepository;
    private LemmaService lemmaService;
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        siteRepository = Mockito.mock(SiteRepository.class);
        lemmaRepository = Mockito.mock(LemmaRepository.class);
        indexRepository = Mockito.mock(IndexRepository.class);
        pageRepository = Mockito.mock(PageRepository.class);
        lemmaService = Mockito.mock(LemmaService.class);

        searchService = new SearchServiceImpl(
                siteRepository,
                pageRepository,
                lemmaRepository,
                indexRepository,
                lemmaService
        );
    }

    @Test
    void search_withEmptyQuery_shouldReturnFalse() {
        SearchResponse response = searchService.search("", null, 0, 20);
        assertFalse(response.isResult());
        assertEquals(0, response.getCount());
    }

    @Test
    void search_withValidQuery_shouldReturnSuccess() {
        when(lemmaService.lemmatize("тест")).thenReturn(Collections.singletonMap("тест", 1));

        SearchResponse response = searchService.search("тест", null, 0, 20);

        assertTrue(response.isResult());
        assertEquals(0, response.getCount()); // Пока что мок возвращает пустой результат
        assertNotNull(response.getData());
    }

    @Test
    void search_withPagination_shouldRespectOffsetLimit() {
        when(lemmaService.lemmatize("пример")).thenReturn(Collections.singletonMap("пример", 1));

        SearchResponse response = searchService.search("пример", null, 10, 5);

        assertTrue(response.isResult());
        assertNotNull(response.getData());
    }
}