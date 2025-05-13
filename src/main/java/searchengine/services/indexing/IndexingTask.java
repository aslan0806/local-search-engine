package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.services.LemmaService;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndexingTask {

    private final PageRepository pageRepository;
    private final LemmaService lemmaService;

    @Async
    @Transactional
    public void indexPage(SiteEntity site, String path) throws IOException {
        String fullUrl = site.getUrl() + path;
        log.info("📥 Индексация страницы: {}", fullUrl);

        // 1. Загрузка HTML-документа
        Document doc = Jsoup.connect(fullUrl)
                .userAgent("Mozilla/5.0 (compatible; search-bot/1.0)")
                .referrer("http://www.google.com")
                .get();

        String html = doc.outerHtml();
        String text = doc.body().text();

        // 2. Сохраняем страницу
        Page page = new Page();
        page.setSite(site);
        page.setPath(path.isEmpty() ? "/" : path);
        page.setCode(200);
        page.setContent(html);
        pageRepository.save(page);

        // 3. Получаем леммы
        Map<String, Integer> lemmas = lemmaService.lemmatize(text);

        // 4. Сохраняем леммы и индексы
        lemmaService.saveLemmas(site, page, lemmas);

        log.info("✅ Индексация завершена: {}", fullUrl);
    }

    public void indexSite(SiteEntity site) {
        // Можно реализовать рекурсивный обход или загрузку корневой страницы
        // Оставим пока пустым, зависит от архитектуры обхода
    }
}