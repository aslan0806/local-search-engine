package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class SiteIndexerService {

    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    private final LemmaService lemmaService;

    public void indexSite(SiteEntity site) {
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        site.setLastError(null);
        siteRepository.save(site);

        Set<String> visited = new ConcurrentSkipListSet<>();

        try {
            SiteIndexerTask rootTask = new SiteIndexerTask(
                    site.getUrl(), site, visited,
                    pageRepository, lemmaRepository, indexRepository, lemmaService
            );
            ForkJoinPool.commonPool().invoke(rootTask);

            site.setStatus(SiteStatus.INDEXED);
        } catch (Exception e) {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Индексирование прервано: " + e.getMessage());
        }

        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }
}