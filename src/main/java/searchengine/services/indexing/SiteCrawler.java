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

    public void crawl(SiteEntity site) {
        crawlPage(site, site.getUrl());
    }

    private void crawlPage(SiteEntity site, String url) {
        if (visited.contains(url)) return;
        visited.add(url);

        try {
            indexingTask.indexPage(url, site); // 🔥 Индексируем страницу
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String absUrl = link.absUrl("href");

                if (absUrl.startsWith(site.getUrl()) && isSameDomain(url, absUrl)) {
                    crawlPage(site, absUrl);
                }
            }

        } catch (IOException e) {
            System.out.println("⚠️ Ошибка при обходе: " + url + " — " + e.getMessage());
        }
    }

    private boolean isSameDomain(String baseUrl, String newUrl) {
        try {
            return new java.net.URL(baseUrl).getHost().equals(new java.net.URL(newUrl).getHost());
        } catch (Exception e) {
            return false;
        }
    }
}