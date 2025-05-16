package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.RecursiveSiteParser;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class SiteIndexerService {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    public void indexSite(SiteEntity site) {
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);

        try {
            Set<String> visited = ConcurrentHashMap.newKeySet(); // Потокобезопасная коллекция
            ForkJoinPool pool = new ForkJoinPool();

            RecursiveSiteParser task = new RecursiveSiteParser(site.getUrl(), site, visited, pageRepository);
            pool.invoke(task);

            site.setStatus(SiteStatus.INDEXED);
        } catch (Exception e) {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Ошибка индексации: " + e.getMessage());
        }

        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }
}