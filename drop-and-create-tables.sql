-- Отключаем проверки внешних ключей SET FOREIGN_KEY_CHECKS = 0; -- Удаляем старые таблицы DROP TABLE IF EXISTS page_index; DROP TABLE IF EXISTS lemma; DROP TABLE IF EXISTS page; DROP TABLE IF EXISTS 
site; -- Включаем обратно проверки внешних ключей SET FOREIGN_KEY_CHECKS = 1; -- Создаём таблицу site CREATE TABLE site (
    id INT AUTO_INCREMENT PRIMARY KEY, status ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL, status_time DATETIME NOT NULL, last_error TEXT, url VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL ); 
-- Создаём таблицу page CREATE TABLE page (
    id INT AUTO_INCREMENT PRIMARY KEY, path VARCHAR(255) NOT NULL, code INT NOT NULL, content TEXT NOT NULL, site_id INT NOT NULL, FOREIGN KEY (site_id) REFERENCES site(id) ); -- Создаём таблицу lemma 
CREATE TABLE lemma (
    id INT AUTO_INCREMENT PRIMARY KEY, lemma VARCHAR(255) NOT NULL, frequency INT NOT NULL, site_id INT NOT NULL, FOREIGN KEY (site_id) REFERENCES site(id) ); -- Создаём таблицу page_index CREATE 
TABLE page_index (
    id INT AUTO_INCREMENT PRIMARY KEY, page_id INT NOT NULL, lemma_id INT NOT NULL, rank FLOAT NOT NULL, FOREIGN KEY (page_id) REFERENCES page(id), FOREIGN KEY (lemma_id) REFERENCES lemma(id)
);
