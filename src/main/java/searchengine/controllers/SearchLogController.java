package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.SearchLogStatistics;
import searchengine.services.SearchLogService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs")
public class SearchLogController {

    private final SearchLogService searchLogService;

    @GetMapping("/statistics")
    public SearchLogStatistics getLogStats() {
        return searchLogService.getStatistics();
    }
}