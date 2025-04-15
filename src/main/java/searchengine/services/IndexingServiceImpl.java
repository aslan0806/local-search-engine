package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.IndexingTask;
import searchengine.services.indexing.RecursiveSiteParser;

import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final IndexingTask indexingTask;

    @Override
    public IndexingResponse startIndexing() {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Set<String> visited = new ConcurrentSkipListSet<>();

        for (SiteEntity site : siteRepository.findAll()) {
            RecursiveSiteParser parser = new RecursiveSiteParser(site.getUrl(), site, indexingTask, visited);
            forkJoinPool.execute(parser);
        }

        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        // 💡 пока не реализовано
        return new IndexingResponse(false, "Остановка не реализована");
    }
}