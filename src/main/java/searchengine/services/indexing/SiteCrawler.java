package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SiteCrawler {

    private final IndexingTask indexingTask;
    private final Set<String> visited = new HashSet<>();

    public void crawl(SiteEntity site, String path) {
        String fullUrl = site.getUrl() + path;

        if (visited.contains(fullUrl)) {
            return;
        }

        visited.add(fullUrl);

        try {
            Document doc = Jsoup.connect(fullUrl).get();

            indexingTask.indexPage(site, path); // ✅ индексируем эту страницу

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.absUrl("href");

                // фильтруем только внутренние ссылки
                if (href.startsWith(site.getUrl())) {
                    String relativePath = href.replace(site.getUrl(), "");
                    if (relativePath.isEmpty()) {
                        relativePath = "/";
                    }

                    // рекурсивно обходим
                    crawl(site, relativePath);
                }
            }

        } catch (IOException e) {
            System.out.println("⚠️ Ошибка при обходе: " + fullUrl);
        }
    }

    public void clearVisited() {
        visited.clear();
    }
}