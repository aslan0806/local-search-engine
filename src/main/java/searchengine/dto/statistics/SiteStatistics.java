package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteStatistics {
    private String url;
    private String name;
    private String status;
    private String statusTime;
    private String error;
    private int pages;
    private int lemmas;
}