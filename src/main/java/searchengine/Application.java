package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import searchengine.config.SitesList;

@SpringBootApplication
@EnableConfigurationProperties(SitesList.class) // 👈 вот эта строка — обязательна!
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}