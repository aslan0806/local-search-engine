package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

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
    private final List<SiteIndexerTask> activeTasks = Collections.synchronizedList(new ArrayList<>());
    private ForkJoinPool forkJoinPool;

    @Override
    public Map<String, Object> startIndexing() {
        if (indexingRunning) {
            return Map.of("result", false, "error", "Индексация уже запущена");
        }

        indexingRunning = true;

        new Thread(() -> {
            for (Site configSite : sitesList.getSites()) {
                String url = configSite.getUrl();

                siteRepository.findAll().stream()
                        .filter(site -> site.getUrl().equals(url))
                        .forEach(site -> {
                            pageRepository.deleteAll(site.getPages());
                            siteRepository.delete(site);
                        });

                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setUrl(configSite.getUrl());
                siteEntity.setName(configSite.getName());
                siteEntity.setStatus(SiteStatus.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);

                SiteIndexerTask task = new SiteIndexerTask(siteEntity, siteRepository, pageRepository, url);
                activeTasks.add(task);

                forkJoinPool = new ForkJoinPool();
                forkJoinPool.invoke(task);
            }

            indexingRunning = false;
        }).start();

        return Map.of("result", true);
    }

    @Override
    public Map<String, Object> stopIndexing() {
        if (!indexingRunning) {
            return Map.of("result", false, "error", "Индексация не запущена");
        }

        indexingRunning = false;

        activeTasks.forEach(SiteIndexerTask::cancel);
        if (forkJoinPool != null) {
            forkJoinPool.shutdownNow();
        }

        siteRepository.findAll().forEach(site -> {
            if (site.getStatus() == SiteStatus.INDEXING) {
                site.setStatus(SiteStatus.FAILED);
                site.setLastError("Индексация остановлена пользователем");
                site.setStatusTime(LocalDateTime.now());
                siteRepository.save(site);
            }
        });

        return Map.of("result", true);
    }

    @Override
    public Map<String, Object> indexPage(String url) {
        Optional<Site> optional = sitesList.getSites().stream()
                .filter(site -> url.startsWith(site.getUrl()))
                .findFirst();

        if (optional.isEmpty()) {
            return Map.of("result", false, "error",
                    "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }

        Site configSite = optional.get();

        SiteEntity siteEntity = siteRepository.findByUrl(configSite.getUrl())
                .orElseGet(() -> {
                    SiteEntity newSite = new SiteEntity();
                    newSite.setUrl(configSite.getUrl());
                    newSite.setName(configSite.getName());
                    newSite.setStatus(SiteStatus.INDEXED);
                    newSite.setStatusTime(LocalDateTime.now());
                    return siteRepository.save(newSite);
                });

        try {
            Document doc = Jsoup.connect(url).get();

            // Удаляем старую страницу (если есть)
            Optional<Page> oldPage = pageRepository.findByPathAndSite(url, siteEntity);
            oldPage.ifPresent(pageRepository::delete);

            Page page = new Page();
            page.setSite(siteEntity);
            page.setPath(url);
            page.setCode(200);
            page.setContent(doc.html());
            Page savedPage = pageRepository.save(page);

            // Лемматизация
            Map<String, Integer> lemmas = lemmaService.lemmatize(doc.text());

            // Обновление таблиц lemma + index
            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                String lemmaText = entry.getKey();
                int count = entry.getValue();

                Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaText, siteEntity)
                        .orElseGet(() -> {
                            Lemma newLemma = new Lemma();
                            newLemma.setSite(siteEntity);
                            newLemma.setLemma(lemmaText);
                            newLemma.setFrequency(0);
                            return newLemma;
                        });

                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaRepository.save(lemma);

                Index index = new Index();
                index.setLemma(lemma);
                index.setPage(savedPage);
                index.setRank(count);
                indexRepository.save(index);
            }

            return Map.of("result", true);

        } catch (IOException e) {
            siteEntity.setStatus(SiteStatus.FAILED);
            siteEntity.setLastError("Ошибка подключения: " + e.getMessage());
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);

            return Map.of("result", false, "error", "Ошибка при индексации страницы: " + e.getMessage());
        }
    }
}