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

    public void crawl(String url, SiteEntity site) {
        if (visitedUrls.contains(url)) return;
        visitedUrls.add(url);

        try {
            // ⬇️ Загрузка страницы
            Document doc = Jsoup.connect(url).get();

            // 🔤 Индексация текущей страницы
            indexingTask.indexPage(url, site);

            // 🔗 Сбор всех ссылок
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("abs:href");

                // ✅ Фильтрация только внутренних страниц
                if (href.startsWith(site.getUrl()) &&
                        !href.contains("#") &&
                        !href.endsWith(".pdf") &&
                        !href.endsWith(".jpg") &&
                        !href.endsWith(".png") &&
                        !href.endsWith(".jpeg")) {
                    crawl(href, site); // 🔁 Рекурсивный вызов
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при обходе " + url + ": " + e.getMessage());
        }
    }

    public void clearVisited() {
        visitedUrls.clear(); // 🔄 сброс между сайтами
    }
}