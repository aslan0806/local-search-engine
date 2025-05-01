package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Integer> {

    long countBySite(SiteEntity site);

    Optional<Page> findByPathAndSite(String path, SiteEntity site); // 👈 Вот ЭТО добавляем!

}