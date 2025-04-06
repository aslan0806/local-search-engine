package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteStatus;
import searchengine.model.Site as SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    private volatile boolean indexingRunning = false;
    private final List<SiteIndexerTask> activeTasks = Collections.synchronizedList(new ArrayList<>());
    private ForkJoinPool forkJoinPool;

    @Override
    public Map<String, Object> startIndexing() {
        if (indexingRunning) {
            return Map.of("result", false, "error", "Индексация уже запущена");
        }

        indexingRunning = true;

        new Thread(() -> {
            for (Site configSite : sitesList.getSites()) {
                String url = configSite.getUrl();

                siteRepository.findAll().stream()
                        .filter(site -> site.getUrl().equals(url))
                        .forEach(site -> {
                            pageRepository.deleteAll(site.getPages());
                            siteRepository.delete(site);
                        });

                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setUrl(configSite.getUrl());
                siteEntity.setName(configSite.getName());
                siteEntity.setStatus(SiteStatus.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);

                SiteIndexerTask task = new SiteIndexerTask(siteEntity, siteRepository, pageRepository, url);
                activeTasks.add(task);

                forkJoinPool = new ForkJoinPool();
                forkJoinPool.invoke(task);
            }

            indexingRunning = false;
        }).start();

        return Map.of("result", true);
    }

    @Override
    public Map<String, Object> stopIndexing() {
        if (!indexingRunning) {
            return Map.of("result", false, "error", "Индексация не запущена");
        }

        indexingRunning = false;

        // Прерываем все задачи
        activeTasks.forEach(SiteIndexerTask::cancel);
        if (forkJoinPool != null) {
            forkJoinPool.shutdownNow();
        }

        siteRepository.findAll().forEach(site -> {
            if (site.getStatus() == SiteStatus.INDEXING) {
                site.setStatus(SiteStatus.FAILED);
                site.setLastError("Индексация остановлена пользователем");
                site.setStatusTime(LocalDateTime.now());
                siteRepository.save(site);
            }
        });

        return Map.of("result", true);
    }
}