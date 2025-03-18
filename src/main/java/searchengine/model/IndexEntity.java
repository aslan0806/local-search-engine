package searchengine.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "index_table") // Используем имя, отличное от "index"
public class IndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity page;

    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false)
    private LemmaEntity lemma;

    // Переименовано, чтобы избежать конфликтов с зарезервированными словами
    @Column(name = "lemma_rank", nullable = false)
    private float lemmaRank;
}