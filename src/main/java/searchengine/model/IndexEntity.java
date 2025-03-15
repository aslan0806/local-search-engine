package searchengine.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "index_table") // «index» может быть зарезервированным словом
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

    @Column(nullable = false)
    private float rank;
}