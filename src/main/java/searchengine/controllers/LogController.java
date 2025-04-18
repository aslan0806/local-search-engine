package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.services.SearchLogService;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final SearchLogService searchLogService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam(defaultValue = "csv") String format
    ) {
        byte[] data = searchLogService.exportLogs(format);
        String filename = "logs_export." + format;
        String contentType = format.equalsIgnoreCase("json") ? "application/json" : "text/csv";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", contentType)
                .body(data);
    }
}