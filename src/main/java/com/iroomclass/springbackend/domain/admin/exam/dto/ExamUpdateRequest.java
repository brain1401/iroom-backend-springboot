package com.iroomclass.springbackend.domain.admin.exam.dto;

import jakarta.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 수정 요청 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 수정 요청")
public record ExamUpdateRequest(
    @Schema(description = "시험명", example = "1학년 기말고사")
    String examName,
    
    @Schema(description = "시험 관련 메모/설명", example = "1학년 기말고사 - 자연수와 0 단원")
    String content,
    
    @Min(value = 1, message = "학생 수는 1명 이상이어야 합니다.")
    @Schema(description = "학생 수", example = "25")
    Integer studentCount
) {
    // 수정 요청이므로 모든 필드가 optional이어서 compact constructor에서 null 체크하지 않음
}