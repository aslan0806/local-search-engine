// 📄 src/test/java/searchengine/services/IndexingServiceImplTest.java

package searchengine.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.impl.IndexingServiceImpl;
import searchengine.services.indexing.IndexingTask;
import searchengine.services.indexing.SiteCrawler;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class IndexingServiceImplTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private SiteCrawler siteCrawler;

    @Mock
    private IndexingTask indexingTask;

    @InjectMocks
    private IndexingServiceImpl indexingService;

    @Test
    void startIndexing_whenAlreadyRunning_shouldReturnError() {
        indexingService.startIndexing();
        IndexingResponse response = indexingService.startIndexing();
        assertFalse(response.isResult());
        assertTrue(response.getError().contains("Индексация уже запущена"));
    }

    @Test
    void stopIndexing_whenNotRunning_shouldReturnError() {
        IndexingResponse response = indexingService.stopIndexing();
        assertFalse(response.isResult());
        assertTrue(response.getError().contains("не запущена"));
    }

    @Test
    void startIndexing_shouldStartSuccessfully() {
        when(siteRepository.findAll()).thenReturn(Collections.emptyList());

        IndexingResponse response = indexingService.startIndexing();
        assertTrue(response.isResult());
        assertNull(response.getError());
    }
}