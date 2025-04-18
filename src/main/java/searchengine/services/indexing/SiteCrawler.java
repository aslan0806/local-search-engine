package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import searchengine.model.SiteEntity;

@Component
@RequiredArgsConstructor
public class SiteCrawler {

    private final IndexingTask indexingTask;

    @Async // 🚀 Асинхронный запуск
    public void crawlSite(SiteEntity site) {
        indexingTask.indexSite(site); // запустит индексацию
    }
}