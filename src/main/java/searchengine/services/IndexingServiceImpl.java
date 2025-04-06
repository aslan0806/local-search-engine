package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteStatus;
import searchengine.model.Page;
import searchengine.model.Site as SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    private boolean indexingRunning = false;

    @Override
    public Map<String, Object> startIndexing() {
        if (indexingRunning) {
            return Map.of(
                    "result", false,
                    "error", "Индексация уже запущена"
            );
        }

        indexingRunning = true;

        new Thread(() -> {
            for (Site configSite : sitesList.getSites()) {
                String url = configSite.getUrl();

                // Удаляем старые записи по этому сайту
                siteRepository.findAll().stream()
                        .filter(site -> site.getUrl().equals(url))
                        .forEach(site -> {
                            pageRepository.deleteAll(site.getPages());
                            siteRepository.delete(site);
                        });

                // Создаём новую запись сайта
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setUrl(configSite.getUrl());
                siteEntity.setName(configSite.getName());
                siteEntity.setStatus(SiteStatus.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);

                // Запускаем обход сайта через ForkJoin
                ForkJoinPool pool = new ForkJoinPool();
                pool.invoke(new SiteIndexerTask(siteEntity, siteRepository, pageRepository, configSite.getUrl()));
            }

            indexingRunning = false;
        }).start();

        return Map.of("result", true);
    }
}