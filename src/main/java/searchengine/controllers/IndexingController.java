package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.services.IndexingService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j // 👉 добавляем логгер
public class IndexingController {

    private final IndexingService indexingService;

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        log.info("🔄 Получен запрос: /startIndexing");
        IndexingResponse response = indexingService.startIndexing();
        log.info("✅ Ответ: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        log.info("⛔ Получен запрос: /stopIndexing");
        IndexingResponse response = indexingService.stopIndexing();
        log.info("🛑 Ответ: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestParam String url) {
        log.info("📄 Индексация одной страницы: {}", url);
        IndexingResponse response = indexingService.indexPage(url);
        log.info("📦 Ответ: {}", response);
        return ResponseEntity.ok(response);
    }
}