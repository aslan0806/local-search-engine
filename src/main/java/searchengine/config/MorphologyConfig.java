package searchengine.config;

import org.apache.lucene.morphology.russian.RussianMorphology;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MorphologyConfig {

    @Bean
    public RussianMorphology russianMorphology() throws IOException {
        return new RussianMorphology();
    }
}