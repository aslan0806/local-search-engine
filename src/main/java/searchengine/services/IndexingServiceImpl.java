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
        IndexingResponse response = new IndexingResponse();
        try {
            List<SiteEntity> sites = siteRepository.findAll();
            for (SiteEntity site : sites) {
                indexingTask.indexSite(site); // ✅ важно: вызываем метод indexSite
            }
            response.setResult(true);
            response.setError(null);
        } catch (Exception e) {
            response.setResult(false);
            response.setError("Ошибка при индексации: " + e.getMessage());
        }

        return response;
    }

    @Override
    public IndexingResponse stopIndexing() {
        IndexingResponse response = new IndexingResponse();
        response.setResult(false);
        response.setError("Остановка индексации пока не реализована");
        return response;
    }
}