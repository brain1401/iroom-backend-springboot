package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험지 문서 목록 조회 응답 DTO
 * 
 * 시험지 초안별 문서 목록 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDocumentListResponse {
    
    private Long examDraftId;       // 시험지 초안 ID
    private String examName;        // 시험지 이름
    private int grade;              // 학년
    private List<DocumentInfo> documents;  // 문서 목록
    private int totalCount;         // 총 개수
    
    /**
     * 문서 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentInfo {
        private Long documentId;        // 문서 ID
        private String documentType;    // 문서 타입 (ANSWER_SHEET, QUESTION_PAPER, ANSWER_KEY)
        private String documentTypeName; // 문서 타입 한글명 (답안지, 문제지, 답안)
        private String qrCodeUrl;       // QR 코드 URL (답안지만 해당)
    }
}
