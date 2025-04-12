package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
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

    public void indexPage(String url, SiteEntity site) throws IOException {
        // 1. Получаем HTML по URL
        Document document = Jsoup.connect(url).get();
        String htmlContent = document.outerHtml();
        String textContent = document.text();

        // 2. Сохраняем страницу
        Page page = new Page();
        page.setSite(site);
        page.setPath(url.replace(site.getUrl(), ""));
        page.setCode(200);
        page.setContent(htmlContent);
        pageRepository.save(page);

        // 3. Лемматизация текста страницы
        Map<String, Integer> lemmas = lemmaService.lemmatize(textContent);

        // 4. Сохраняем леммы и индексы
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaText = entry.getKey();
            int frequency = entry.getValue();

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
            index.setRank(frequency);
            indexRepository.save(index);
        }
    }
}