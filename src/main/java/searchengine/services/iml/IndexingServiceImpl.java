package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.IndexingTask;
import searchengine.services.IndexingService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
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
        Optional<SiteEntity> optionalSite = siteRepository.findAll()
                .stream()
                .filter(site -> url.startsWith(site.getUrl()))
                .findFirst();

        if (optionalSite.isEmpty()) {
            return new IndexingResponse(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }

        SiteEntity site = optionalSite.get();
        String path = url.replace(site.getUrl(), "");

        // удалить старую страницу, если есть
        Optional<Page> oldPage = pageRepository.findByPathAndSite(path, site);
        oldPage.ifPresent(pageRepository::delete);

        indexingTask.indexPage(site, path); // 🧠 основной вызов
        return new IndexingResponse(true, null);
    }
}