package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
public class IndexingTask {

    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    @Transactional
    public void indexSite(SiteEntity site) {
        try {
            // Загрузка главной страницы сайта
            String url = site.getUrl();
            Document doc = Jsoup.connect(url).get();
            String html = doc.outerHtml();

            // Сохраняем страницу
            Page page = new Page();
            page.setSite(site);
            page.setPath("/"); // индексируем главную страницу
            page.setCode(200);
            page.setContent(html);
            pageRepository.save(page);

            // Лемматизация
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

            System.out.println("✅ Индексация сайта завершена: " + url);

        } catch (IOException e) {
            System.out.println("❌ Ошибка при индексации сайта: " + site.getUrl());
            e.printStackTrace();
        }
    }

    @Transactional
    public void indexPage(String fullUrl, SiteEntity site) {
        try {
            Document doc = Jsoup.connect(fullUrl).get();
            String html = doc.outerHtml();

            Page page = new Page();
            page.setSite(site);
            page.setPath(fullUrl.replace(site.getUrl(), ""));
            page.setCode(200);
            page.setContent(html);
            pageRepository.save(page);

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

            System.out.println("✅ Индексирована страница: " + fullUrl);

        } catch (IOException e) {
            System.out.println("❌ Ошибка при индексации страницы: " + fullUrl);
            e.printStackTrace();
        }
    }
}