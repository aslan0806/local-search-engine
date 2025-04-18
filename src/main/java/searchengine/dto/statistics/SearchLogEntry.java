package searchengine.dto.statistics;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchLogEntry {
    private String query;
    private String site;
    private int offset;
    private int limit;
    private int results;
    private LocalDateTime timestamp;
}