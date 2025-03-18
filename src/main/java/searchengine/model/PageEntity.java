package searchengine.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "page", indexes = {
        @Index(name = "idx_path", columnList = "path")
})
public class PageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;

    // Изменено: используем VARCHAR(255) вместо TEXT, чтобы можно было создать индекс
    @Column(nullable = false, length = 255)
    private String path;

    @Column(nullable = false)
    private int code;

    // Поле content оставляем, как есть, если требуется хранить больше данных
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;
}