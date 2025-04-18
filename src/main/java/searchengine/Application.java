// 📄 Application.java
package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // 💡 Включает поддержку @Async
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}