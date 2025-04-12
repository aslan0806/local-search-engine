package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final IndexingTask indexingTask;

    private volatile boolean isIndexing = false;

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexing) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }

        isIndexing = true;

        List<SiteEntity> sites = siteRepository.findAll();

        for (SiteEntity site : sites) {
            // 🔧 Временно хардкодим пути, заменим позже на парсинг сайта
            List<String> paths = List.of("/", "/news", "/about");

            for (String path : paths) {
                if (!isIndexing) {
                    return new IndexingResponse(false, "Индексация остановлена");
                }
                indexingTask.indexPage(site, path);
            }
        }

        isIndexing = false;
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexing) {
            return new IndexingResponse(false, "Индексация не запущена");
        }

        isIndexing = false;
        return new IndexingResponse(true, null);
    }
}