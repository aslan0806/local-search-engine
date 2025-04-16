package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.SiteEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    long countBySite(SiteEntity site);

    List<Page> findAllBySite(SiteEntity site); // ✅ добавь этот метод
}