package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchLogStatistics {
    private long totalLogs;
    private long uniqueQueries;
    private long uniqueSites;
}