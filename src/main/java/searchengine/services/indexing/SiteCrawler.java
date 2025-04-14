package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.model.SiteEntity;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SiteCrawler {

    private final IndexingTask indexingTask;

    private final Set<String> visited = new HashSet<>();

    public void crawl(SiteEntity site, String path) {
        if (visited.contains(path)) {
            return;
        }

        visited.add(path);
        indexingTask.indexPage(site, path);

        try {
            String fullUrl = site.getUrl() + path;
            Document doc = Jsoup.connect(fullUrl).get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String href = link.attr("abs:href"); // абсолютный путь
                if (href.startsWith(site.getUrl())) {
                    String nextPath = href.replace(site.getUrl(), "");
                    if (isValidPath(nextPath)) {
                        crawl(site, nextPath);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("⚠️ Ошибка при обходе страницы: " + path);
        }
    }

    private boolean isValidPath(String path) {
        return path.startsWith("/") &&
                !path.contains("#") &&
                !path.contains("?") &&
                !path.matches(".*\\.(jpg|jpeg|png|gif|css|js|svg|ico)$");
    }
}