// 📄 src/test/java/searchengine/controllers/IndexingControllerTest.java

package searchengine.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.services.IndexingService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@WebMvcTest(IndexingController.class)
class IndexingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IndexingService indexingService;

    @Test
    void startIndexing_shouldReturnOk() throws Exception {
        when(indexingService.startIndexing()).thenReturn(new IndexingResponse(true, null));

        mockMvc.perform(post("/api/startIndexing"))
                .andExpect(status().isOk());
    }

    @Test
    void stopIndexing_shouldReturnOk() throws Exception {
        when(indexingService.stopIndexing()).thenReturn(new IndexingResponse(true, null));

        mockMvc.perform(post("/api/stopIndexing"))
                .andExpect(status().isOk());
    }

    @Test
    void indexPage_shouldReturnOk() throws Exception {
        when(indexingService.indexPage(Mockito.anyString())).thenReturn(new IndexingResponse(true, null));

        mockMvc.perform(get("/api/indexPage")
                        .param("url", "https://example.com/test"))
                .andExpect(status().isOk());
    }
}