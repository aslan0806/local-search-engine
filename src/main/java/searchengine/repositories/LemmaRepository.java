package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.SiteEntity;
import java.util.Optional;
@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    long countBySite(SiteEntity site);


    // 🔍 Этот метод нужен для поиска леммы по тексту и сайту
    Optional<Lemma> findByLemmaAndSite(String lemma, SiteEntity site);
}