package com.iroomclass.springbackend.domain.user.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.common.ResultStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.iroomclass.springbackend.domain.user.dto.LoginRequest;
import com.iroomclass.springbackend.domain.user.entity.Admin;
import com.iroomclass.springbackend.domain.user.service.TeacherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 컨트롤러
 * 
 * * 관리자 인증 정보 검증 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자", description = "관리자 인증 정보 검증 API")
public class AdminController {

    private final TeacherService adminService;

    /**
     * 관리자 인증 정보 검증
     * 
     * @param request 인증 요청 정보
     * @return 인증 검증 결과
     */
    @PostMapping("/verify-credentials")
    @Operation(summary = "관리자 인증 정보 검증", description = "관리자 아이디와 비밀번호가 올바른지 검증합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "인증 성공",
                content = @Content(
                    schema = @Schema(implementation = ApiResponse.SuccessResponse.class),
                    examples = @ExampleObject(
                        name = "인증 성공",
                        summary = "관리자 인증 성공",
                        value = """
                        {
                          "result": "SUCCESS",
                          "message": "관리자 인증 정보 검증 성공",
                          "data": "인증 성공"
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", 
                    description = "인증 실패 (잘못된 아이디/비밀번호)", 
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    summary = "잘못된 아이디 또는 비밀번호",
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "message": "인증에 실패했습니다",
                                      "data": null
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "잘못된 요청", 
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "입력 검증 실패",
                                    summary = "입력 데이터 검증 실패",
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "message": "입력 데이터 검증에 실패했습니다",
                                      "data": null
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
                        summary = "예상치 못한 서버 오류",
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
    })
    public ApiResponse<String> verifyCredentials(@Valid @RequestBody LoginRequest request) {
        log.info("관리자 인증 정보 검증 요청: {}", request.username());

        try {
            Admin admin = adminService.verifyCredentials(request.username(), request.password());
            log.info("관리자 인증 정보 검증 성공: {}", request.username());
            return ApiResponse.success("관리자 인증 정보 검증 성공", "인증 성공");
        } catch (RuntimeException e) {
            log.warn("관리자 인증 정보 검증 실패: {} - {}", request.username(), e.getMessage());
            return new ApiResponse<>(ResultStatus.ERROR, "인증 실패", null);
        }
    }
}