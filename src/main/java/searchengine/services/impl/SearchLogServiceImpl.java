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

    private final SearchLogRepository logRepository;

    @Override
    public List<Object[]> getTopQueries() {
        return logRepository.findTopQueries();
    }

    @Override
    public List<Object[]> getTopSites() {
        return logRepository.countBySite();
    }

    @Override
    public List<SearchLog> getLastLogs() {
        return logRepository.findTop10ByOrderByTimestampDesc();
    }

    @Override
    public List<SearchLog> getLogsBetweenDates(LocalDateTime from, LocalDateTime to) {
        return logRepository.findAllByTimestampBetween(from, to);
    }
}