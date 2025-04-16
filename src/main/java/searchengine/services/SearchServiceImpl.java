package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResultItem;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.LemmaService;
import searchengine.services.SearchService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    @Override
    public SearchResponse search(String query, String siteUrl, int offset, int limit) {
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

        List<SearchResultItem> allResults = new ArrayList<>();

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

                String snippet = makeSnippet(doc.text(), queryLemmas);

                SearchResultItem item = new SearchResultItem();
                item.setSite(site.getUrl());
                item.setSiteName(site.getName());
                item.setUri(page.getPath());
                item.setTitle(doc.title());
                item.setSnippet(snippet);
                item.setRelevance(relevance);

                allResults.add(item);
            }
        }

        allResults.sort(Comparator.comparing(SearchResultItem::getRelevance).reversed());

        // ✅ Добавим пагинацию
        int toIndex = Math.min(offset + limit, allResults.size());
        List<SearchResultItem> paged = allResults.subList(
                Math.min(offset, allResults.size()),
                toIndex
        );

        return new SearchResponse(true, allResults.size(), paged);
    }

    private String makeSnippet(String text, List<String> lemmas) {
        for (String lemma : lemmas) {
            int index = text.toLowerCase().indexOf(lemma);
            if (index != -1) {
                int start = Math.max(0, index - 30);
                int end = Math.min(text.length(), index + 70);
                String snippet = text.substring(start, end);
                return snippet.replaceAll("(?i)(" + lemma + ")", "<b>$1</b>");
            }
        }
        return text.length() > 150 ? text.substring(0, 150) + "..." : text;
    }
}