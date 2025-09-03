package com.iroomclass.springbackend.domain.student.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 최근 제출한 시험 응답 DTO
 * 
 * <p>학생이 최근 제출한 시험의 기본 정보를 담는 DTO입니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "최근 제출한 시험 정보", example = """
    {
      "examName": "중간고사",
      "submittedAt": "2024-08-17T14:30:00",
      "content": "수학 중간고사 문제",
      "totalQuestions": 20
    }
    """)
public record RecentSubmissionDto(
    
    @Schema(description = "시험명", example = "중간고사")
    String examName,
    
    @Schema(description = "제출 날짜", example = "2024-08-17T14:30:00")
    LocalDateTime submittedAt,
    
    @Schema(description = "시험 내용", example = "수학 중간고사 문제")
    String content,
    
    @Schema(description = "총 문항 수", example = "20")
    Long totalQuestions
    
) {
    /**
     * 정적 팩토리 메서드 - 생성자 대안
     * 
     * @param examName       시험명
     * @param submittedAt    제출 날짜
     * @param content        시험 내용
     * @param totalQuestions 총 문항 수
     * @return RecentSubmissionDto 인스턴스
     */
    public static RecentSubmissionDto of(String examName, LocalDateTime submittedAt, String content, Long totalQuestions) {
        return new RecentSubmissionDto(examName, submittedAt, content, totalQuestions);
    }
}