package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.SearchLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Integer> {

    @Query("SELECT s.query, COUNT(s.query) FROM SearchLog s GROUP BY s.query ORDER BY COUNT(s.query) DESC")
    List<Object[]> findTopQueries();

    @Query("SELECT s.site, COUNT(s.site) FROM SearchLog s GROUP BY s.site ORDER BY COUNT(s.site) DESC")
    List<Object[]> countBySite();

    List<SearchLog> findTop10ByOrderByTimestampDesc();

    List<SearchLog> findAllByTimestampBetween(LocalDateTime from, LocalDateTime to); // 🔍 Фильтрация по дате
}