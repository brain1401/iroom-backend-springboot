package com.iroomclass.springbackend.domain.admin.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관리자 대시보드 DTO
 * 
 * <p>관리자 메인 화면에 표시할 데이터를 담습니다.</p>
 */
@Schema(description = "관리자 대시보드 정보")
public record AdminDashboardDto(
    @Schema(description = "환영 메시지", example = "학원 관리 시스템에 오신 것을 환영합니다!")
    String welcomeMessage,

    @Schema(description = "마지막 로그인 시간", example = "2025-01-01T00:00:00")
    LocalDateTime lastLogin,

    @Schema(description = "시스템 상태", example = "정상 운영 중")
    String systemStatus
) {}
