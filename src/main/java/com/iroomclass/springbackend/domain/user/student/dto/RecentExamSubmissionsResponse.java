package com.iroomclass.springbackend.domain.user.student.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 최근 시험 제출 이력 응답 DTO
 * 
 * 학생 메인화면에서 최근 제출한 시험 3건을 표시하기 위한 DTO입니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "최근 시험 제출 이력 응답")
public record RecentExamSubmissionsResponse(
    @Schema(description = "학생 이름", example = "김철수")
    String studentName,

    @Schema(description = "학생 전화번호", example = "010-1234-5678")
    String studentPhone,

    @Schema(description = "최근 시험 제출 목록 (최대 3건)")
    List<RecentExamInfo> recentExams
) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public RecentExamSubmissionsResponse {
        Objects.requireNonNull(studentName, "studentName는 필수입니다");
        Objects.requireNonNull(studentPhone, "studentPhone는 필수입니다");
        Objects.requireNonNull(recentExams, "recentExams는 필수입니다");
    }

    /**
     * 최근 시험 정보
     */
    @Schema(description = "최근 시험 정보")
    public record RecentExamInfo(
        @Schema(description = "시험 제출 ID", example = "1")
        Long submissionId,

        @Schema(description = "시험 ID", example = "1") 
        Long examId,

        @Schema(description = "시험명", example = "1학년 중간고사")
        String examName,

        @Schema(description = "총 문제 수", example = "20")
        int totalQuestions,

        @Schema(description = "단원명 목록", example = "정수와 유리수, 문자와 식")
        String unitNames,

        @Schema(description = "제출일시")
        LocalDateTime submittedAt,

        @Schema(description = "총점", example = "85")
        Integer totalScore,

        @Schema(description = "정답률", example = "85.0")
        double correctRate
    ) {
        /**
         * Compact Constructor - 입력 검증 수행
         */
        public RecentExamInfo {
            Objects.requireNonNull(submissionId, "submissionId는 필수입니다");
            Objects.requireNonNull(examId, "examId는 필수입니다");
            Objects.requireNonNull(examName, "examName는 필수입니다");
            Objects.requireNonNull(unitNames, "unitNames는 필수입니다");
        }
    }
}