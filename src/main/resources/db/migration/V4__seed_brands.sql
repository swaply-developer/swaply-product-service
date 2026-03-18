-- ============================================================
-- V4: 기본 브랜드 데이터 시드
-- ProductCreate.jsx 의 BRANDS_BY_CATEGORY 와 동일한 목록
-- ============================================================

-- 카테고리 ID 변수 추출 (depth=1 대분류 기준)
SET @cat_women   = (SELECT category_id FROM category WHERE name = '여성의류' AND depth = 1);
SET @cat_men     = (SELECT category_id FROM category WHERE name = '남성의류' AND depth = 1);
SET @cat_shoes   = (SELECT category_id FROM category WHERE name = '신발'     AND depth = 1);
SET @cat_digital = (SELECT category_id FROM category WHERE name = '디지털'   AND depth = 1);

-- 여성의류 브랜드
INSERT INTO brand (name, name_en, logo_url, category_id) VALUES
                                                             ('유니클로', 'UNIQLO',       NULL, @cat_women),
                                                             ('자라',     'ZARA',         NULL, @cat_women),
                                                             ('에이치앤엠', 'H&M',        NULL, @cat_women),
                                                             ('코스',     'COS',          NULL, @cat_women),
                                                             ('망고',     'Mango',        NULL, @cat_women),
                                                             ('아더에러', 'Ader Error',   NULL, @cat_women),
                                                             ('마할로',   'MAHALO',       NULL, @cat_women);

-- 남성의류 브랜드
INSERT INTO brand (name, name_en, logo_url, category_id) VALUES
                                                             ('슈프림',       'Supreme',       NULL, @cat_men),
                                                             ('스톤아일랜드', 'Stone Island',  NULL, @cat_men),
                                                             ('칼하트',       'Carhartt',      NULL, @cat_men),
                                                             ('폴로',         'Polo',          NULL, @cat_men),
                                                             ('엠엘비',       'MLB',           NULL, @cat_men);

-- 신발 브랜드
INSERT INTO brand (name, name_en, logo_url, category_id) VALUES
                                                             ('나이키',       'Nike',          NULL, @cat_shoes),
                                                             ('아디다스',     'Adidas',        NULL, @cat_shoes),
                                                             ('뉴발란스',     'New Balance',   NULL, @cat_shoes),
                                                             ('반스',         'Vans',          NULL, @cat_shoes),
                                                             ('컨버스',       'Converse',      NULL, @cat_shoes),
                                                             ('살로몬',       'Salomon',       NULL, @cat_shoes),
                                                             ('크록스',       'Crocs',         NULL, @cat_shoes);

-- 디지털 브랜드
INSERT INTO brand (name, name_en, logo_url, category_id) VALUES
                                                             ('애플',   'Apple',   NULL, @cat_digital),
                                                             ('삼성',   'Samsung', NULL, @cat_digital),
                                                             ('소니',   'Sony',    NULL, @cat_digital),
                                                             ('엘지',   'LG',      NULL, @cat_digital),
                                                             ('보스',   'Bose',    NULL, @cat_digital),
                                                             ('다이슨', 'Dyson',   NULL, @cat_digital),
                                                             ('제이비엘', 'JBL',   NULL, @cat_digital);
