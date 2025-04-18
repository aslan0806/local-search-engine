package searchengine.services;

import searchengine.model.SearchLog;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchLogService {
    List<SearchLog> getLastLogs();
    List<Object[]> getTopQueries();
    List<Object[]> getTopSites();
    List<SearchLog> getLogsBetween(LocalDateTime from, LocalDateTime to);
    byte[] exportLogs(String format); // 🆕 Добавлен метод экспорта
}