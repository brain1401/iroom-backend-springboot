package com.iroomclass.springbackend.domain.admin.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험지 문서 생성 응답 DTO
 * 
 * 시험지 문서 생성 완료 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDocumentCreateResponse {
    
    private Long examDraftId;       // 시험지 초안 ID
    private String examName;        // 시험지 이름
    private int grade;              // 학년
    private int totalQuestions;     // 총 문제 개수
    private int documentCount;      // 생성된 문서 개수
}
