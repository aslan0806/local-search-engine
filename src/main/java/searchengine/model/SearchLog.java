package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_log")
@Getter
@Setter
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String query;
    private String site;
    private int offset;
    private int limit;
    private int results;

    @Column(name = "timestamp", columnDefinition = "DATETIME")
    private LocalDateTime timestamp;
}