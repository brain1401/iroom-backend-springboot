package com.iroomclass.springbackend.domain.admin.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDocument;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDraft;

import java.util.List;

/**
 * 시험지 문서 Repository
 * 
 * 시험지 문서(문제지, 답안지) 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public interface ExamDocumentRepository extends JpaRepository<ExamDocument, Long> {
    
    /**
     * 시험지 초안별 문서 조회
     * 
     * 사용처: 시험지 목록에서 "문제보기", "답안보기" 버튼 클릭 시
     * 예시: "1학년 중간고사" → 문제지와 답안지 문서 조회
     * 
     * @param examDraft 시험지 초안
     * @return 해당 시험지의 문서 목록 (문제지, 답안지)
     */
    List<ExamDocument> findByExamDraft(ExamDraft examDraft);

    /**
     * 시험지 초안 ID와 문서 타입으로 조회
     * 
     * 사용처: 특정 문서만 조회할 때 (문제지만 또는 답안지만)
     * 예시: "1학년 중간고사" + "문제지" → 문제지 문서만 조회
     * 
     * @param examDraftId 시험지 초안 ID
     * @param documentType 문서 타입 (문제지, 답안지)
     * @return 해당 조건의 문서
     */
    ExamDocument findByExamDraftIdAndDocumentType(Long examDraftId, ExamDocument.DocumentType documentType);
    
    /**
     * 시험지 초안 ID로 모든 문서 조회
     * 
     * 사용처: 시험지 초안의 모든 문서 목록 조회
     * 예시: "1학년 중간고사" → 답안지, 문제지, 답안 모두 조회
     * 
     * @param examDraftId 시험지 초안 ID
     * @return 해당 시험지 초안의 모든 문서 목록
     */
    List<ExamDocument> findByExamDraftId(Long examDraftId);
}
