package com.iroomclass.springbackend.domain.auth.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.auth.dto.VerifyStudentRequest;
import com.iroomclass.springbackend.domain.auth.dto.VerifyTeacherRequest;
import com.iroomclass.springbackend.domain.auth.dto.VerificationResponse;
import com.iroomclass.springbackend.domain.auth.service.AuthVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 검증 컨트롤러
 * 
 * <p>
 * 학생과 선생님의 DB 매칭 검증을 제공하는 REST API 컨트롤러입니다.
 * JWT 토큰이나 세션 관리 없이 단순한 true/false 검증 결과를 반환합니다.
 * </p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증 검증 API", description = "학생/선생님 DB 매칭 검증 API")
public class AuthController {

    private final AuthVerificationService authVerificationService;

    /**
     * 학생 검증
     * 
     * <p>
     * 학생의 이름, 전화번호, 생년월일을 통해 데이터베이스에서 매칭되는 
     * 학생이 있는지 검증합니다. 3-factor 인증을 통한 정확한 매칭을 보장합니다.
     * </p>
     * 
     * @param request 학생 검증 요청 데이터
     * @return 검증 성공 시 true, 실패 시 false
     */
    @PostMapping("/verify-student")
    @Operation(
        summary = "학생 검증",
        description = """
            학생의 3-factor 정보(이름, 전화번호, 생년월일)를 통해 DB에서 매칭되는 학생이 있는지 검증합니다.
            
            **검증 방식:**
            - 이름: 정확히 일치하는 학생 이름
            - 전화번호: 010-1234-5678 형식의 정확한 매칭
            - 생년월일: YYYY-MM-DD 형식의 정확한 매칭
            - 역할: STUDENT 역할을 가진 사용자만 대상
            
            **반환값:**
            - verified: true (검증 성공) 또는 false (검증 실패)
            
            **보안 고려사항:**
            - 개인정보 보호를 위해 검증 실패 시 구체적인 실패 이유를 제공하지 않습니다
            - 브루트 포스 공격 방지를 위해 적절한 요청 제한이 필요할 수 있습니다
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "검증 완료 - 성공/실패 여부 반환",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "학생 검증 성공",
                        summary = "DB에서 매칭되는 학생을 찾은 경우",
                        value = """
                            {
                              "result": "SUCCESS",
                              "message": "학생 검증이 완료되었습니다",
                              "data": {
                                "verified": true
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "학생 검증 실패",
                        summary = "DB에서 매칭되는 학생을 찾지 못한 경우",
                        value = """
                            {
                              "result": "SUCCESS",
                              "message": "학생 검증이 완료되었습니다",
                              "data": {
                                "verified": false
                              }
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 - 필수 필드 누락 또는 형식 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "입력 검증 오류",
                    value = """
                        {
                          "result": "ERROR",
                          "message": "이름은 필수입니다",
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
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                        {
                          "result": "ERROR",
                          "message": "학생 검증 중 오류가 발생했습니다",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    public ApiResponse<VerificationResponse> verifyStudent(@Valid @RequestBody VerifyStudentRequest request) {
        log.info("학생 검증 API 호출: 이름={}", request.name());

        try {
            VerificationResponse response = authVerificationService.verifyStudent(request);
            
            if (response.verified()) {
                log.info("학생 검증 API 성공: 이름={}", request.name());
            } else {
                log.info("학생 검증 API 실패: 이름={}", request.name());
            }
            
            return ApiResponse.success("학생 검증이 완료되었습니다", response);
            
        } catch (Exception e) {
            log.error("학생 검증 API 오류: 이름={}, 오류={}", request.name(), e.getMessage(), e);
            throw new RuntimeException("학생 검증 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 선생님 검증
     * 
     * <p>
     * 선생님의 사용자명과 비밀번호를 통해 데이터베이스에서 매칭되는 
     * 선생님이 있는지 검증합니다. 관리자 역할의 사용자만 대상으로 합니다.
     * </p>
     * 
     * @param request 선생님 검증 요청 데이터
     * @return 검증 성공 시 true, 실패 시 false
     */
    @PostMapping("/verify-teacher")
    @Operation(
        summary = "선생님 검증",
        description = """
            선생님의 사용자명과 비밀번호를 통해 DB에서 매칭되는 선생님이 있는지 검증합니다.
            
            **검증 방식:**
            - 사용자명: 정확히 일치하는 관리자 사용자명
            - 비밀번호: 정확히 일치하는 비밀번호 (평문 비교)
            - 역할: ADMIN 역할을 가진 사용자만 대상
            
            **반환값:**
            - verified: true (검증 성공) 또는 false (검증 실패)
            
            **보안 고려사항:**
            - 비밀번호가 평문으로 저장되어 있다면 해싱 처리가 필요합니다
            - 로그인 시도 제한 및 모니터링이 권장됩니다
            - 검증 실패 시 구체적인 실패 이유를 제공하지 않습니다
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "검증 완료 - 성공/실패 여부 반환",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "선생님 검증 성공",
                        summary = "DB에서 매칭되는 선생님을 찾은 경우",
                        value = """
                            {
                              "result": "SUCCESS",
                              "message": "선생님 검증이 완료되었습니다",
                              "data": {
                                "verified": true
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "선생님 검증 실패",
                        summary = "DB에서 매칭되는 선생님을 찾지 못한 경우",
                        value = """
                            {
                              "result": "SUCCESS",
                              "message": "선생님 검증이 완료되었습니다",
                              "data": {
                                "verified": false
                              }
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 - 필수 필드 누락 또는 형식 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "입력 검증 오류",
                    value = """
                        {
                          "result": "ERROR",
                          "message": "사용자명은 필수입니다",
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
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                        {
                          "result": "ERROR",
                          "message": "선생님 검증 중 오류가 발생했습니다",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    public ApiResponse<VerificationResponse> verifyTeacher(@Valid @RequestBody VerifyTeacherRequest request) {
        log.info("선생님 검증 API 호출: 사용자명={}", request.username());

        try {
            VerificationResponse response = authVerificationService.verifyTeacher(request);
            
            if (response.verified()) {
                log.info("선생님 검증 API 성공: 사용자명={}", request.username());
            } else {
                log.info("선생님 검증 API 실패: 사용자명={}", request.username());
            }
            
            return ApiResponse.success("선생님 검증이 완료되었습니다", response);
            
        } catch (Exception e) {
            log.error("선생님 검증 API 오류: 사용자명={}, 오류={}", request.username(), e.getMessage(), e);
            throw new RuntimeException("선생님 검증 중 오류가 발생했습니다", e);
        }
    }
}