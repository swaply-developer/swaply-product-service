-- [1단계] 대분류 (Depth 1) 등록
INSERT INTO category (parent_id, name, depth) VALUES
    (NULL, '여성의류', 1),
    (NULL, '남성의류', 1),
    (NULL, '신발', 1),
    (NULL, '디지털', 1);

-- [2단계] 중분류 (Depth 2) 등록을 위해 대분류 ID를 긴 변수명으로 추출
SET @category_id_female_clothing = (SELECT category_id FROM category WHERE name='여성의류' AND depth=1);
SET @category_id_male_clothing = (SELECT category_id FROM category WHERE name='남성의류' AND depth=1);
SET @category_id_shoes = (SELECT category_id FROM category WHERE name='신발' AND depth=1);
SET @category_id_digital = (SELECT category_id FROM category WHERE name='디지털' AND depth=1);

-- 2-1. 여성의류 중분류 등록
INSERT INTO category (parent_id, name, depth) VALUES
    (@category_id_female_clothing, '아우터', 2),
    (@category_id_female_clothing, '상의', 2),
    (@category_id_female_clothing, '하의', 2),
    (@category_id_female_clothing, '원피스', 2),
    (@category_id_female_clothing, '셋업/세트', 2),
    (@category_id_female_clothing, '언더웨어/홈웨어', 2);

-- 2-2. 남성의류 중분류 등록
INSERT INTO category (parent_id, name, depth) VALUES
    (@category_id_male_clothing, '아우터', 2),
    (@category_id_male_clothing, '상의', 2),
    (@category_id_male_clothing, '하의', 2),
    (@category_id_male_clothing, '정장', 2);

-- 2-3. 신발 중분류 등록
INSERT INTO category (parent_id, name, depth) VALUES
    (@category_id_shoes, '운동화/스니커즈', 2),
    (@category_id_shoes, '부츠', 2),
    (@category_id_shoes, '힐/펌프스', 2),
    (@category_id_shoes, '샌들/슬리퍼', 2);

-- 2-4. 디지털 중분류 등록
INSERT INTO category (parent_id, name, depth) VALUES
    (@category_id_digital, '스마트폰', 2),
    (@category_id_digital, '노트북/PC', 2),
    (@category_id_digital, '카메라', 2),
    (@category_id_digital, '음향기기', 2);

-- [3단계] 소분류 (Depth 3) 등록을 위해 중분류 ID를 긴 변수명으로 추출

-- 여성의류 하위 (2단계 카테고리들)
SET @female_category_outer = (SELECT category_id FROM category WHERE name='아우터' AND parent_id=@category_id_female_clothing);
SET @female_category_top = (SELECT category_id FROM category WHERE name='상의' AND parent_id=@category_id_female_clothing);
SET @female_category_bottom = (SELECT category_id FROM category WHERE name='하의' AND parent_id=@category_id_female_clothing);
SET @female_category_dress = (SELECT category_id FROM category WHERE name='원피스' AND parent_id=@category_id_female_clothing);
SET @female_category_setup = (SELECT category_id FROM category WHERE name='셋업/세트' AND parent_id=@category_id_female_clothing);
SET @female_category_innerwear = (SELECT category_id FROM category WHERE name='언더웨어/홈웨어' AND parent_id=@category_id_female_clothing);

INSERT INTO category (parent_id, name, depth) VALUES
    (@female_category_outer, '패딩', 3), (@female_category_outer, '점퍼', 3), (@female_category_outer, '코트', 3), (@female_category_outer, '자켓', 3), (@female_category_outer, '가디건', 3), (@female_category_outer, '조끼/베스트', 3),
    (@female_category_top, '티셔츠', 3), (@female_category_top, '니트/스웨터', 3), (@female_category_top, '블라우스', 3), (@female_category_top, '셔츠', 3),
    (@female_category_bottom, '데님팬츠', 3), (@female_category_bottom, '슬랙스', 3), (@female_category_bottom, '스커트', 3), (@female_category_bottom, '트레이닝', 3),
    (@female_category_dress, '미니', 3), (@female_category_dress, '미디', 3), (@female_category_dress, '롱', 3),
    (@female_category_setup, '투피스', 3), (@female_category_setup, '정장세트', 3),
    (@female_category_innerwear, '브라', 3), (@female_category_innerwear, '팬티', 3), (@female_category_innerwear, '홈웨어', 3);

