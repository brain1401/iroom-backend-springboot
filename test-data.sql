-- 테스트 계정 데이터 삽입 스크립트
-- 생성 시간: 2025-09-02

-- 1. 테스트용 관리자 계정
-- username: admin_test, password: admin123!
INSERT INTO user (
    id, username, password, refresh_token, name, email, phone, role, 
    grade, birth_date, academy_name, created_at, updated_at
) VALUES (
    UNHEX(REPLACE(UUID(), '-', '')), 
    'admin_test', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- 암호화된 "password" (BCrypt)
    NULL,
    '테스트 관리자', 
    'admin@test.com',
    NULL,
    'ADMIN',
    NULL,
    NULL,
    '테스트 학원',
    NOW(),
    NOW()
);

-- 2. 테스트용 학생 계정 (3-factor 인증용)
-- name: 김철수, phone: 010-1234-5678, birthDate: 2008-03-15
INSERT INTO user (
    id, username, password, refresh_token, name, email, phone, role,
    grade, birth_date, academy_name, created_at, updated_at
) VALUES (
    UNHEX(REPLACE(UUID(), '-', '')),
    NULL,
    NULL, 
    NULL,
    '김철수',
    NULL,
    '010-1234-5678',
    'STUDENT',
    2,
    '2008-03-15',
    NULL,
    NOW(),
    NOW()
);

-- 3. 추가 테스트용 학생 계정
-- name: 이영희, phone: 010-9876-5432, birthDate: 2009-07-22
INSERT INTO user (
    id, username, password, refresh_token, name, email, phone, role,
    grade, birth_date, academy_name, created_at, updated_at
) VALUES (
    UNHEX(REPLACE(UUID(), '-', '')),
    NULL,
    NULL,
    NULL,
    '이영희',
    NULL,
    '010-9876-5432', 
    'STUDENT',
    1,
    '2009-07-22',
    NULL,
    NOW(),
    NOW()
);

-- 데이터 확인 쿼리
SELECT 
    id, username, name, email, phone, role, grade, birth_date, academy_name,
    created_at, updated_at
FROM user 
ORDER BY created_at DESC;