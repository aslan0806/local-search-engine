package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.SiteCrawler;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final SiteCrawler siteCrawler;

    @Override
    public IndexingResponse startIndexing() {
        List<SiteEntity> sites = siteRepository.findAll();

        for (SiteEntity site : sites) {
            siteCrawler.clearVisited(); // очищаем перед каждым сайтом
            siteCrawler.crawl(site, "/");
        }

        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        return new IndexingResponse(false, "Остановка пока не реализована");
    }
}