-- 남성의류 하위 (2단계 카테고리들)
SET @male_category_outer = (SELECT category_id FROM category WHERE name='아우터' AND parent_id=@category_id_male_clothing);
SET @male_category_top = (SELECT category_id FROM category WHERE name='상의' AND parent_id=@category_id_male_clothing);
SET @male_category_bottom = (SELECT category_id FROM category WHERE name='하의' AND parent_id=@category_id_male_clothing);
SET @male_category_suit = (SELECT category_id FROM category WHERE name='정장' AND parent_id=@category_id_male_clothing);

INSERT INTO category (parent_id, name, depth) VALUES
    (@male_category_outer, '패딩', 3), (@male_category_outer, '점퍼', 3), (@male_category_outer, '코트', 3), (@male_category_outer, '자켓', 3),
    (@male_category_top, '티셔츠', 3), (@male_category_top, '맨투맨', 3), (@male_category_top, '셔츠', 3), (@male_category_top, '후드', 3),
    (@male_category_bottom, '데님', 3), (@male_category_bottom, '슬랙스', 3), (@male_category_bottom, '반바지', 3),
    (@male_category_suit, '수트', 3), (@male_category_suit, '자켓', 3), (@male_category_suit, '바지', 3);

-- 신발 하위 (2단계 카테고리들)
SET @shoes_category_sneakers = (SELECT category_id FROM category WHERE name='운동화/스니커즈' AND parent_id=@category_id_shoes);
SET @shoes_category_boots = (SELECT category_id FROM category WHERE name='부츠' AND parent_id=@category_id_shoes);
SET @shoes_category_heels = (SELECT category_id FROM category WHERE name='힐/펌프스' AND parent_id=@category_id_shoes);
SET @shoes_category_sandals = (SELECT category_id FROM category WHERE name='샌들/슬리퍼' AND parent_id=@category_id_shoes);

INSERT INTO category (parent_id, name, depth) VALUES
    (@shoes_category_sneakers, '러닝화', 3), (@shoes_category_sneakers, '하이탑', 3), (@shoes_category_sneakers, '로우탑', 3),
    (@shoes_category_boots, '워커', 3), (@shoes_category_boots, '첼시', 3), (@shoes_category_boots, '롱부츠', 3),
    (@shoes_category_heels, '하이힐', 3), (@shoes_category_heels, '로우힐', 3),
    (@shoes_category_sandals, '샌들', 3), (@shoes_category_sandals, '슬리퍼', 3);

-- 디지털 하위 (2단계 카테고리들)
SET @digital_category_smartphone = (SELECT category_id FROM category WHERE name='스마트폰' AND parent_id=@category_id_digital);
SET @digital_category_pc = (SELECT category_id FROM category WHERE name='노트북/PC' AND parent_id=@category_id_digital);
SET @digital_category_camera = (SELECT category_id FROM category WHERE name='카메라' AND parent_id=@category_id_digital);
SET @digital_category_audio = (SELECT category_id FROM category WHERE name='음향기기' AND parent_id=@category_id_digital);

INSERT INTO category (parent_id, name, depth) VALUES
  (@digital_category_smartphone, '아이폰', 3), (@digital_category_smartphone, '갤럭시', 3), (@digital_category_smartphone, '기타', 3),
  (@digital_category_pc, '맥북', 3), (@digital_category_pc, '윈도우', 3), (@digital_category_pc, '부품', 3),
  (@digital_category_camera, 'DSLR', 3), (@digital_category_camera, '미러리스', 3), (@digital_category_camera, '렌즈', 3),
  (@digital_category_audio, '헤드폰', 3), (@digital_category_audio, '이어폰', 3), (@digital_category_audio, '스피커', 3);