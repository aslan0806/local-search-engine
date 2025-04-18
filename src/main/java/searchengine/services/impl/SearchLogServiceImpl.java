package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.SearchLog;
import searchengine.repositories.SearchLogRepository;
import searchengine.services.SearchLogService;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchLogServiceImpl implements SearchLogService {

    private final SearchLogRepository logRepository;

    @Override
    public List<SearchLog> getLastLogs() {
        return logRepository.findTop10ByOrderByTimestampDesc();
    }

    @Override
    public List<Object[]> getTopQueries() {
        return logRepository.findTopQueries();
    }

    @Override
    public List<Object[]> getTopSites() {
        return logRepository.countBySite();
    }

    @Override
    public List<SearchLog> getLogsBetween(LocalDateTime from, LocalDateTime to) {
        return logRepository.findAllByTimestampBetween(from, to);
    }

    @Override
    public byte[] exportLogs(String format) {
        List<SearchLog> logs = logRepository.findAll();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {

            if (format.equalsIgnoreCase("json")) {
                writer.write("[\n");
                for (int i = 0; i < logs.size(); i++) {
                    SearchLog log = logs.get(i);
                    writer.write(String.format(
                            "  {\"query\":\"%s\",\"site\":\"%s\",\"timestamp\":\"%s\"}%s\n",
                            log.getQuery(), log.getSite(), log.getTimestamp(),
                            (i < logs.size() - 1) ? "," : ""
                    ));
                }
                writer.write("]");
            } else {
                writer.write("Query,Site,Timestamp\n");
                for (SearchLog log : logs) {
                    writer.write(String.format("\"%s\",\"%s\",\"%s\"\n",
                            log.getQuery(), log.getSite(), log.getTimestamp()));
                }
            }

            writer.flush();
            return out.toByteArray();

        } catch (Exception e) {
            return ("Error generating export: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }
}