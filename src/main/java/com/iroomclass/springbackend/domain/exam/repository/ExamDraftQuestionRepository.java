package com.iroomclass.springbackend.domain.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iroomclass.springbackend.domain.exam.entity.ExamDraftQuestion;
import java.util.List;

/**
 * 시험지 초안 문제 Repository
 * 
 * 시험지 초안에 포함된 문제 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public interface ExamDraftQuestionRepository extends JpaRepository<ExamDraftQuestion, Long> {
    
    /**
     * 특정 시험지 초안에 포함된 문제 목록 조회
     * 
     * 사용처: 문제 목록 화면에서 시험지에 포함된 문제들 표시
     * 예시: "1학년 중간고사" 시험지 → 1번부터 20번까지의 문제들 순서대로 표시
     * 
     * @param examDraftId 시험지 초안 ID
     * @return 해당 시험지 초안의 문제 목록 (순서대로)
     */
    List<ExamDraftQuestion> findByExamDraftIdOrderBySeqNo(Long examDraftId);
    
    /**
     * 특정 시험지 초안의 문제 개수 조회
     * 
     * 사용처: 시험지 목록에서 문항수 표시
     * 예시: "1학년 중간고사" → 총 20문제
     * 
     * @param examDraftId 시험지 초안 ID
     * @return 해당 시험지 초안의 문제 개수
     */
    long countByExamDraftId(Long examDraftId);
}
