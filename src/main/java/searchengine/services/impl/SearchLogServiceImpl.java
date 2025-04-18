package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.SearchLog;
import searchengine.repositories.SearchLogRepository;
import searchengine.services.SearchLogService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchLogServiceImpl implements SearchLogService {

    private final SearchLogRepository repository;

    @Override
    public List<Object[]> getTopQueries() {
        return repository.findTopQueries();
    }

    @Override
    public List<Object[]> getTopSites() {
        return repository.countBySite();
    }

    @Override
    public List<SearchLog> getLastLogs() {
        return repository.findTop10ByOrderByTimestampDesc();
    }

    @Override
    public List<SearchLog> getLogsBetween(LocalDateTime start, LocalDateTime end) {
        return repository.findByDateRange(start, end);
    }
}