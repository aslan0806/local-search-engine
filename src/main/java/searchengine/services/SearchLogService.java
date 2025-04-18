package searchengine.services;

import searchengine.model.SearchLog;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchLogService {
    List<Object[]> getTopQueries();
    List<Object[]> getTopSites();
    List<SearchLog> getLastLogs();
    List<SearchLog> getLogsBetweenDates(LocalDateTime from, LocalDateTime to);
}