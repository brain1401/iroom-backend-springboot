package com.iroomclass.springbackend.domain.admin.exam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 시험 등록 요청 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamCreateRequest {
    
    /**
     * 시험지 초안 ID
     */
    private Long examDraftId;
    
    /**
     * 시험 관련 메모/설명
     */
    private String content;
    
    /**
     * 학생 수
     */
    private Integer studentCount;
}
