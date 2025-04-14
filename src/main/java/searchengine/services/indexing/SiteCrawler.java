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

    public void crawl(String url, SiteEntity site) {
        if (visited.contains(url) || !url.startsWith(site.getUrl())) {
            return;
        }

        visited.add(url);

        try {
            // Загрузка и индексация
            indexingTask.indexPage(url, site);

            // Парсим HTML
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String href = link.absUrl("href");

                // Фильтрация: только внутренние ссылки
                if (href.startsWith(site.getUrl()) && !href.contains("#") && !href.endsWith(".pdf") && !href.endsWith(".jpg")) {
                    crawl(href, site); // рекурсия
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Ошибка обхода страницы: " + url + " - " + e.getMessage());
        }
    }
}