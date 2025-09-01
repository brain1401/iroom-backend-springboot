package com.iroomclass.springbackend.domain.analysis.dto;

import com.iroomclass.springbackend.common.BaseRecord;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관리자 대시보드 응답 DTO
 * 
 * <p>현재는 학원명만 표시하는 간단한 대시보드입니다.</p>
 */
@Schema(name = "DashboardResponse", description = "관리자 대시보드 정보")
public record DashboardResponse(
    @Schema(description = "학원명", example = "이룸클래스")
    String academyName
) implements BaseRecord {
    
    public DashboardResponse {
        requireNonNull(academyName, "academyName");
    }
}