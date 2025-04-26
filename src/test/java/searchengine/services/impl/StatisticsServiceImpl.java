package searchengine.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.StatisticsServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsServiceImplTest {

    private StatisticsServiceImpl statisticsService;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;

    @BeforeEach
    void setup() {
        siteRepository = mock(SiteRepository.class);
        pageRepository = mock(PageRepository.class);
        lemmaRepository = mock(LemmaRepository.class);

        statisticsService = new StatisticsServiceImpl(siteRepository, pageRepository, lemmaRepository);
    }

    @Test
    void testGetStatistics() {
        StatisticsResponse response = statisticsService.getStatistics();
        assertTrue(response.isResult());
    }
}