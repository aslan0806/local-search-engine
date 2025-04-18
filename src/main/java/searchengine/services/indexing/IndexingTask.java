package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.services.LemmaService;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndexingTask {

    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    @Async
    @Transactional
    public void indexPage(SiteEntity site, String path) throws IOException {
        String fullUrl = site.getUrl() + path;
        log.info("🔍 Начинаем индексацию: {}", fullUrl);

        Document doc = Jsoup.connect(fullUrl).get();
        String html = doc.outerHtml();

        Page page = new Page();
        page.setSite(site);
        page.setPath(path);
        page.setCode(200);
        page.setContent(html);
        pageRepository.save(page);
        log.info("📄 Сохранена страница: {}", path);

        Map<String, Integer> lemmas = lemmaService.lemmatize(doc.text());

        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaText = entry.getKey();
            int count = entry.getValue();

            Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaText, site)
                    .orElseGet(() -> {
                        Lemma newLemma = new Lemma();
                        newLemma.setSite(site);
                        newLemma.setLemma(lemmaText);
                        newLemma.setFrequency(0);
                        return newLemma;
                    });

            lemma.setFrequency(lemma.getFrequency() + 1);
            lemmaRepository.save(lemma);

            Index index = new Index();
            index.setPage(page);
            index.setLemma(lemma);
            index.setRank(count);
            indexRepository.save(index);
        }

        log.info("✅ Индексация завершена: {}", fullUrl);
    }
}