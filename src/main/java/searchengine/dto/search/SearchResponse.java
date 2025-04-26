package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor // ✅ Теперь есть пустой конструктор для тестов
@Builder
public class SearchResponse {
    private boolean result;
    private int count;
    private List<SearchResultItem> data;

    public static SearchResponse success(List<SearchResultItem> data) {
        return SearchResponse.builder()
                .result(true)
                .count(data.size())
                .data(data)
                .build();
    }

    public static SearchResponse error() {
        return SearchResponse.builder()
                .result(false)
                .count(0)
                .data(List.of())
                .build();
    }
}