package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.PageIndex;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.services.LemmaService;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class SiteIndexerTask extends RecursiveAction {

    private final String url;
    private final SiteEntity site;
    private final Set<String> visited;

    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    @Override
    protected void compute() {
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

            Map<String, Integer> lemmas = lemmaService.lemmatize(doc.text());
            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                String lemmaStr = entry.getKey();
                int count = entry.getValue();

                Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaStr, site)
                        .orElseGet(() -> {
                            Lemma newLemma = new Lemma();
                            newLemma.setLemma(lemmaStr);
                            newLemma.setSite(site);
                            newLemma.setFrequency(0);
                            return newLemma;
                        });

                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaRepository.save(lemma);

                PageIndex index = new PageIndex();
                index.setPage(page);
                index.setLemma(lemma);
                index.setRank(count);
                indexRepository.save(index);
            }

            Elements links = doc.select("a[href]");
            invokeAll(
                    links.stream()
                            .map(link -> link.absUrl("href"))
                            .filter(link -> link.startsWith(site.getUrl()))
                            .filter(link -> !visited.contains(link))
                            .map(link -> new SiteIndexerTask(link, site, visited, pageRepository, lemmaRepository, indexRepository, lemmaService))
                            .toList()
            );

        } catch (IOException e) {
            System.out.println("❌ Ошибка при загрузке страницы: " + url);
        }
    }
}