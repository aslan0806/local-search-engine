package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SiteIndexerService {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    private final Set<String> visited = new HashSet<>();

    @Transactional
    public void indexSite(SiteEntity site) {
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);

        try {
            crawlSite(site.getUrl(), site);
            site.setStatus(SiteStatus.INDEXED);
        } catch (Exception e) {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Ошибка: " + e.getMessage());
        }

        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    private void crawlSite(String baseUrl, SiteEntity site) throws IOException {
        crawlPage(baseUrl, site);
    }

    private void crawlPage(String url, SiteEntity site) {
        if (visited.contains(url)) return;
        visited.add(url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot")
                    .referrer("https://google.com")
                    .get();

            Page page = new Page();
            page.setCode(200);
            page.setContent(doc.html());
            page.setPath(url.replace(site.getUrl(), ""));
            page.setSite(site);

            System.out.println("✔️ Сохраняем страницу: " + page.getPath());
            pageRepository.save(page);

            Elements links = doc.select("a[href]");
            for (var element : links) {
                String link = element.absUrl("href");
                if (link.startsWith(site.getUrl()) && !visited.contains(link)) {
                    crawlPage(link, site);
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Не удалось загрузить: " + url);
        }
    }
}