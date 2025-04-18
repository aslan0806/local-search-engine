package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.model.SearchLog;
import searchengine.services.SearchLogService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final SearchLogService logService;

    @GetMapping("/top-queries")
    public ResponseEntity<List<Object[]>> topQueries() {
        return ResponseEntity.ok(logService.getTopQueries());
    }

    @GetMapping("/top-sites")
    public ResponseEntity<List<Object[]>> topSites() {
        return ResponseEntity.ok(logService.getTopSites());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SearchLog>> recentLogs() {
        return ResponseEntity.ok(logService.getLastLogs());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<SearchLog>> filterByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(logService.getLogsBetweenDates(from, to));
    }
}