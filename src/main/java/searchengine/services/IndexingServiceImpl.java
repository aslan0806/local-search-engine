package searchengine.services;

import org.springframework.stereotype.Service;

@Service
public class IndexingServiceImpl implements IndexingService {

    @Override
    public boolean startIndexing() {
        // 1. Получить список сайтов из конфигурации
        // 2. Для каждого сайта: удалить старые данные, создать новую запись со статусом INDEXING,
        //    запустить многопоточный обход с использованием JSoup и ForkJoinPool.
        // 3. По завершении обновить статус на INDEXED или FAILED при возникновении ошибок.
        return true;
    }

    @Override
    public boolean stopIndexing() {
        // Остановить все запущенные потоки обхода и обновить статусы.
        return true;
    }

    @Override
    public boolean indexPage(String url) {
        // Если URL принадлежит одному из сайтов из конфигурации:
        //    - Если страница уже есть – удалить старые данные
        //    - Скачать страницу, сохранить её в БД
        //    - Провести лемматизацию текста и обновить таблицы lemma и index
        // Иначе вернуть ошибку.
        return true;
    }
}