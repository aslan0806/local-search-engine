package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.IndexingTask;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final IndexingTask indexingTask;

    @Override
    public IndexingResponse startIndexing() {
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse indexPage(String url) {
        Optional<SiteEntity> siteOpt = siteRepository.findAll().stream()
                .filter(site -> url.startsWith(site.getUrl()))
                .findFirst();

        if (siteOpt.isEmpty()) {
            return new IndexingResponse(false, "Страница за пределами разрешённых сайтов");
        }

        try {
            SiteEntity site = siteOpt.get();
            indexingTask.indexPage(url, site); // ✅ Используем task
            return new IndexingResponse(true, null);

        } catch (Exception e) {
            return new IndexingResponse(false, "Ошибка индексации: " + e.getMessage());
        }
    }
}