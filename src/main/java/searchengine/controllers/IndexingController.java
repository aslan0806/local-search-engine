package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.services.IndexingService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IndexingController {

    private final IndexingService indexingService;

    @PostMapping("/startIndexing")
    public ResponseEntity<Map<String, Object>> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    @PostMapping("/stopIndexing")
    public ResponseEntity<Map<String, Object>> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    // ✅ Новый метод
    @PostMapping("/indexPage")
    public ResponseEntity<Map<String, Object>> indexPage(@RequestParam String url) {
        return ResponseEntity.ok(indexingService.indexPage(url));
    }
}