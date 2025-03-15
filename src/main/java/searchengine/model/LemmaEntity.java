package searchengine.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "lemma")
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;

    @Column(nullable = false)
    private String lemma;

    @Column(nullable = false)
    private int frequency;
}