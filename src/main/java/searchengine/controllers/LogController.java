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
@RequiredArgsConstructor
@RequestMapping("/api/logs")
public class LogController {

    private final SearchLogService searchLogService;

    @GetMapping("/recent")
    public ResponseEntity<List<SearchLog>> getRecentLogs() {
        return ResponseEntity.ok(searchLogService.getLastLogs());
    }

    @GetMapping("/top-queries")
    public ResponseEntity<List<Object[]>> getTopQueries() {
        return ResponseEntity.ok(searchLogService.getTopQueries());
    }

    @GetMapping("/top-sites")
    public ResponseEntity<List<Object[]>> getTopSites() {
        return ResponseEntity.ok(searchLogService.getTopSites());
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<SearchLog>> getLogsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(searchLogService.getLogsBetween(from, to));
    }
}