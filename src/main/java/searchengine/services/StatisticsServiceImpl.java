package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;

    @Override
    public StatisticsResponse getStatistics() {
        List<Site> siteEntities = siteRepository.findAll();

        // Total statistics
        TotalStatistics total = new TotalStatistics();
        total.setSites(siteEntities.size());
        total.setIndexing(true); // Пока заглушка, можно заменить флагом из общего сервиса

        int totalPages = 0;
        int totalLemmas = 0; // Пока временно, нужно будет доработать при наличии таблицы lemma

        List<DetailedStatisticsItem> detailedItems = new ArrayList<>();

        for (Site site : siteEntities) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setUrl(site.getUrl());
            item.setName(site.getName());
            item.setStatus(site.getStatus().name());
            item.setStatusTime(site.getStatusTime()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
            item.setError(site.getLastError());

            int pages = site.getPages() != null ? site.getPages().size() : 0;
            int lemmas = 0; // сюда позже можно внедрить реальный подсчёт из lemmaRepository

            item.setPages(pages);
            item.setLemmas(lemmas);

            totalPages += pages;
            totalLemmas += lemmas;

            detailedItems.add(item);
        }

        total.setPages(totalPages);
        total.setLemmas(totalLemmas);

        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailedItems);

        StatisticsResponse response = new StatisticsResponse();
        response.setResult(true);
        response.setStatistics(data);

        return response;
    }
}