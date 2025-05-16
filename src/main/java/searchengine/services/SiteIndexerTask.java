package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

public class SiteIndexerTask extends RecursiveAction {

    private final String url;
    private final SiteEntity site;
    private final Set<String> visited;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    public SiteIndexerTask(String url,
                           SiteEntity site,
                           Set<String> visited,
                           PageRepository pageRepository,
                           SiteRepository siteRepository) {
        this.url = url;
        this.site = site;
        this.visited = visited;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
    }

    @Override
    protected void compute() {
        if (!visited.add(url)) return;

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot")
                    .referrer("https://google.com")
                    .get();

            Page page = new Page();
            page.setCode(200);
            page.setPath(url.replace(site.getUrl(), ""));
            page.setContent(doc.html());
            page.setSite(site);
            pageRepository.save(page);

            Elements links = doc.select("a[href]");
            List<SiteIndexerTask> subTasks = links.stream()
                    .map(link -> link.absUrl("href"))
                    .filter(this::isValidLink)
                    .map(link -> new SiteIndexerTask(link, site, visited, pageRepository, siteRepository))
                    .collect(Collectors.toList());

            invokeAll(subTasks);

        } catch (IOException e) {
            System.err.println("❌ Ошибка при загрузке страницы: " + url);
        }
    }

    private boolean isValidLink(String link) {
        return link.startsWith(site.getUrl())
                && !link.contains("#")
                && !link.matches(".*\\.(jpg|png|pdf|docx?|css|js|svg|ico)$")
                && !visited.contains(link);
    }

    public static void startIndexing(SiteEntity site,
                                     PageRepository pageRepository,
                                     SiteRepository siteRepository) {
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);

        Set<String> visited = ConcurrentHashMap.newKeySet();
        SiteIndexerTask rootTask = new SiteIndexerTask(site.getUrl(), site, visited, pageRepository, siteRepository);

        try {
            new java.util.concurrent.ForkJoinPool().invoke(rootTask);
            site.setStatus(SiteStatus.INDEXED);
        } catch (Exception e) {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Ошибка: " + e.getMessage());
        }

        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }
}