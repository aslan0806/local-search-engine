package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.IndexingTask;
import searchengine.services.indexing.SiteCrawler;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final IndexingTask indexingTask;
    private final SiteCrawler siteCrawler;

    private volatile boolean isIndexingRunning = false;

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexingRunning) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }

        isIndexingRunning = true;

        List<SiteEntity> sites = siteRepository.findAll();

        for (SiteEntity site : sites) {
            siteCrawler.clearVisited();
            siteCrawler.setInterrupted(false);
            siteCrawler.crawl(site.getUrl(), site);

            if (!isIndexingRunning) {
                break;
            }
        }

        isIndexingRunning = false;
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        isIndexingRunning = false;
        siteCrawler.setInterrupted(true); // 🔌 Прерываем рекурсивный обход

        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse indexPage(String url) {
        SiteEntity site = siteRepository.findAll().stream()
                .filter(s -> url.startsWith(s.getUrl()))
                .findFirst()
                .orElse(null);

        if (site == null) {
            return new IndexingResponse(false, "Страница за пределами разрешённых сайтов");
        }

        try {
            indexingTask.indexPage(url, site);
            return new IndexingResponse(true, null);
        } catch (Exception e) {
            return new IndexingResponse(false, "Ошибка индексации: " + e.getMessage());
        }
    }
}