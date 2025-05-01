package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.SearchLogStatistics;
import searchengine.model.SearchLog;
import searchengine.services.SearchLogService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class SearchLogController {

    private final SearchLogService logService;

    @GetMapping("/last")
    public ResponseEntity<List<SearchLog>> getLastLogs() {
        List<SearchLog> logs = logService.getLastLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/statistics")
    public ResponseEntity<SearchLogStatistics> getStatistics() {
        SearchLogStatistics statistics = logService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam String format,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        byte[] data;
        if (from != null && to != null) {
            LocalDateTime fromTime = LocalDateTime.parse(from);
            LocalDateTime toTime = LocalDateTime.parse(to);
            data = logService.exportLogsFiltered(format, fromTime, toTime);
        } else {
            data = logService.exportLogs(format);
        }

        String contentType = format.equalsIgnoreCase("json") ?
                MediaType.APPLICATION_JSON_VALUE :
                "text/csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=search-logs." + format)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}