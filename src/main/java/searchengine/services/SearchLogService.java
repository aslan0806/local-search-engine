package searchengine.services;

import searchengine.dto.statistics.SearchLogStatistics;
import searchengine.model.SearchLog;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchLogService {
    List<SearchLog> getLastLogs();
    List<Object[]> getTopQueries();
    List<Object[]> getTopSites();
    List<SearchLog> getLogsBetween(LocalDateTime from, LocalDateTime to);
    byte[] exportLogs(String format);
    byte[] exportLogsFiltered(String format, LocalDateTime from, LocalDateTime to);

    SearchLogStatistics getStatistics(); // 🔥 Новый метод для общей статистики
}