package com.iroomclass.springbackend.domain.exam.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 시험 상세 조회 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDetailResponse {
    
    /**
     * 시험 ID
     */
    private Long examId;
    
    /**
     * 시험지 초안 ID
     */
    private Long examDraftId;
    
    /**
     * 시험명
     */
    private String examName;
    
    /**
     * 학년
     */
    private Integer grade;
    
    /**
     * 시험 관련 메모/설명
     */
    private String content;
    
    /**
     * 학생 수
     */
    private Integer studentCount;
    
    /**
     * QR 코드 URL
     */
    private String qrCodeUrl;
    
    /**
     * 등록일시
     */
    private LocalDateTime createdAt;
}
