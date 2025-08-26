package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 시험 답안 생성 요청 DTO
 * 
 * 학생이 답안 이미지를 업로드할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswerCreateRequest {
    
    /**
     * 시험 제출 ID
     */
    @NotNull(message = "시험 제출 ID는 필수입니다.")
    private Long examSubmissionId;
    
    /**
     * 문제 ID
     */
    @NotNull(message = "문제 ID는 필수입니다.")
    private Long questionId;
    
    /**
     * 답안 이미지 URL
     * 업로드된 이미지의 URL
     */
    @NotNull(message = "답안 이미지는 필수입니다.")
    private String answerImageUrl;
}

/**
 * 전체 답안지 촬영 요청 DTO
 * 
 * 학생이 답안지 전체를 한 번에 촬영할 때 사용됩니다.
 * AI가 전체 이미지에서 각 문제별 답안을 자동 추출합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ExamAnswerSheetCreateRequest {
    
    /**
     * 시험 제출 ID
     */
    @NotNull(message = "시험 제출 ID는 필수입니다.")
    private Long examSubmissionId;
    
    /**
     * 전체 답안지 이미지 URL 목록
     * 답안지가 여러 장일 수 있으므로 List로 관리
     * 업로드된 전체 답안지 이미지들의 URL
     */
    @NotNull(message = "답안지 이미지는 필수입니다.")
    @Size(min = 1, message = "최소 1장의 답안지 이미지가 필요합니다.")
    private List<String> answerSheetImageUrls;
}
