package com.iroomclass.springbackend.domain.admin.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 학원명 응답 DTO
 * 
 * 학원명 조회 결과를 담습니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학원명 응답 정보")
public class AcademyNameResponse {

    /**
     * 학원명
     */
    @Schema(description = "학원명", example = "이룸클래스")
    private String academyName;
}
