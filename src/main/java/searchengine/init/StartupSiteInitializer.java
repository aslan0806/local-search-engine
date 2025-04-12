package searchengine.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class StartupSiteInitializer implements CommandLineRunner {

    private final SitesList sitesList;
    private final SiteRepository siteRepository;

    @Override
    public void run(String... args) {
        for (var site : sitesList.getSites()) {
            if (siteRepository.findByUrl(site.getUrl()) == null) {
                SiteEntity entity = new SiteEntity();
                entity.setUrl(site.getUrl());
                entity.setName(site.getName());
                entity.setStatus(StatusType.INDEXING);
                entity.setStatusTime(LocalDateTime.now());
                entity.setLastError(null);
                siteRepository.save(entity);
            }
        }
    }
}