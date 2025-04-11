package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.dto.statistics.*;
import searchengine.services.StatisticsService;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Override
    public StatisticsResponse getStatistics() {
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();

        TotalStatistics total = new TotalStatistics();
        total.setSites(1);
        total.setPages(100);
        total.setLemmas(5000);
        total.setIndexing(true);

        data.setTotal(total);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        DetailedStatisticsItem siteStats = new DetailedStatisticsItem();
        siteStats.setName("ExampleSite");
        siteStats.setUrl("https://example.com");
        siteStats.setPages(100);
        siteStats.setLemmas(5000);
        siteStats.setStatus("INDEXED");
        siteStats.setStatusTime(System.currentTimeMillis());
        siteStats.setError("");

        detailed.add(siteStats);
        data.setDetailed(detailed);

        response.setStatistics(data);
        response.setResult(true);

        return response;
    }
}