package com.iroomclass.springbackend.domain.admin.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관리자 대시보드 DTO (간단 버전)
 * 
 * <p>현재는 학원명만 표시하는 간단한 대시보드입니다.</p>
 */
@Schema(description = "관리자 대시보드 정보")
public record DashboardDto(
    @Schema(description = "학원명", example = "이이룸클래스")
    String academyName
) {
    
}
