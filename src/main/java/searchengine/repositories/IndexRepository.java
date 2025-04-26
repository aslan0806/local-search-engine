package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Lemma;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
    @Query("SELECT i.page.id, SUM(i.rank) FROM Index i WHERE i.lemma IN :lemmas GROUP BY i.page.id")
    List<Object[]> findPageRelevance(List<Lemma> lemmas);
}