package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

public class SiteIndexerTask extends RecursiveAction {

    private final Site site;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final String url;
    private final Set<String> visited = new HashSet<>();
    private volatile boolean isCancelled = false;

    public SiteIndexerTask(Site site, SiteRepository siteRepository, PageRepository pageRepository, String url) {
        this.site = site;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.url = url;
    }

    public void cancel() {
        isCancelled = true;
    }

    @Override
    protected void compute() {
        if (!isCancelled) {
            processPage(url);
        }
    }

    private void processPage(String link) {
        if (isCancelled || !visited.add(link)) return;

        try {
            Document doc = Jsoup.connect(link)
                    .userAgent("HeliontSearchBot")
                    .referrer("https://google.com")
                    .get();

            String path = link.replaceFirst(site.getUrl(), "");
            if (path.isEmpty()) path = "/";

            Page page = new Page();
            page.setPath(path);
            page.setCode(doc.connection().response().statusCode());
            page.setContent(doc.html());
            page.setSite(site);

            pageRepository.save(page);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);

            Elements links = doc.select("a[href]");
            Set<SiteIndexerTask> subTasks = new HashSet<>();

            for (var element : links) {
                String href = element.absUrl("href");
                if (href.startsWith(site.getUrl()) && !href.contains("#") && !href.endsWith(".pdf")) {
                    SiteIndexerTask subTask = new SiteIndexerTask(site, siteRepository, pageRepository, href);
                    if (!isCancelled) {
                        subTask.fork();
                        subTasks.add(subTask);
                    }
                }
            }

            subTasks.forEach(SiteIndexerTask::join);

        } catch (IOException e) {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Ошибка при обработке страницы: " + link);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }
    }
}