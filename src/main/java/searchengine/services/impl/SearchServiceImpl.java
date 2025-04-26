package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResultItem;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.LemmaService;
import searchengine.services.SearchService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    @Override
    public SearchResponse search(String query, String siteUrl, int offset, int limit) {
        log.info("🔎 Новый поисковый запрос: '{}', сайт: '{}', offset={}, limit={}", query, siteUrl, offset, limit);

        if (query == null || query.isBlank()) {
            return new SearchResponse(false, 0, Collections.emptyList());
        }

        List<String> queryLemmas = new ArrayList<>(lemmaService.lemmatize(query).keySet());
        if (queryLemmas.isEmpty()) {
            return new SearchResponse(false, 0, Collections.emptyList());
        }

        List<SiteEntity> sites = (siteUrl == null || siteUrl.isBlank())
                ? siteRepository.findAll()
                : Collections.singletonList(siteRepository.findByUrl(siteUrl));

        List<SearchResultItem> results = new ArrayList<>();
        float maxRelevance = 0f;

        for (SiteEntity site : sites) {
            List<Lemma> lemmas = lemmaRepository.findAllByLemmaInAndSite(queryLemmas, site);
            if (lemmas.isEmpty()) continue;

            List<Object[]> relevanceData = indexRepository.findPageRelevance(lemmas);

            for (Object[] row : relevanceData) {
                Integer pageId = (Integer) row[0];
                Double rank = (Double) row[1];

                Optional<Page> pageOpt = pageRepository.findById(pageId);
                if (pageOpt.isEmpty()) continue;

                Page page = pageOpt.get();
                Document doc = Jsoup.parse(page.getContent());
                String title = doc.title();
                String bodyText = Jsoup.clean(doc.body().text(), Safelist.none());
                String snippet = makeSnippet(bodyText, queryLemmas);

                SearchResultItem item = new SearchResultItem();
                item.setSite(page.getSite().getUrl());
                item.setSiteName(page.getSite().getName());
                item.setUri(page.getPath());
                item.setTitle(title);
                item.setSnippet(snippet);
                item.setRelevance(rank.floatValue());

                results.add(item);
                maxRelevance = Math.max(maxRelevance, rank.floatValue());
            }
        }

        // Нормализация релевантности
        for (SearchResultItem item : results) {
            item.setRelevance(item.getRelevance() / maxRelevance);
        }

        results.sort(Comparator.comparing(SearchResultItem::getRelevance).reversed());

        int fromIndex = Math.min(offset, results.size());
        int toIndex = Math.min(fromIndex + limit, results.size());

        return new SearchResponse(true, results.size(), results.subList(fromIndex, toIndex));
    }

    private String makeSnippet(String text, List<String> lemmas) {
        for (String lemma : lemmas) {
            String lowerText = text.toLowerCase();
            if (lowerText.contains(lemma)) {
                int idx = lowerText.indexOf(lemma);
                int start = Math.max(0, idx - 30);
                int end = Math.min(text.length(), idx + 70);
                String snippet = text.substring(start, end);
                return snippet.replaceAll("(?i)(" + lemma + ")", "<b>$1</b>");
            }
        }
        return text.length() > 150 ? text.substring(0, 150) + "..." : text;
    }
}