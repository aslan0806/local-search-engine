package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResultItem;
import searchengine.model.*;
import searchengine.repositories.*;
import searchengine.services.LemmaService;
import searchengine.services.SearchService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;
    private final SearchLogRepository searchLogRepository;

    @Override
    public SearchResponse search(String query, String siteUrl, int offset, int limit) {
        if (query == null || query.isBlank()) {
            return logAndReturn(query, siteUrl, offset, limit, 0,
                    new SearchResponse(false, 0, Collections.emptyList()));
        }

        List<String> queryLemmas = new ArrayList<>(lemmaService.lemmatize(query).keySet());

        if (queryLemmas.isEmpty()) {
            return logAndReturn(query, siteUrl, offset, limit, 0,
                    new SearchResponse(false, 0, Collections.emptyList()));
        }

        List<SiteEntity> sites = (siteUrl == null || siteUrl.isBlank())
                ? siteRepository.findAll()
                : Collections.singletonList(siteRepository.findByUrl(siteUrl));

        List<SearchResultItem> results = new ArrayList<>();

        for (SiteEntity site : sites) {
            List<Lemma> lemmas = lemmaRepository.findAllByLemmaInAndSite(queryLemmas, site);
            if (lemmas.isEmpty()) continue;

            List<Index> indexes = indexRepository.findAllByLemmaIn(lemmas);

            Map<Page, Float> relevanceMap = new HashMap<>();
            for (Index index : indexes) {
                Page page = index.getPage();
                float rank = index.getRank();
                relevanceMap.put(page, relevanceMap.getOrDefault(page, 0f) + rank);
            }

            for (Map.Entry<Page, Float> entry : relevanceMap.entrySet()) {
                Page page = entry.getKey();
                float relevance = entry.getValue();

                Document doc = Jsoup.parse(page.getContent());
                String title = doc.title();
                String bodyText = Jsoup.clean(doc.body().text(), Safelist.none());
                String snippet = makeSnippet(bodyText, queryLemmas);

                SearchResultItem item = new SearchResultItem();
                item.setSite(site.getUrl());
                item.setSiteName(site.getName());
                item.setUri(page.getPath());
                item.setTitle(title);
                item.setSnippet(snippet);
                item.setRelevance(relevance);

                results.add(item);
            }
        }

        results.sort(Comparator.comparing(SearchResultItem::getRelevance).reversed());
        List<SearchResultItem> page = results.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

        return logAndReturn(query, siteUrl, offset, limit, results.size(),
                new SearchResponse(true, results.size(), page));
    }

    private SearchResponse logAndReturn(String query, String site, int offset, int limit, int resultsCount, SearchResponse response) {
        SearchLog log = new SearchLog();
        log.setQuery(query);
        log.setSite(site);
        log.setOffset(offset);
        log.setLimit(limit);
        log.setResults(resultsCount);
        log.setTimestamp(LocalDateTime.now());
        searchLogRepository.save(log);
        return response;
    }

    private String makeSnippet(String text, List<String> lemmas) {
        for (String lemma : lemmas) {
            if (text.toLowerCase().contains(lemma)) {
                int index = text.toLowerCase().indexOf(lemma);
                int start = Math.max(0, index - 30);
                int end = Math.min(text.length(), index + 70);
                String snippet = text.substring(start, end);
                return snippet.replaceAll("(?i)(" + lemma + ")", "<b>$1</b>");
            }
        }
        return text.length() > 150 ? text.substring(0, 150) + "..." : text;
    }
}