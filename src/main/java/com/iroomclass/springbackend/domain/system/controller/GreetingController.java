package com.iroomclass.springbackend.domain.system.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.system.dto.GreetingDto;
import com.iroomclass.springbackend.domain.system.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인사 API 컨트롤러
 */
@Tag(name = "시스템 API", description = "시스템 모니터링 및 테스트용 API")
@RestController
@RequestMapping(value = "/system", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class GreetingController {

    private final SystemService systemService;

    /**
     * 인사 메시지 엔드포인트
     */
    @Operation(summary = "인사 메시지", description = "이름을 받아 인사 메시지 반환")
    @GetMapping("/hello")
    public ApiResponse<GreetingDto> hello(
            @Parameter(description = "인사할 이름", example = "홍길동", name = "name") @RequestParam(name = "name", defaultValue = "World") String name) {

        GreetingDto greeting = systemService.generateGreeting(name);
        return ApiResponse.success("인사 메시지 조회 성공", greeting);
    }
}
