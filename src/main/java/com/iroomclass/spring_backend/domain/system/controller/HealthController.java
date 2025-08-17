package com.iroomclass.spring_backend.domain.system.controller;

import com.iroomclass.spring_backend.common.ApiResponse;
import com.iroomclass.spring_backend.domain.system.dto.SystemHealthDto;
import com.iroomclass.spring_backend.domain.system.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시스템 헬스 API 컨트롤러
 */
@Tag(name = "시스템 API", description = "시스템 모니터링 및 테스트용 API")
@RestController
@RequestMapping(value = "/system", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class HealthController {

    private final SystemService systemService;

    /**
     * 헬스 체크 엔드포인트
     */
    @Operation(summary = "헬스 체크", description = "서버 상태 확인용 엔드포인트")
    @GetMapping("/health")
    public ApiResponse<SystemHealthDto> healthCheck() {
        SystemHealthDto healthStatus = systemService.checkHealth();
        return ApiResponse.success(healthStatus);
    }
}
