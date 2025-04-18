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

    @GetMapping("/latest")
    public List<SearchLog> latest() {
        return searchLogService.getLastLogs();
    }

    @GetMapping("/top/queries")
    public List<Object[]> topQueries() {
        return searchLogService.getTopQueries();
    }

    @GetMapping("/top/sites")
    public List<Object[]> topSites() {
        return searchLogService.getTopSites();
    }

    @GetMapping("/export/filtered")
    public ResponseEntity<byte[]> exportLogsFiltered(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        byte[] data = searchLogService.exportLogsFiltered(format, from, to);

        String filename = "logs_" + from.toLocalDate() + "_to_" + to.toLocalDate() + "." + format;
        String contentType = format.equalsIgnoreCase("json") ? "application/json" : "text/csv";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", contentType)
                .body(data);
    }
}