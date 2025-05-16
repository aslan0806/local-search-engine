package searchengine.services.indexing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

public class RecursiveSiteParser extends RecursiveAction {

    private final String url;
    private final SiteEntity site;
    private final Set<String> visited;
    private final PageRepository pageRepository;

    public RecursiveSiteParser(String url, SiteEntity site, Set<String> visited, PageRepository pageRepository) {
        this.url = url;
        this.site = site;
        this.visited = visited;
        this.pageRepository = pageRepository;
    }

    @Override
    protected void compute() {
        if (!visited.add(url)) return;

        try {
            Document document = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot")
                    .referrer("https://google.com")
                    .get();

            Page page = new Page();
            page.setCode(200);
            page.setPath(url.replace(site.getUrl(), ""));
            page.setContent(document.html());
            page.setSite(site);
            pageRepository.save(page);

            Elements links = document.select("a[href]");
            List<RecursiveSiteParser> tasks = links.stream()
                    .map(el -> el.absUrl("href"))
                    .filter(this::isValidLink)
                    .map(link -> new RecursiveSiteParser(link, site, visited, pageRepository))
                    .toList();

            invokeAll(tasks);

        } catch (IOException e) {
            System.err.println("❌ Ошибка загрузки " + url + ": " + e.getMessage());
        }
    }

    private boolean isValidLink(String link) {
        return link.startsWith(site.getUrl())
                && !link.contains("#")
                && !link.matches(".*\\.(jpg|png|pdf|gif|docx?)$")
                && !visited.contains(link);
    }
}