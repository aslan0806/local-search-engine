package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.*;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SiteRepository siteRepository;

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {
        // 💡 Пока заглушка — только формат
        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(1);

        SearchResultItem item = new SearchResultItem();
        item.setSite(site != null ? site : "https://example.com");
        item.setSiteName("Example Site");
        item.setUri("/example-path");
        item.setTitle("Example Title");
        item.setSnippet("Это пример <b>результата</b> для запроса: " + query);
        item.setRelevance(0.95f);

        List<SearchResultItem> data = new ArrayList<>();
        data.add(item);

        response.setData(data);
        return response;
    }
}