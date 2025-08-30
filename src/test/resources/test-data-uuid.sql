-- 테스트용 기본 데이터 설정 (UUID 버전)

-- UUID 값 정의 (일관성을 위해 미리 정의)
-- UnitCategory UUIDs
-- category1: 123e4567-e89b-12d3-a456-426614174001
-- category2: 123e4567-e89b-12d3-a456-426614174002
-- category3: 123e4567-e89b-12d3-a456-426614174003
-- category4: 123e4567-e89b-12d3-a456-426614174004

-- UnitSubcategory UUIDs
-- subcategory1: 223e4567-e89b-12d3-a456-426614174001
-- subcategory2: 223e4567-e89b-12d3-a456-426614174002
-- subcategory3: 223e4567-e89b-12d3-a456-426614174003
-- subcategory4: 223e4567-e89b-12d3-a456-426614174004
-- subcategory5: 223e4567-e89b-12d3-a456-426614174005

-- Unit UUIDs
-- unit1: 323e4567-e89b-12d3-a456-426614174001
-- unit2: 323e4567-e89b-12d3-a456-426614174002
-- unit3: 323e4567-e89b-12d3-a456-426614174003
-- unit4: 323e4567-e89b-12d3-a456-426614174004
-- unit5: 323e4567-e89b-12d3-a456-426614174005
-- unit6: 323e4567-e89b-12d3-a456-426614174006

-- User UUIDs
-- user1: 423e4567-e89b-12d3-a456-426614174001
-- user2: 423e4567-e89b-12d3-a456-426614174002
-- user3: 423e4567-e89b-12d3-a456-426614174003

-- 1. UnitCategory 데이터 (단원 대분류)
INSERT INTO unit_category (id, category_name, display_order, description) VALUES 
(UNHEX(REPLACE('123e4567-e89b-12d3-a456-426614174001', '-', '')), '수와 연산', 1, '수와 연산 영역'),
(UNHEX(REPLACE('123e4567-e89b-12d3-a456-426614174002', '-', '')), '문자와 식', 2, '문자와 식 영역'),
(UNHEX(REPLACE('123e4567-e89b-12d3-a456-426614174003', '-', '')), '기하', 3, '기하 영역'),
(UNHEX(REPLACE('123e4567-e89b-12d3-a456-426614174004', '-', '')), '통계와 확률', 4, '통계와 확률 영역');

-- 2. UnitSubcategory 데이터 (단원 중분류)
INSERT INTO unit_subcategory (id, category_id, subcategory_name, display_order, description) VALUES 
(UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174001', '-', '')), UNHEX(REPLACE('123e4567-e89b-12d3-a456-426614174001', '-', '')), '정수와 유리수', 1, '정수와 유리수 단원'),
(UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174002', '-', '')), UNHEX(REPLACE('123e4567-e89b-12d3-a456-426614174001', '-', '')), '자연수', 2, '자연수 단원'),
(UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174003', '-', '')), UNHEX(REPLACE('123e4567-e89b-12d3-a456-426614174002', '-', '')), '문자의 사용과 식의 계산', 1, '문자의 사용과 식의 계산'),
(UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174004', '-', '')), UNHEX(REPLACE('123e4567-e89b-12d3-a456-426614174003', '-', '')), '평면도형', 1, '평면도형과 입체도형'),
(UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174005', '-', '')), UNHEX(REPLACE('123e4567-e89b-12d3-a456-426614174004', '-', '')), '통계', 1, '자료의 정리와 해석');

-- 3. Unit 데이터 (세부단원)
INSERT INTO unit (id, subcategory_id, grade, unit_name, unit_code, description, display_order) VALUES 
(UNHEX(REPLACE('323e4567-e89b-12d3-a456-426614174001', '-', '')), UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174001', '-', '')), 1, '자연수의 덧셈과 뺄셈', 'MS1_NUM_ADD_SUB', '1학년 자연수 덧셈뺄셈', 1),
(UNHEX(REPLACE('323e4567-e89b-12d3-a456-426614174002', '-', '')), UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174001', '-', '')), 1, '자연수의 곱셈과 나눗셈', 'MS1_NUM_MUL_DIV', '1학년 자연수 곱셈나눗셈', 2),
(UNHEX(REPLACE('323e4567-e89b-12d3-a456-426614174003', '-', '')), UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174002', '-', '')), 1, '자연수의 성질', 'MS1_NAT_PROP', '1학년 자연수의 성질', 1),
(UNHEX(REPLACE('323e4567-e89b-12d3-a456-426614174004', '-', '')), UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174003', '-', '')), 2, '문자와 식', 'MS2_ALG_EXPR', '2학년 문자와 식', 1),
(UNHEX(REPLACE('323e4567-e89b-12d3-a456-426614174005', '-', '')), UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174004', '-', '')), 2, '삼각형과 사각형', 'MS2_GEO_TRI_QUAD', '2학년 도형', 1),
(UNHEX(REPLACE('323e4567-e89b-12d3-a456-426614174006', '-', '')), UNHEX(REPLACE('223e4567-e89b-12d3-a456-426614174005', '-', '')), 3, '대푯값과 산포도', 'MS3_STAT_MEASURE', '3학년 통계', 1);

-- 4. Question 데이터 (문제) - 일부만 포함하여 테스트 간소화
INSERT INTO question (id, unit_id, question_type, difficulty, question_text, answer_text, scoring_rubric) VALUES 
(UNHEX(REPLACE('$1', '-', '')), UNHEX(REPLACE('$2', '-', '')), '$3', '$4', '$5', '$6', NULL),
(UNHEX(REPLACE('$1', '-', '')), UNHEX(REPLACE('$2', '-', '')), '$3', '$4', '$5', '$6', NULL),
(UNHEX(REPLACE('$1', '-', '')), UNHEX(REPLACE('$2', '-', '')), '$3', '$4', '$5', '$6', NULL),
(UNHEX(REPLACE('$1', '-', '')), UNHEX(REPLACE('$2', '-', '')), '$3', '$4', '$5', '$6', NULL),
(UNHEX(REPLACE('523e4567-e89b-12d3-a456-426614174005', '-', '')), UNHEX(REPLACE('323e4567-e89b-12d3-a456-426614174002', '-', '')), 'MULTIPLE_CHOICE', 'MEDIUM', '24 ÷ 6 = ?', '4');

-- 5. Test Users 데이터 (시험 제출을 위한 사용자)
INSERT INTO users (id, name, phone, birth_date, grade) VALUES 
(UNHEX(REPLACE('423e4567-e89b-12d3-a456-426614174001', '-', '')), '홍길동', '010-1234-5678', '2010-03-15', 1),
(UNHEX(REPLACE('423e4567-e89b-12d3-a456-426614174002', '-', '')), '김영희', '010-2345-6789', '2009-07-22', 2),
(UNHEX(REPLACE('423e4567-e89b-12d3-a456-426614174003', '-', '')), '이철수', '010-3456-7890', '2008-11-30', 3);