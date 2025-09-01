package com.iroomclass.springbackend.domain.system.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.system.dto.GreetingResponse;
import com.iroomclass.springbackend.domain.system.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping(value = "system", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class GreetingController {

    private final SystemService systemService;

    /**
     * 인사 메시지 엔드포인트
     */
    @Operation(summary = "인사 메시지", description = "이름을 받아 인사 메시지 반환", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인사 메시지 생성 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "인사 메시지 성공", summary = "인사 메시지 생성 성공", value = """
                    {
                      "result": "SUCCESS",
                      "message": "인사 메시지 조회 성공",
                      "data": {
                        "id": 1,
                        "content": "안녕하세요, 홍길동님!",
                        "timestamp": "2024-09-01T10:30:00"
                      }
                    }
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "잘못된 파라미터", summary = "파라미터 값이 올바르지 않음", value = """
                    {
                      "result": "ERROR",
                      "message": "파라미터 'name'의 값이 올바르지 않습니다",
                      "data": null
                    }
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "서버 오류", summary = "서버 내부 오류 발생", value = """
                    {
                      "result": "ERROR",
                      "message": "서버 내부 오류가 발생했습니다",
                      "data": null
                    }
                    """)))
    })
    @GetMapping("/hello")
    public ApiResponse<GreetingResponse> hello(
            @Parameter(description = "인사할 이름", example = "홍길동", name = "name") @RequestParam(name = "name", defaultValue = "World") String name) {

        GreetingResponse greeting = systemService.generateGreeting(name);
        return ApiResponse.success("인사 메시지 조회 성공", greeting);
    }
}
