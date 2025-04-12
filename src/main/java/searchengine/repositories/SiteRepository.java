package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

    // 🔍 Поиск сайта по URL
    SiteEntity findByUrl(String url);

    // ✅ Проверка существования сайта по URL
    boolean existsByUrl(String url);
}