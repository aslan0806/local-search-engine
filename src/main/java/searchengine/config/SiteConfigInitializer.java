package searchengine.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SiteConfigInitializer implements CommandLineRunner {

    private final SitesList sitesList;
    private final SiteRepository siteRepository;

    @Override
    public void run(String... args) {
        for (SiteConfig siteConfig : sitesList.getSites()) {
            boolean exists = siteRepository.existsByUrl(siteConfig.getUrl());
            if (!exists) {
                SiteEntity site = new SiteEntity();
                site.setName(siteConfig.getName());
                site.setUrl(siteConfig.getUrl());
                site.setStatus(StatusType.INDEXING);
                site.setStatusTime(LocalDateTime.now());
                site.setLastError(null);
                siteRepository.save(site);
                System.out.println("✅ Сайт добавлен в базу: " + site.getUrl());
            } else {
                System.out.println("ℹ️ Сайт уже есть в базе: " + siteConfig.getUrl());
            }
        }
    }
}