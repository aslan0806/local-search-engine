package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.indexing.IndexingTask;
import searchengine.services.indexing.SiteCrawler;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SiteCrawler siteCrawler;
    private final IndexingTask indexingTask;

    private volatile boolean isIndexing = false;

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexing) {
            return new IndexingResponse(false, "⚠️ Индексация уже запущена");
        }

        isIndexing = true;

        new Thread(() -> {
            for (SiteEntity site : siteRepository.findAll()) {
                if (!isIndexing) break;

                site.setStatus(StatusType.INDEXING);
                site.setStatusTime(LocalDateTime.now());
                site.setLastError(null);
                siteRepository.save(site);

                siteCrawler.crawlSite(site); // ⛏ Рекурсивный обход
            }
            isIndexing = false;
        }).start();

        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexing) {
            return new IndexingResponse(false, "⚠️ Индексация не запущена");
        }

        isIndexing = false;
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse indexPage(String url) {
        SiteEntity site = siteRepository.findAll()
                .stream()
                .filter(s -> url.startsWith(s.getUrl()))
                .findFirst()
                .orElse(null);

        if (site == null) {
            return new IndexingResponse(false, "⛔ Данная страница находится за пределами разрешённых сайтов");
        }

        String path = url.replace(site.getUrl(), "");

        Optional<Page> oldPage = pageRepository.findByPathAndSite(path, site);
        oldPage.ifPresent(pageRepository::delete);

        try {
            indexingTask.indexPage(site, path);
            return new IndexingResponse(true, null);
        } catch (Exception e) {
            return new IndexingResponse(false, "❌ Ошибка индексации страницы: " + e.getMessage());
        }
    }
}