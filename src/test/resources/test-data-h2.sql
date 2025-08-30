-- H2-compatible test data for Question entity testing
-- This file provides minimal test data focused on validating the Question schema changes

-- 1. UnitCategory 데이터 (단원 대분류)
INSERT INTO unit_category (id, category_name, display_order, description) VALUES 
('123e4567-e89b-12d3-a456-426614174001', '수와 연산', 1, '수와 연산 영역'),
('123e4567-e89b-12d3-a456-426614174002', '문자와 식', 2, '문자와 식 영역');

-- 2. UnitSubcategory 데이터 (단원 중분류)
INSERT INTO unit_subcategory (id, category_id, subcategory_name, display_order, description) VALUES 
('223e4567-e89b-12d3-a456-426614174001', '123e4567-e89b-12d3-a456-426614174001', '정수와 유리수', 1, '정수와 유리수 단원'),
('223e4567-e89b-12d3-a456-426614174002', '123e4567-e89b-12d3-a456-426614174001', '자연수', 2, '자연수 단원');

-- 3. Unit 데이터 (세부단원)
INSERT INTO unit (id, subcategory_id, grade, unit_name, unit_code, description, display_order) VALUES 
('323e4567-e89b-12d3-a456-426614174001', '223e4567-e89b-12d3-a456-426614174001', 1, '자연수의 덧셈과 뺄셈', 'MS1_NUM_ADD_SUB', '1학년 자연수 덧셈뺄셈', 1),
('323e4567-e89b-12d3-a456-426614174002', '223e4567-e89b-12d3-a456-426614174001', 1, '자연수의 곱셈과 나눗셈', 'MS1_NUM_MUL_DIV', '1학년 자연수 곱셈나눗셈', 2),
('323e4567-e89b-12d3-a456-426614174003', '223e4567-e89b-12d3-a456-426614174002', 2, '자연수의 성질', 'MS1_NAT_PROP', '2학년 자연수의 성질', 1);

-- 4. Question 데이터 - NEW SCHEMA with answer_text and scoring_rubric
INSERT INTO question (id, unit_id, question_type, difficulty, question_text, answer_text, scoring_rubric) VALUES 
('523e4567-e89b-12d3-a456-426614174001', '323e4567-e89b-12d3-a456-426614174001', 'MULTIPLE_CHOICE', 'EASY', '3 + 5 = ?', '8', NULL),
('523e4567-e89b-12d3-a456-426614174002', '323e4567-e89b-12d3-a456-426614174001', 'MULTIPLE_CHOICE', 'MEDIUM', '12 - 7 = ?', '5', NULL),
('523e4567-e89b-12d3-a456-426614174003', '323e4567-e89b-12d3-a456-426614174002', 'SUBJECTIVE', 'HARD', '24 ÷ 6을 계산하고 과정을 설명하시오', '4', '정확한 나눗셈 계산과 과정 설명 필요'),
('523e4567-e89b-12d3-a456-426614174004', '323e4567-e89b-12d3-a456-426614174002', 'SUBJECTIVE', 'MEDIUM', '15 × 3의 계산 과정을 써보시오', '45', '곱셈 과정을 단계별로 설명'),
('523e4567-e89b-12d3-a456-426614174005', '323e4567-e89b-12d3-a456-426614174003', 'TRUE_FALSE', 'EASY', '모든 자연수는 양수이다', 'true', NULL);

-- 5. Users 데이터 (테스트용)
INSERT INTO users (id, username, full_name, grade) VALUES 
('423e4567-e89b-12d3-a456-426614174001', 'student1', '김철수', 1),
('423e4567-e89b-12d3-a456-426614174002', 'student2', '이영희', 2);

-- 6. Admin 데이터 (테스트용)  
INSERT INTO admin (id, username, full_name, email) VALUES
('623e4567-e89b-12d3-a456-426614174001', 'admin1', '관리자1', 'admin1@test.com');