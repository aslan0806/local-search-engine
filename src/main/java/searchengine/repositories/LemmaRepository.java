package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    Optional<Lemma> findByLemmaAndSite(String lemma, SiteEntity site);

    int countBySite(SiteEntity site);

    // 👇 Добавь этот метод:
    List<Lemma> findAllByLemmaInAndSite(List<String> lemmas, SiteEntity site);
}