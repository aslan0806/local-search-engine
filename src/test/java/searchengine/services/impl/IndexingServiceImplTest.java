package searchengine.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.IndexingTask;
import searchengine.services.indexing.SiteCrawler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IndexingServiceImplTest {

    private IndexingServiceImpl indexingService;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private SiteCrawler siteCrawler;
    private IndexingTask indexingTask;

    @BeforeEach
    void setup() {
        siteRepository = mock(SiteRepository.class);
        pageRepository = mock(PageRepository.class);
        siteCrawler = mock(SiteCrawler.class);
        indexingTask = mock(IndexingTask.class);

        indexingService = new IndexingServiceImpl(siteRepository, pageRepository, siteCrawler, indexingTask);
    }

    @Test
    void testStartIndexing() {
        IndexingResponse response = indexingService.startIndexing();
        assertTrue(response.isResult());
    }

    @Test
    void testStopIndexing() {
        indexingService.startIndexing();
        IndexingResponse response = indexingService.stopIndexing();
        assertTrue(response.isResult());
    }
}