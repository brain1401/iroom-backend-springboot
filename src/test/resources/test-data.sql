-- 테스트용 기본 데이터 설정

-- 1. UnitCategory 데이터 (단원 대분류)
INSERT INTO unit_category (id, category_name, display_order, description) VALUES 
(1, '수와 연산', 1, '수와 연산 영역'),
(2, '문자와 식', 2, '문자와 식 영역'),
(3, '기하', 3, '기하 영역'),
(4, '통계와 확률', 4, '통계와 확률 영역');

-- 2. UnitSubcategory 데이터 (단원 중분류)
INSERT INTO unit_subcategory (id, category_id, subcategory_name, display_order, description) VALUES 
(1, 1, '정수와 유리수', 1, '정수와 유리수 단원'),
(2, 1, '자연수', 2, '자연수 단원'),
(3, 2, '문자의 사용과 식의 계산', 1, '문자의 사용과 식의 계산'),
(4, 3, '평면도형', 1, '평면도형과 입체도형'),
(5, 4, '통계', 1, '자료의 정리와 해석');

-- 3. Unit 데이터 (세부단원)
INSERT INTO unit (id, subcategory_id, grade, unit_name, unit_code, description, display_order) VALUES 
(1, 1, 1, '자연수의 덧셈과 뺄셈', 'MS1_NUM_ADD_SUB', '1학년 자연수 덧셈뺄셈', 1),
(2, 1, 1, '자연수의 곱셈과 나눗셈', 'MS1_NUM_MUL_DIV', '1학년 자연수 곱셈나눗셈', 2),
(3, 2, 1, '자연수의 성질', 'MS1_NAT_PROP', '1학년 자연수의 성질', 1),
(4, 3, 2, '문자와 식', 'MS2_ALG_EXPR', '2학년 문자와 식', 1),
(5, 4, 2, '삼각형과 사각형', 'MS2_GEO_TRI_QUAD', '2학년 도형', 1),
(6, 5, 3, '대푯값과 산포도', 'MS3_STAT_MEASURE', '3학년 통계', 1);

-- 4. Question 데이터 (문제)
INSERT INTO question (id, unit_id, question_type, difficulty, question_text, answer_key) VALUES 
(1, 1, 'MULTIPLE_CHOICE', 'EASY', '3 + 5 = ?', '8'),
(2, 1, 'MULTIPLE_CHOICE', 'EASY', '7 - 2 = ?', '5'),
(3, 1, 'SUBJECTIVE', 'MEDIUM', '15 + 27을 계산하세요.', '42'),
(4, 2, 'MULTIPLE_CHOICE', 'EASY', '4 × 3 = ?', '12'),
(5, 2, 'MULTIPLE_CHOICE', 'MEDIUM', '24 ÷ 6 = ?', '4'),
(6, 2, 'SUBJECTIVE', 'MEDIUM', '36 ÷ 4를 계산하세요.', '9'),
(7, 3, 'MULTIPLE_CHOICE', 'EASY', '삼각형의 내각의 합은?', '180도'),
(8, 3, 'SUBJECTIVE', 'HARD', '직각삼각형의 특징을 설명하세요.', '한 각이 90도인 삼각형'),
(9, 4, 'MULTIPLE_CHOICE', 'MEDIUM', '주어는 문장의 어떤 성분인가?', '주체'),
(10, 5, 'SUBJECTIVE', 'HARD', '이 글의 주제를 찾으세요.', '환경보호의 중요성'),
(11, 1, 'MULTIPLE_CHOICE', 'EASY', '10 + 15 = ?', '25'),
(12, 1, 'MULTIPLE_CHOICE', 'EASY', '20 - 8 = ?', '12'),
(13, 1, 'SUBJECTIVE', 'MEDIUM', '45 + 37을 계산하세요.', '82'),
(14, 2, 'MULTIPLE_CHOICE', 'EASY', '6 × 7 = ?', '42'),
(15, 2, 'MULTIPLE_CHOICE', 'MEDIUM', '48 ÷ 8 = ?', '6'),
(16, 2, 'SUBJECTIVE', 'MEDIUM', '63 ÷ 7을 계산하세요.', '9'),
(17, 1, 'MULTIPLE_CHOICE', 'EASY', '100 - 45 = ?', '55'),
(18, 1, 'SUBJECTIVE', 'EASY', '123 + 456을 계산하세요.', '579'),
(19, 2, 'MULTIPLE_CHOICE', 'MEDIUM', '9 × 8 = ?', '72'),
(20, 2, 'SUBJECTIVE', 'HARD', '144 ÷ 12를 계산하고 과정을 설명하세요.', '12');

-- 5. Test Users 데이터 (시험 제출을 위한 사용자)
INSERT INTO users (id, name, phone, birth_date, grade) VALUES 
(1, '홍길동', '010-1234-5678', '2010-03-15', 1),
(2, '김영희', '010-2345-6789', '2009-07-22', 2),
(3, '이철수', '010-3456-7890', '2008-11-30', 3);