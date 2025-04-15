package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.SiteEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

@RequiredArgsConstructor
public class RecursiveSiteParser extends RecursiveTask<Void> {

    private final String url;
    private final SiteEntity site;
    private final IndexingTask indexingTask;
    private final Set<String> visited;

    @Override
    protected Void compute() {
        try {
            if (visited.contains(url)) return null;

            visited.add(url);

            Document doc = Jsoup.connect(url).get();

            String path = url.replace(site.getUrl(), "");
            if (path.isBlank()) path = "/";

            indexingTask.indexPage(site, path); // 🔁 Индексируем

            Elements links = doc.select("a[href]");
            Set<RecursiveSiteParser> tasks = new HashSet<>();

            for (Element link : links) {
                String href = link.absUrl("href");

                // Только внутренние страницы
                if (href.startsWith(site.getUrl()) && !href.contains("#") && !href.matches(".*\\.(jpg|png|gif|pdf|docx?)$")) {
                    if (!visited.contains(href)) {
                        RecursiveSiteParser task = new RecursiveSiteParser(href, site, indexingTask, visited);
                        task.fork();
                        tasks.add(task);
                    }
                }
            }

            for (RecursiveSiteParser task : tasks) {
                task.join();
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка парсинга: " + url + " — " + e.getMessage());
        }

        return null;
    }
}