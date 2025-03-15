package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    // Добавьте SearchService, если необходимо

    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() {
        boolean started = indexingService.startIndexing();
        return ResponseEntity.ok("{\"result\": " + started + "}");
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing() {
        boolean stopped = indexingService.stopIndexing();
        return ResponseEntity.ok("{\"result\": " + stopped + "}");
    }

    @PostMapping("/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam String url) {
        boolean indexed = indexingService.indexPage(url);
        if (indexed) {
            return ResponseEntity.ok("{\"result\": true}");
        } else {
            return ResponseEntity.badRequest().body("{\"result\": false, \"error\": \"Ошибка индексации\"}");
        }
    }

    // Метод для поиска /api/search можно добавить аналогичным образом
}