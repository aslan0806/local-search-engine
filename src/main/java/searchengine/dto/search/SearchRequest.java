package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchRequest {
    private String query;     // 🔍 Поисковый запрос
    private String site;      // 🌐 URL сайта (может быть null — значит искать по всем)
    private int offset;       // 📍 Отступ от начала
    private int limit;        // 🔢 Кол-во результатов на странице
}