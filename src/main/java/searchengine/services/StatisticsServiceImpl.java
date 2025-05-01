package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.*;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
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

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        int totalPages = 0;
        int totalLemmas = 0;

        for (SiteEntity site : sites) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            item.setStatus(site.getStatus().toString());
            item.setStatusTime(
                    site.getStatusTime() != null
                            ? Timestamp.valueOf(site.getStatusTime()).getTime()
                            : 0
            );
            item.setError(site.getLastError() == null ? "" : site.getLastError());

            Long pageCountLong = pageRepository.countBySite(site);
            int pageCount = pageCountLong.intValue();

            Long lemmaCountLong = lemmaRepository.countBySite(site);
            int lemmaCount = lemmaCountLong.intValue();

            item.setPages(pageCount);
            item.setLemmas(lemmaCount);
            totalPages += pageCount;
            totalLemmas += lemmaCount;

            detailed.add(item);
        }

        total.setPages(totalPages);
        total.setLemmas(totalLemmas);

        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);

        StatisticsResponse response = new StatisticsResponse();
        response.setResult(true);
        response.setStatistics(data);

        return response;
    }
}