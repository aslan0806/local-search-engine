package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.SiteEntity;
import java.util.List;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    long countBySite(SiteEntity site);

    List<Lemma> findAllByLemmaInAndSite(List<String> lemmas, SiteEntity site); // 👈 ВАЖНО: ДОБАВЛЯЕМ!

}