package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.StatisticsService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        List<SiteEntity> sites = siteRepository.findAll();
        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        long totalPages = 0;
        long totalLemmas = 0;

        for (SiteEntity site : sites) {
            long pages = pageRepository.countBySite(site);
            long lemmas = lemmaRepository.countBySite(site);

            totalPages += pages;
            totalLemmas += lemmas;

            DetailedStatisticsItem item = new DetailedStatisticsItem(
                    site.getUrl(),
                    site.getName(),
                    site.getStatus().toString(),
                    site.getStatusTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    site.getLastError(),
                    (int) pages,
                    (int) lemmas
            );

            detailed.add(item);
        }

        TotalStatistics total = new TotalStatistics(
                sites.size(),
                (int) totalPages,
                (int) totalLemmas,
                true
        );

        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);

        StatisticsResponse response = new StatisticsResponse();
        response.setResult(true);
        response.setStatistics(data);

        return response;
    }
}