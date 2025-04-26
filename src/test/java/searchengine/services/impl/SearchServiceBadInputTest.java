package searchengine.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import searchengine.dto.search.SearchResponse;
import searchengine.services.SearchService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchServiceBadInputTest {

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = mock(SearchService.class);
    }

    @Test
    void searchNullQuery_shouldReturnError() {
        when(searchService.search(null, null, 0, 20)).thenReturn(
                new SearchResponse(false, 0, java.util.Collections.emptyList())
        );

        SearchResponse response = searchService.search(null, null, 0, 20);

        assertFalse(response.isResult());
        assertEquals(0, response.getCount());
    }
}