package searchengine.dto.statistics;

import lombok.Data;

import java.util.List;

@Data
public class SearchLogStatistics {
    private int totalSearches;
    private List<String> topQueries;
    private List<String> queriesPerSite;
    private List<SearchLogEntry> lastQueries;
}