package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    long countBySite(SiteEntity site);

    Optional<Lemma> findByLemmaAndSite(String lemma, SiteEntity site);

    List<Lemma> findByLemmaInAndSite(List<String> lemmas, SiteEntity site);
}