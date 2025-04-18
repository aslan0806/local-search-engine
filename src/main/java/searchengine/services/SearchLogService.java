package searchengine.services;

import searchengine.dto.statistics.SearchLogStatistics;

public interface SearchLogService {
    SearchLogStatistics getStatistics();
}