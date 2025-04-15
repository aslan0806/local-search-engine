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

    public void crawlSite(SiteEntity site) {
        String baseUrl = site.getUrl();
        visited.clear();
        crawl(baseUrl, site);
    }

    private void crawl(String url, SiteEntity site) {
        if (visited.contains(url) || !url.startsWith(site.getUrl())) {
            return;
        }

        visited.add(url);
        String path = url.replace(site.getUrl(), "");
        try {
            indexingTask.indexPage(site, path);

            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absHref = link.attr("abs:href").split("#")[0]; // убираем якоря
                if (absHref.startsWith(site.getUrl()) && !visited.contains(absHref)) {
                    crawl(absHref, site);
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Ошибка при обходе: " + url);
        }
    }
}