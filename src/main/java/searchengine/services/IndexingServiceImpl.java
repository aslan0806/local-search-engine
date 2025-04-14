package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.IndexingTask;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final IndexingTask indexingTask;

    @Override
    public IndexingResponse startIndexing() {
        List<SiteEntity> sites = siteRepository.findAll();

        for (SiteEntity site : sites) {
            System.out.println("🔎 Индексация сайта: " + site.getUrl()); // ✅ site.getUrl() работает
            indexingTask.indexSite(site);
        }

        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        // Пока заглушка
        return new IndexingResponse(false, "Остановка пока не реализована");
    }
}