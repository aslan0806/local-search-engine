package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResultItem;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final LemmaService lemmaService;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;

    @Override
    public SearchResponse search(String query, String siteUrl, int offset, int limit) {
        SearchResponse response = new SearchResponse();
        if (query == null || query.trim().isEmpty()) {
            response.setResult(false);
            response.setData(Collections.emptyList());
            response.setCount(0);
            return response;
        }

        Map<String, Integer> lemmas = lemmaService.lemmatize(query);
        if (lemmas.isEmpty()) {
            response.setResult(false);
            response.setData(Collections.emptyList());
            response.setCount(0);
            return response;
        }

        Set<Page> foundPages = new HashSet<>();
        List<Lemma> lemmaEntities = new ArrayList<>();

        List<SiteEntity> sitesToSearch = siteUrl == null
                ? siteRepository.findAll()
                : Collections.singletonList(siteRepository.findByUrl(siteUrl));

        for (SiteEntity site : sitesToSearch) {
            for (String lemmaText : lemmas.keySet()) {
                lemmaRepository.findByLemmaAndSite(lemmaText, site)
                        .ifPresent(lemmaEntities::add);
            }
        }

        if (lemmaEntities.isEmpty()) {
            response.setResult(false);
            response.setData(Collections.emptyList());
            response.setCount(0);
            return response;
        }

        Map<Page, Float> pageRelevance = new HashMap<>();
        for (Lemma lemma : lemmaEntities) {
            for (Index index : lemma.getIndexes()) {
                Page page = index.getPage();
                pageRelevance.put(page,
                        pageRelevance.getOrDefault(page, 0f) + index.getRank());
            }
        }

        List<Map.Entry<Page, Float>> sorted = new ArrayList<>(pageRelevance.entrySet());
        sorted.sort(Map.Entry.<Page, Float>comparingByValue().reversed());

        List<SearchResultItem> resultItems = new ArrayList<>();
        int end = Math.min(offset + limit, sorted.size());
        for (int i = offset; i < end; i++) {
            Page page = sorted.get(i).getKey();
            Float relevance = sorted.get(i).getValue();
            resultItems.add(toSearchResult(page, relevance));
        }

        response.setResult(true);
        response.setCount(sorted.size());
        response.setData(resultItems);
        return response;
    }

    private SearchResultItem toSearchResult(Page page, float relevance) {
        SearchResultItem item = new SearchResultItem();
        item.setSite(page.getSite().getUrl());
        item.setSiteName(page.getSite().getName());
        item.setUri(page.getPath());

        String text = Jsoup.parse(page.getContent()).text();
        item.setTitle(extractTitle(text));
        item.setSnippet(makeSnippet(text));
        item.setRelevance(relevance);

        return item;
    }

    private String extractTitle(String text) {
        return text.length() > 50 ? text.substring(0, 50) + "..." : text;
    }

    private String makeSnippet(String text) {
        return text.length() > 150 ? text.substring(0, 150) + "..." : text;
    }
}