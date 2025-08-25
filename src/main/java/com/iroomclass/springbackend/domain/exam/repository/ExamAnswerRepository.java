package com.iroomclass.springbackend.domain.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.iroomclass.springbackend.domain.exam.entity.ExamAnswer;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;

/**
 * 시험 답안 Repository
 * 
 * 시험 답안 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {
    
    /**
     * 시험 제출별 답안 목록 조회
     * 
     * 사용처: 답안 보기에서 학생의 모든 문제 답안 조회
     * 예시: "김철수"의 "1학년 중간고사" → 1번부터 20번까지의 답안들 조회
     * 
     * @param submission 시험 제출
     * @return 해당 제출의 답안 목록
     */
    List<ExamAnswer> findBySubmission(ExamSubmission submission);

    /**
     * 시험 제출 ID로 답안 목록 조회
     * 
     * 사용처: 답안 보기에서 학생의 모든 문제 답안 조회 (ID로 조회)
     * 예시: 제출 ID로 해당 학생의 모든 답안 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 해당 제출의 답안 목록
     */
    List<ExamAnswer> findBySubmissionId(Long submissionId);

    /**
     * 정답 여부별 답안 조회
     * 
     * 사용처: 정답/오답 통계, 성적 분석
     * 예시: "김철수"의 정답들만 조회, 오답들만 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param isCorrect 정답 여부 (true: 정답, false: 오답)
     * @return 해당 조건의 답안 목록
     */
    List<ExamAnswer> findBySubmissionIdAndIsCorrect(Long submissionId, Boolean isCorrect);

    /**
     * 단원별 답안 조회
     * 
     * 사용처: 단원별 성적 분석
     * 예시: "정수의 덧셈" 단원에서 "김철수"가 맞춘 문제들 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param unitName 단원명
     * @return 해당 단원의 답안 목록
     */
    List<ExamAnswer> findBySubmissionIdAndUnitName(Long submissionId, String unitName);

    /**
     * 난이도별 답안 조회
     * 
     * 사용처: 난이도별 성적 분석
     * 예시: "중" 난이도에서 "김철수"가 맞춘 문제들 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param difficulty 난이도 (하, 중, 상)
     * @return 해당 난이도의 답안 목록
     */
    List<ExamAnswer> findBySubmissionIdAndDifficulty(Long submissionId, ExamAnswer.Difficulty difficulty);
}
