package com.iroomclass.springbackend.domain.exam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 시험 수정 요청 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamUpdateRequest {
    
    /**
     * 시험명
     */
    private String examName;
    
    /**
     * 시험 관련 메모/설명
     */
    private String content;
    
    /**
     * 학생 수
     */
    private Integer studentCount;
}
