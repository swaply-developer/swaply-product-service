-- 관리자 상품 관리 기능을 위해 product.status ENUM 에 HIDDEN, DELETED 추가
ALTER TABLE product
    MODIFY COLUMN status ENUM('SALE', 'RESERVED', 'SOLD_OUT', 'HIDDEN', 'DELETED') DEFAULT 'SALE';
