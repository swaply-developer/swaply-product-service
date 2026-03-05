-- ==========================================
-- 2. 카테고리 및 상품 서비스 (Product & Brand)
-- ==========================================

CREATE TABLE category (
                          category_id INT AUTO_INCREMENT PRIMARY KEY,
                          parent_id INT NULL,
                          name VARCHAR(50) NOT NULL,
                          depth INT NOT NULL,
                          FOREIGN KEY (parent_id) REFERENCES category(category_id)
);

CREATE TABLE brand (
                       brand_id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       name_en VARCHAR(100),
                       category_id INT NOT NULL,
                       logo_url VARCHAR(255),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (category_id) REFERENCES category(category_id)
);

CREATE TABLE brand_follow (
                              follow_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              member_id BIGINT NOT NULL,
                              brand_id INT NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (member_id) REFERENCES member(member_id),
                              FOREIGN KEY (brand_id) REFERENCES brand(brand_id)
);

CREATE TABLE product (
                         product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         seller_id BIGINT NOT NULL,
                         category_id INT NOT NULL,
                         brand_id INT,
                         title VARCHAR(255) NOT NULL,
                         trade_type VARCHAR(255) NOT NULL,
                         description TEXT NOT NULL,
                         price DECIMAL(18, 0) NOT NULL,
                         status ENUM('SALE', 'RESERVED', 'SOLD_OUT') DEFAULT 'SALE',
                         view_count INT DEFAULT 0,
                         wish_count INT DEFAULT 0,
                         refreshed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (seller_id) REFERENCES member(member_id),
                         FOREIGN KEY (category_id) REFERENCES category(category_id),
                         FOREIGN KEY (brand_id) REFERENCES brand(brand_id)
);

CREATE TABLE product_image (
                               image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               product_id BIGINT NOT NULL,
                               image_url VARCHAR(255) NOT NULL,
                               is_thumbnail BOOLEAN DEFAULT FALSE,
                               sort_order INT DEFAULT 0,
                               FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE wish (
                      wish_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      member_id BIGINT NOT NULL,
                      product_id BIGINT NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      FOREIGN KEY (member_id) REFERENCES member(member_id),
                      FOREIGN KEY (product_id) REFERENCES product(product_id)
);