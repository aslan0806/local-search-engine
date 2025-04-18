package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.model.SearchLog;
import searchengine.repositories.SearchLogRepository;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs")
public class LogExportController {

    private final SearchLogRepository logRepository;

    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv() {
        List<SearchLog> logs = logRepository.findAll();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);

        // Заголовки CSV
        writer.println("ID,Query,Site,Offset,Limit,Results,Timestamp");

        for (SearchLog log : logs) {
            writer.printf("%d,\"%s\",\"%s\",%d,%d,%d,\"%s\"\n",
                    log.getId(),
                    log.getQuery(),
                    log.getSite(),
                    log.getOffset(),
                    log.getLimit(),
                    log.getResults(),
                    log.getTimestamp()
            );
        }

        writer.flush();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=search_log.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(baos.toByteArray());
    }

    @GetMapping("/json")
    public List<SearchLog> exportJson() {
        return logRepository.findAll();
    }
}