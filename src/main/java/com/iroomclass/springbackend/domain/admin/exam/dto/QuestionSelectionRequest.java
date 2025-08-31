package com.iroomclass.springbackend.domain.admin.exam.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;



/**
 * 시험지에 문제 추가 요청 DTO
 * 
 * 문제 직접 선택 시스템에서 특정 문제를 시험지에 추가할 때 사용합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public record QuestionSelectionRequest(
    
    @NotNull(message = "문제 ID는 필수입니다")
    @Schema(description = "선택할 문제 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID questionId,
    
    @NotNull(message = "배점은 필수입니다")
    @Positive(message = "배점은 양수여야 합니다") 
    @Schema(description = "문제 배점", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer points
) {
    
    /**
     * Compact constructor로 중요한 불변성 유지
     * 
     * 이중 검증 아키텍처:
     * 1. Compact Constructor: 객체 생성을 방해하는 중요한 문제만 처리 (Bean Validation과 연계 가능)
     * 2. Bean Validation: 비즈니스 룰 및 상세한 검증 (프레임워크 레벨에서 @Valid 사용)
     * 
     * Bean Validation이 정상동작할 수 있도록 기본적인 제약만 검사
     */
    public QuestionSelectionRequest {
        // 이중 검증 아키텍처: Compact Constructor가 우선 실행되고, Bean Validation은 프레임워크에서 별도 실행
        // Compact Constructor 테스트들이 명시적으로 이 검증들을 기대함
        
        // Null 체크 - NullPointerException 예상
        if (questionId == null) {
            throw new NullPointerException("문제 ID는 필수입니다");
        }
        if (points == null) {
            throw new NullPointerException("배점은 필수입니다");
        }
        
        // UUID는 범위 체크 불필요 (UUID 형식 자체가 유효성을 보장)
        if (points <= 0) {
            throw new IllegalArgumentException("배점은 양수여야 합니다: " + points);
        }

    }
}