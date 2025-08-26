package com.iroomclass.springbackend.domain.admin.dashboard.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.admin.dashboard.dto.DashboardDto;
import com.iroomclass.springbackend.domain.admin.dashboard.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 API 컨트롤러 (간단 버전)
 * 
 * <p>현재는 학원명만 표시하는 간단한 대시보드 API입니다.</p>
 */
@Slf4j
@Tag(name = "관리자 - 대시보드 API", description = "관리자 대시보드 API")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 데이터 조회 (간단 버전)
     * 
     * @return 학원명이 포함된 대시보드 정보
     */
    @Operation(
        summary = "대시보드 데이터 조회",
        description = "관리자 메인 화면에 표시할 학원명을 조회합니다"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard() {
        log.info("대시보드 데이터 조회 요청");
        
        DashboardDto dashboard = dashboardService.getDashboardData();
        
        return ResponseEntity.ok(ApiResponse.success("대시보드 조회 성공", dashboard));
    }

    
}
