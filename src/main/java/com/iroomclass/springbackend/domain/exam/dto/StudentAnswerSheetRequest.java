package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 학생 정보 기반 답안지 조회 요청 DTO
 * 
 * <p>학생 정보와 시험 ID를 통해 해당 학생의 답안지를 조회하기 위한 요청 DTO입니다.</p>
 */
@Schema(description = "학생 답안지 조회 요청 DTO")
public record StudentAnswerSheetRequest(
    @NotBlank(message = "학생 이름은 필수입니다")
    @Schema(description = "학생 이름", example = "김철수", required = true) 
    String name,
    
    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
    @Schema(description = "학생 전화번호", example = "010-1234-5678", required = true)
    String phone,
    
    @NotNull(message = "생년월일은 필수입니다")
    @Schema(description = "학생 생년월일", example = "2008-03-15", required = true)
    LocalDate birthDate
) {
    /**
     * Compact constructor로 null 검증
     */
    public StudentAnswerSheetRequest {
        Objects.requireNonNull(name, "name은 null일 수 없습니다");
        Objects.requireNonNull(phone, "phone은 null일 수 없습니다");
        Objects.requireNonNull(birthDate, "birthDate는 null일 수 없습니다");
        
        // 추가 검증: 이름 공백 제거
        name = name.trim();
        phone = phone.trim();
    }
}