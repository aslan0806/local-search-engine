package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repositories.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    private volatile boolean indexingRunning = false;

    @Override
    public Map<String, Object> startIndexing() {
        // уже реализовано ранее
        return Map.of("result", true);
    }

    @Override
    public Map<String, Object> stopIndexing() {
        // уже реализовано ранее
        return Map.of("result", true);
    }

    @Override
    public Map<String, Object> indexPage(String url) {
        if (url == null || url.isBlank()) {
            return Map.of("result", false, "error", "URL не передан");
        }

        // Проверяем, принадлежит ли URL сайту из конфигурации
        Site configSite = sitesList.getSites().stream()
                .filter(site -> url.startsWith(site.getUrl()))
                .findFirst()
                .orElse(null);

        if (configSite == null) {
            return Map.of("result", false,
                    "error", "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot")
                    .referrer("https://google.com")
                    .get();

            String path = url.replaceFirst(configSite.getUrl(), "");
            if (path.isEmpty()) path = "/";

            // Получаем или создаём сайт в базе
            searchengine.model.Site siteEntity = siteRepository.findByUrl(configSite.getUrl())
                    .orElseGet(() -> {
                        searchengine.model.Site site = new searchengine.model.Site();
                        site.setUrl(configSite.getUrl());
                        site.setName(configSite.getName());
                        site.setStatus(SiteStatus.INDEXED);
                        site.setStatusTime(LocalDateTime.now());
                        return siteRepository.save(site);
                    });

            // Удаляем предыдущую версию страницы
            pageRepository.deleteByPathAndSite(path, siteEntity);

            // Сохраняем страницу
            Page page = new Page();
            page.setSite(siteEntity);
            page.setPath(path);
            page.setCode(doc.connection().response().statusCode());
            page.setContent(doc.html());
            pageRepository.save(page);

            // Обработка лемм
            String text = doc.body().text();
            Map<String, Integer> lemmaMap = lemmaService.getLemmaMap(text);

            for (var entry : lemmaMap.entrySet()) {
                String lemmaText = entry.getKey();
                int count = entry.getValue();

                Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaText, siteEntity)
                        .orElseGet(() -> {
                            Lemma l = new Lemma();
                            l.setSite(siteEntity);
                            l.setLemma(lemmaText);
                            l.setFrequency(0);
                            return l;
                        });

                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaRepository.save(lemma);

                Index index = new Index();
                index.setPage(page);
                index.setLemma(lemma);
                index.setRank(count);
                indexRepository.save(index);
            }

            return Map.of("result", true);

        } catch (IOException e) {
            return Map.of("result", false, "error", "Ошибка при загрузке страницы: " + e.getMessage());
        }
    }
}