package searchengine.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.dto.search.SearchResponse;
import searchengine.services.SearchService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(1);
        response.setData(List.of());
        when(searchService.search(anyString(), anyString(), anyInt(), anyInt())).thenReturn(response);
    }

    @Test
    void searchEndpoint_ReturnsSuccessResponse() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("query", "example"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
    }
}