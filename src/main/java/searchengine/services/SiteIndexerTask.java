package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

@Component
@RequiredArgsConstructor
public class SiteIndexerTask extends RecursiveAction {

    private final SiteEntity site;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    private final Set<String> visited = new HashSet<>();
    private final boolean isCancelled = false; // пока без остановки, можно будет доработать

    @Override
    @Transactional
    protected void compute() {
        site.setStatus(StatusType.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);

        try {
            indexSite(site.getUrl());
            site.setStatus(StatusType.INDEXED);
        } catch (Exception e) {
            site.setStatus(StatusType.FAILED);
            site.setLastError(e.getMessage());
        }

        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    private void indexSite(String baseUrl) throws IOException {
        processPage(baseUrl);
    }

    private void processPage(String url) {
        if (visited.contains(url)) return;
        visited.add(url);

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

            // Рекурсивная индексация ссылок
            Elements links = doc.select("a[href]");
            for (var element : links) {
                String link = element.absUrl("href");
                if (link.startsWith(site.getUrl()) && !visited.contains(link)) {
                    processPage(link);
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Ошибка при загрузке страницы: " + url);
        }
    }
}