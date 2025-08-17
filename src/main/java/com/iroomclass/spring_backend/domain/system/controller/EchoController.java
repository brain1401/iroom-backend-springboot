package com.iroomclass.spring_backend.domain.system.controller;

import com.iroomclass.spring_backend.common.ApiResponse;
import com.iroomclass.spring_backend.domain.system.dto.EchoDto;
import com.iroomclass.spring_backend.domain.system.dto.EchoRequestDto;
import com.iroomclass.spring_backend.domain.system.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 에코 API 컨트롤러
 */
@Tag(name = "시스템 API", description = "시스템 모니터링 및 테스트용 API")
@RestController
@RequestMapping(value = "/api/system", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EchoController {

    private final SystemService systemService;

    /**
     * 에코 메시지 엔드포인트
     */
    @Operation(summary = "에코 메시지", description = "받은 메시지를 그대로 반환함")
    @PostMapping(value = "/echo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<EchoDto> echo(
            @Parameter(description = "에코할 메시지") @Valid @RequestBody EchoRequestDto request) {

        EchoDto echoResult = systemService.generateEcho(request.message());
        return ApiResponse.success("에코 메시지 처리 성공", echoResult);
    }
}
