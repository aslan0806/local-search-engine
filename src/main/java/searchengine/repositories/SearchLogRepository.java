package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SearchLog;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Integer> {
}