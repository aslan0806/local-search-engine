package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.SiteEntity;

public interface SiteRepository extends JpaRepository<SiteEntity, Long> {
    // При необходимости можно добавить методы поиска по URL, статусу и т.п.
}