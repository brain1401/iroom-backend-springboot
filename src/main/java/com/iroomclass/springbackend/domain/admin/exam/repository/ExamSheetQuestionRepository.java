package com.iroomclass.springbackend.domain.admin.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheetQuestion;

import java.util.List;
import java.util.UUID;

/**
 * 시험지 문제 Repository
 * 
 * 시험지에 포함된 문제 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public interface ExamSheetQuestionRepository extends JpaRepository<ExamSheetQuestion, UUID> {
    
    /**
     * 특정 시험지에 포함된 문제 목록 조회
     * 
     * 사용처: 문제 목록 화면에서 시험지에 포함된 문제들 표시
     * 예시: "1학년 중간고사" 시험지 → 1번부터 20번까지의 문제들 순서대로 표시
     * 
     * @param examSheetId 시험지 ID
     * @return 해당 시험지의 문제 목록 (순서대로)
     */
    List<ExamSheetQuestion> findByExamSheetIdOrderBySeqNo(UUID examSheetId);
    
    /**
     * 특정 시험지의 문제 개수 조회
     * 
     * 사용처: 시험지 목록에서 문항수 표시
     * 예시: "1학년 중간고사" → 총 20문제
     * 
     * @param examSheetId 시험지 ID
     * @return 해당 시험지의 문제 개수
     */
    long countByExamSheetId(UUID examSheetId);
    
    /**
     * 특정 시험지의 특정 문제 조회
     * 
     * 사용처: 문제 교체 시 특정 문제 번호의 문제 조회
     * 예시: 1학년 중간고사 5번 문제 교체 → 5번 문제 정보 조회
     * 
     * @param examSheetId 시험지 ID
     * @param seqNo 문제 번호
     * @return 해당 문제 정보
     */
    java.util.Optional<ExamSheetQuestion> findByExamSheetIdAndSeqNo(UUID examSheetId, int seqNo);

    /**
     * 특정 시험지의 특정 문제 조회 (문제 ID로)
     * 
     * 사용처: 학생 결과 조회 시 문제 번호와 배점 정보 조회
     * 예시: 학생 답안에서 문제 번호와 배점 표시
     * 
     * @param examSheetId 시험지 ID
     * @param questionId 문제 ID
     * @return 해당 문제 정보
     */
    java.util.Optional<ExamSheetQuestion> findByExamSheetIdAndQuestionId(UUID examSheetId, UUID questionId);
    
    /**
     * 특정 시험지의 문제 목록 조회 (문제 순서 기준 정렬)
     * 
     * 문제 직접 선택 시스템에서 questionOrder 필드를 기준으로 정렬된 문제 목록을 조회합니다.
     * seqNo와 questionOrder가 다를 수 있으므로 별도 메서드로 제공합니다.
     * 
     * @param examSheetId 시험지 ID
     * @return 해당 시험지의 문제 목록 (questionOrder 순서대로)
     */
    List<ExamSheetQuestion> findByExamSheetIdOrderByQuestionOrder(UUID examSheetId);
    
    /**
     * 특정 순서 이후의 문제들 조회 (포함)
     * 
     * 문제 추가 시 기존 문제들의 순서를 재조정할 때 사용됩니다.
     * 예: 3번 위치에 새 문제 추가 시, 기존 3번 이후 문제들을 4번부터 재배치
     * 
     * @param examSheetId 시험지 ID
     * @param questionOrder 기준 순서 (이 순서 이상인 문제들 조회)
     * @return 기준 순서 이후의 문제 목록
     */
    List<ExamSheetQuestion> findByExamSheetIdAndQuestionOrderGreaterThanEqual(UUID examSheetId, Integer questionOrder);
    
    /**
     * 특정 순서 이후의 문제들 조회 (초과)
     * 
     * 문제 제거 시 나머지 문제들의 순서를 재조정할 때 사용됩니다.
     * 예: 3번 문제 제거 시, 4번 이후 문제들을 3번부터 재배치
     * 
     * @param examSheetId 시험지 ID
     * @param questionOrder 기준 순서 (이 순서 초과인 문제들 조회)
     * @return 기준 순서 이후의 문제 목록
     */
    List<ExamSheetQuestion> findByExamSheetIdAndQuestionOrderGreaterThan(UUID examSheetId, Integer questionOrder);
}