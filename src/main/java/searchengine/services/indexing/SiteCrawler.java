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
    private final Set<String> visitedUrls = new HashSet<>();
    private volatile boolean isInterrupted = false;

    public void crawl(String url, SiteEntity site) {
        if (isInterrupted || visitedUrls.contains(url)) return;
        visitedUrls.add(url);

        try {
            Document doc = Jsoup.connect(url).get();
            indexingTask.indexPage(url, site);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("abs:href");

                if (href.startsWith(site.getUrl()) &&
                        !href.contains("#") &&
                        !href.endsWith(".pdf") &&
                        !href.endsWith(".jpg") &&
                        !href.endsWith(".png") &&
                        !href.endsWith(".jpeg")) {
                    crawl(href, site);
                }

                if (isInterrupted) break;
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при обходе " + url + ": " + e.getMessage());
        }
    }

    public void clearVisited() {
        visitedUrls.clear();
    }

    public void setInterrupted(boolean interrupted) {
        this.isInterrupted = interrupted;
    }
}