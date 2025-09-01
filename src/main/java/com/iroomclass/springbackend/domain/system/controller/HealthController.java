package com.iroomclass.springbackend.domain.system.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.system.dto.SystemHealthDto;
import com.iroomclass.springbackend.domain.system.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Operation(
            summary = "종합 시스템 헬스체크", 
            description = "Spring Boot 애플리케이션, 데이터베이스, AI 서버의 상태를 종합적으로 확인합니다.",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "헬스체크 성공",
                    content = @Content(
                        schema = @Schema(implementation = ApiResponse.SuccessResponse.class),
                        examples = @ExampleObject(
                            name = "헬스체크 성공",
                            summary = "정상 응답 예시",
                            value = """
                            {
                              "result": "SUCCESS",
                              "message": "시스템이 정상적으로 동작 중입니다",
                              "data": {
                                "status": "UP",
                                "timestamp": "2024-09-01T10:30:00",
                                "message": "All systems operational"
                              }
                            }
                            """
                        )
                    )
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500", 
                    description = "서버 내부 오류", 
                    content = @Content(
                        schema = @Schema(implementation = ApiResponse.ErrorResponse.class),
                        examples = @ExampleObject(
                            name = "서버 오류",
                            summary = "서버 내부 오류 발생",
                            value = """
                            {
                              "result": "ERROR",
                              "message": "서버 내부 오류가 발생했습니다",
                              "data": null
                            }
                            """
                        )
                    )
                )
            }
    )
    @GetMapping("/health")
    public ApiResponse<SystemHealthDto> healthCheck() {
        SystemHealthDto healthStatus = systemService.checkHealth();
        return ApiResponse.success(healthStatus);
    }
}