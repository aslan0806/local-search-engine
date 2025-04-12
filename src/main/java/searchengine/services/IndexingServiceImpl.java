package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final AtomicBoolean indexingRunning = new AtomicBoolean(false);

    @Override
    public IndexingResponse startIndexing() {
        if (indexingRunning.get()) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }

        indexingRunning.set(true);
        new Thread(this::runIndexing).start();

        return new IndexingResponse(true, null);
    }

    private void runIndexing() {
        List<SiteEntity> sites = siteRepository.findAll();
        for (SiteEntity site : sites) {
            site.setStatus(StatusType.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);

            try {
                // Здесь позже будет реальный обход страниц
                Thread.sleep(2000); // Симулируем задержку индексации
                site.setStatus(StatusType.INDEXED);
            } catch (InterruptedException e) {
                site.setStatus(StatusType.FAILED);
                site.setLastError("Индексация прервана: " + e.getMessage());
            }

            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }

        indexingRunning.set(false);
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!indexingRunning.get()) {
            return new IndexingResponse(false, "Индексация не запущена");
        }

        indexingRunning.set(false);
        return new IndexingResponse(true, null);
    }
}