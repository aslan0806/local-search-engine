package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.SiteEntity;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    long countBySite(SiteEntity site);

    // 🔍 Исправлено: теперь возвращается Optional
    Optional<Page> findByPathAndSite(String path, SiteEntity site);
}