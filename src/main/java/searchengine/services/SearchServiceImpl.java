package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResultItem;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositories.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final LemmaService lemmaService;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    @Override
    public SearchResponse search(String query, String siteUrl, int offset, int limit) {
        SearchResponse response = new SearchResponse();
        if (query.isBlank()) {
            response.setResult(false);
            return response;
        }

        // 1. Определяем сайт
        List<SiteEntity> sites = siteUrl == null
                ? siteRepository.findAll()
                : Collections.singletonList(siteRepository.findByUrl(siteUrl));

        // 2. Получаем леммы из запроса
        Map<String, Integer> lemmas = lemmaService.lemmatize(query);

        // 3. Получаем все леммы, существующие в базе
        List<Lemma> foundLemmas = new ArrayList<>();
        for (SiteEntity site : sites) {
            for (String lemma : lemmas.keySet()) {
                lemmaRepository.findByLemmaAndSite(lemma, site).ifPresent(foundLemmas::add);
            }
        }

        if (foundLemmas.isEmpty()) {
            response.setResult(true);
            response.setCount(0);
            response.setData(Collections.emptyList());
            return response;
        }

        // 4. Собираем все страницы, на которых есть хотя бы одна из лемм
        Map<Page, Float> pageRelevance = new HashMap<>();
        for (Lemma lemma : foundLemmas) {
            List<Index> indexes = indexRepository.findAll()
                    .stream()
                    .filter(i -> i.getLemma().getId() == lemma.getId())
                    .toList();

            for (Index index : indexes) {
                Page page = index.getPage();
                float rank = index.getRank();
                pageRelevance.put(page, pageRelevance.getOrDefault(page, 0f) + rank);
            }
        }

        // 5. Сортируем по убыванию релевантности
        List<Map.Entry<Page, Float>> sorted = pageRelevance.entrySet()
                .stream()
                .sorted(Map.Entry.<Page, Float>comparingByValue().reversed())
                .skip(offset)
                .limit(limit)
                .toList();

        // 6. Формируем результат
        List<SearchResultItem> items = new ArrayList<>();
        for (Map.Entry<Page, Float> entry : sorted) {
            Page page = entry.getKey();
            float relevance = entry.getValue();

            SearchResultItem item = new SearchResultItem();
            item.setSite(page.getSite().getUrl());
            item.setSiteName(page.getSite().getName());
            item.setUri(page.getPath());
            item.setTitle(getTitle(page.getContent()));
            item.setSnippet(getSnippet(page.getContent(), lemmas.keySet()));
            item.setRelevance(relevance);
            items.add(item);
        }

        response.setResult(true);
        response.setCount(pageRelevance.size());
        response.setData(items);
        return response;
    }

    private String getTitle(String html) {
        return Jsoup.parse(html).title();
    }

    private String getSnippet(String html, Set<String> lemmas) {
        String text = Jsoup.parse(html).text();
        for (String lemma : lemmas) {
            int idx = text.toLowerCase().indexOf(lemma.toLowerCase());
            if (idx != -1) {
                int start = Math.max(0, idx - 30);
                int end = Math.min(text.length(), idx + 60);
                return "... " + text.substring(start, end).replaceAll("(?i)(" + lemma + ")", "<b>$1</b>") + " ...";
            }
        }
        return text.length() > 160 ? text.substring(0, 160) + "..." : text;
    }
}