-- ===============================================
-- ExamResult.totalScore 데이터 무결성 수정 스크립트
-- ===============================================
-- 문제: 모든 ExamResult.totalScore 값이 NULL
-- 해결: ExamResultQuestion의 score 합계로 재계산

USE campus_25SW_FS_p3_4;

-- 1. 현재 상태 확인
SELECT '=== 수정 전 상태 확인 ===' as status;
SELECT COUNT(*) as total_exam_results FROM exam_result;
SELECT COUNT(*) as null_total_score_count FROM exam_result WHERE total_score IS NULL;
SELECT COUNT(*) as non_null_total_score_count FROM exam_result WHERE total_score IS NOT NULL;

-- 2. 샘플 데이터 확인 (수정 전)
SELECT '=== 수정 전 샘플 데이터 ===' as status;
SELECT 
    er.id,
    er.total_score as current_total_score,
    er.version,
    es.student_id,
    e.grade
FROM exam_result er
JOIN exam_submission es ON er.submission_id = es.id
JOIN exam e ON es.exam_id = e.id
WHERE e.grade = 1
LIMIT 5;

-- 3. ExamResultQuestion 데이터 확인
SELECT '=== ExamResultQuestion 샘플 확인 ===' as status;
SELECT 
    erq.exam_result_id,
    COUNT(*) as question_count,
    SUM(erq.score) as calculated_total_score
FROM exam_result_question erq
WHERE erq.exam_result_id IN (
    SELECT er.id FROM exam_result er 
    JOIN exam_submission es ON er.submission_id = es.id
    JOIN exam e ON es.exam_id = e.id
    WHERE e.grade = 1
    LIMIT 3
)
GROUP BY erq.exam_result_id;

-- 4. 트랜잭션 시작
START TRANSACTION;

-- 5. 백업 테이블 생성 (안전장치)
SELECT '=== 백업 테이블 생성 ===' as status;
CREATE TABLE exam_result_backup_before_fix AS SELECT * FROM exam_result WHERE total_score IS NULL;
SELECT CONCAT('백업된 레코드 수: ', COUNT(*)) as backup_info FROM exam_result_backup_before_fix;

-- 6. totalScore 업데이트 (배치 방식)
SELECT '=== totalScore 업데이트 시작 ===' as status;

UPDATE exam_result er
SET total_score = (
    SELECT COALESCE(SUM(erq.score), 0)
    FROM exam_result_question erq
    WHERE erq.exam_result_id = er.id
)
WHERE er.total_score IS NULL;

-- 7. 결과 검증
SELECT '=== 수정 후 상태 확인 ===' as status;
SELECT COUNT(*) as remaining_null_count FROM exam_result WHERE total_score IS NULL;
SELECT COUNT(*) as fixed_count FROM exam_result WHERE total_score IS NOT NULL;

-- 8. 수정된 데이터 샘플 확인
SELECT '=== 수정 후 샘플 데이터 ===' as status;
SELECT 
    er.id,
    er.total_score as updated_total_score,
    er.version,
    es.student_id,
    e.grade
FROM exam_result er
JOIN exam_submission es ON er.submission_id = es.id
JOIN exam e ON es.exam_id = e.id
WHERE e.grade = 1 AND er.total_score IS NOT NULL
LIMIT 5;

-- 9. Grade 1 학생들의 평균 점수 확인 (수정 후)
SELECT '=== Grade 1 평균 점수 확인 (수정 후) ===' as status;
SELECT 
    s.id as student_id,
    s.name as student_name,
    AVG(er.total_score) as average_score
FROM exam_result er
JOIN exam_submission es ON er.submission_id = es.id
JOIN student s ON es.student_id = s.id
JOIN exam e ON es.exam_id = e.id
WHERE e.grade = 1
AND er.version = (
    SELECT MAX(er2.version) 
    FROM exam_result er2 
    WHERE er2.submission_id = er.submission_id
)
GROUP BY s.id, s.name
LIMIT 10;

-- 10. 커밋하기 전 확인 메시지
SELECT '=== 검증 완료 - 커밋 준비됨 ===' as status;
SELECT 'COMMIT; 명령어를 실행하여 변경사항을 저장하세요.' as next_action;

-- 수동으로 COMMIT을 실행해야 함
-- COMMIT;

-- 문제가 있다면 롤백
-- ROLLBACK;