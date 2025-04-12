package searchengine.services;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class PageLoader {

    @SneakyThrows
    public String loadHtml(String url) {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; search-bot/1.0)")
                .referrer("https://www.google.com")
                .get();
        return doc.html();
    }
}