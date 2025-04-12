package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.net.URL;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IndexingTask {

    private final PageLoader pageLoader;
    private final LemmaService lemmaService;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Transactional
    public void indexPage(SiteEntity site, String path) {
        try {
            String fullUrl = site.getUrl() + path;
            String html = pageLoader.loadHtml(fullUrl);
            Document document = Jsoup.parse(html);
            String content = document.body().text();

            // 1. Сохраняем страницу
            Page page = new Page();
            page.setPath(path);
            page.setCode(200);
            page.setContent(html);
            page.setSite(site);
            pageRepository.save(page);

            // 2. Лемматизация
            Map<String, Integer> lemmas = lemmaService.lemmatize(content);

            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                String lemmaText = entry.getKey();
                int frequency = entry.getValue();

                // 3. Лемма: найдём или создадим
                Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaText, site)
                        .orElseGet(() -> {
                            Lemma newLemma = new Lemma();
                            newLemma.setLemma(lemmaText);
                            newLemma.setFrequency(0);
                            newLemma.setSite(site);
                            return newLemma;
                        });

                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaRepository.save(lemma);

                // 4. Индекс
                Index index = new Index();
                index.setPage(page);
                index.setLemma(lemma);
                index.setRank(frequency); // количество раз, которое встретилось
                indexRepository.save(index);
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка индексации " + path + ": " + e.getMessage());
        }
    }
}