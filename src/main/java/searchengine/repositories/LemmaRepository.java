package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    // Подсчёт количества лемм для конкретного сайта
    long countBySite(SiteEntity site);

    // Поиск одной леммы по тексту и сайту
    Optional<Lemma> findByLemmaAndSite(String lemma, SiteEntity site);

    // Поиск всех лемм по списку значений и сайту
    List<Lemma> findAllByLemmaInAndSite(List<String> lemmas, SiteEntity site);
}