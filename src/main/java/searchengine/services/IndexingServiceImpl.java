package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.IndexingService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final IndexingTask indexingTask;
    private final SiteCrawler siteCrawler;

    @Override
    public IndexingResponse startIndexing() {
        List<SiteEntity> sites = siteRepository.findAll();
        for (SiteEntity site : sites) {
            siteCrawler.crawlSite(site);
        }
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        // Пока просто заглушка
        return new IndexingResponse(false, "Остановка индексации не реализована");
    }

    public IndexingResponse indexPage(String url) {
        Optional<SiteEntity> siteOpt = siteRepository.findAll().stream()
                .filter(site -> url.startsWith(site.getUrl()))
                .findFirst();

        if (siteOpt.isEmpty()) {
            return new IndexingResponse(false, "Страница находится за пределами разрешённых сайтов");
        }

        SiteEntity site = siteOpt.get();
        String path = url.replace(site.getUrl(), "");

        Optional<Page> existing = pageRepository.findByPathAndSite(path, site);
        existing.ifPresent(pageRepository::delete); // если есть — удалим старую

        try {
            indexingTask.indexPage(site, path);
        } catch (IOException e) {
            return new IndexingResponse(false, "Ошибка при индексации: " + e.getMessage());
        }

        return new IndexingResponse(true, null);
    }
}