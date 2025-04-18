package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.SearchLogEntry;
import searchengine.dto.statistics.SearchLogStatistics;
import searchengine.model.SearchLog;
import searchengine.repositories.SearchLogRepository;
import searchengine.services.SearchLogService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchLogServiceImpl implements SearchLogService {

    private final SearchLogRepository searchLogRepository;

    @Override
    public SearchLogStatistics getStatistics() {
        SearchLogStatistics stats = new SearchLogStatistics();

        stats.setTotalSearches((int) searchLogRepository.count());

        stats.setTopQueries(searchLogRepository.findTopQueries().stream()
                .map(arr -> arr[0] + " (" + arr[1] + ")")
                .limit(10)
                .collect(Collectors.toList()));

        stats.setQueriesPerSite(searchLogRepository.countBySite().stream()
                .map(arr -> arr[0] + ": " + arr[1] + " запросов")
                .collect(Collectors.toList()));

        List<SearchLogEntry> lastEntries = searchLogRepository.findTop10ByOrderByTimestampDesc().stream()
                .map(log -> {
                    SearchLogEntry entry = new SearchLogEntry();
                    entry.setQuery(log.getQuery());
                    entry.setSite(log.getSite());
                    entry.setOffset(log.getOffset());
                    entry.setLimit(log.getLimit());
                    entry.setResults(log.getResults());
                    entry.setTimestamp(log.getTimestamp());
                    return entry;
                })
                .collect(Collectors.toList());

        stats.setLastQueries(lastEntries);

        return stats;
    }
}