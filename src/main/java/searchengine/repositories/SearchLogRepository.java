package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.SearchLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Integer> {

    // Топ запросов по количеству
    @Query("SELECT s.query, COUNT(s) as cnt FROM SearchLog s GROUP BY s.query ORDER BY cnt DESC")
    List<Object[]> findTopQueries();

    // Топ сайтов по количеству запросов
    @Query("SELECT s.site, COUNT(s) as cnt FROM SearchLog s GROUP BY s.site ORDER BY cnt DESC")
    List<Object[]> countBySite();

    // Последние 10 логов
    List<SearchLog> findTop10ByOrderByTimestampDesc();

    // Фильтрация по дате
    List<SearchLog> findAllByTimestampBetween(LocalDateTime from, LocalDateTime to);
}