package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.repositories.SearchLogRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs/stats")
public class LogStatsController {

    private final SearchLogRepository logRepository;

    @GetMapping("/top-queries")
    public List<Map<String, Object>> getTopQueries() {
        List<Object[]> results = logRepository.findTopQueries();
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("query", row[0]);
            map.put("count", row[1]);
            return map;
        }).toList();
    }

    @GetMapping("/by-site")
    public List<Map<String, Object>> getCountBySite() {
        List<Object[]> results = logRepository.countBySite();
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("site", row[0]);
            map.put("count", row[1]);
            return map;
        }).toList();
    }

    @GetMapping("/recent")
    public List<?> getRecentLogs() {
        return logRepository.findTop10ByOrderByTimestampDesc();
    }
}