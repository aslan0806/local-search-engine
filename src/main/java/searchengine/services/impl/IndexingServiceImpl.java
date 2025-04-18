package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SiteCrawler siteCrawler;
    private final IndexingTask indexingTask;

    private volatile boolean isIndexing = false;

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexing) {
            log.warn("⚠️ Попытка повторного запуска индексации");
            return new IndexingResponse(false, "⚠️ Индексация уже запущена");
        }

        isIndexing = true;
        log.info("▶️ Запуск индексации всех сайтов");

        new Thread(() -> {
            for (SiteEntity site : siteRepository.findAll()) {
                if (!isIndexing) break;

                site.setStatus(StatusType.INDEXING);
                site.setStatusTime(LocalDateTime.now());
                site.setLastError(null);
                siteRepository.save(site);

                log.info("🌐 Индексация сайта: {}", site.getUrl());
                siteCrawler.crawlSite(site);
            }
            isIndexing = false;
            log.info("🏁 Индексация завершена");
        }).start();

        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexing) {
            log.warn("⚠️ Попытка остановки несуществующей индексации");
            return new IndexingResponse(false, "⚠️ Индексация не запущена");
        }

        isIndexing = false;
        log.info("🛑 Индексация остановлена пользователем");
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
            log.error("⛔ URL вне разрешенных сайтов: {}", url);
            return new IndexingResponse(false, "⛔ Данная страница находится за пределами разрешенных сайтов");
        }

        String path = url.replace(site.getUrl(), "");

        Optional<Page> oldPage = pageRepository.findByPathAndSite(path, site);
        oldPage.ifPresent(pageRepository::delete);

        try {
            indexingTask.indexPage(site, path);
            log.info("🔁 Страница переиндексирована: {}", url);
            return new IndexingResponse(true, null);
        } catch (Exception e) {
            log.error("❌ Ошибка при индексации {}: {}", url, e.getMessage());
            return new IndexingResponse(false, "❌ Ошибка индексации: " + e.getMessage());
        }
    }
}