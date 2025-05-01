package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "search_log")
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "`limit`", nullable = false) // экранируем слово limit через кавычки
    private int limit;

    @Column(nullable = false)
    private int offset;

    private String query;

    @Column(nullable = false)
    private int results;

    private String site;

    private LocalDateTime timestamp;
}