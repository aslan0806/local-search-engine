package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.search.SearchResponse;
import searchengine.services.SearchService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        log.info("🔍 Поиск: query='{}', site='{}', offset={}, limit={}", query, site, offset, limit);
        SearchResponse response = searchService.search(query, site, offset, limit);
        log.info("🔎 Найдено результатов: {}", response.getCount());
        return ResponseEntity.ok(response);
    }
}