package com.iroomclass.springbackend.domain.user.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.user.dto.*;
import com.iroomclass.springbackend.domain.user.entity.User;
import com.iroomclass.springbackend.domain.user.service.UnifiedAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * 통합 인증 컨트롤러
 * 
 * <p>
 * 학생과 관리자 로그인을 모두 지원하는 통합 인증 API를 제공합니다.
 * userType에 따라 적절한 인증 방식을 자동 선택하며, 모든 사용자에게 JWT 토큰을 발급합니다.
 * </p>
 * 
 * <ul>
 * <li><strong>STUDENT</strong>: 3-factor 인증 (이름 + 전화번호 + 생년월일)</li>
 * <li><strong>TEACHER</strong>: 기본 인증 (사용자명 + 비밀번호)</li>
 * </ul>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "통합 인증 API", description = "학생/관리자 로그인 및 JWT 토큰 관리 API")
public class AuthController {

    private final UnifiedAuthService unifiedAuthService;

    /**
     * 통합 로그인
     * 
     * <p>
     * userType에 따라 적절한 인증 방식을 적용합니다:
     * </p>
     * <ul>
     * <li><strong>STUDENT</strong>: 3-factor 인증 (이름 + 전화번호 + 생년월일)</li>
     * <li><strong>TEACHER</strong>: 기본 인증 (사용자명 + 비밀번호)</li>
     * </ul>
     * 
     * @param loginRequest 통합 로그인 요청 데이터
     * @return 로그인 성공 시 JWT 토큰과 사용자 정보
     */
    @PostMapping("/login")
    @Operation(summary = "통합 로그인", description = """
            사용자 타입(userType)에 따라 자동으로 인증 방식을 선택하여 로그인을 처리합니다.

            **사용자 타입별 인증 방식:**

            📚 **STUDENT** (학생 로그인)
            - 3-factor 인증: 이름 + 전화번호 + 생년월일
            - name, phone, birthDate 필드가 필수입니다
            - 학생만 로그인 가능

            👩‍🏫 **TEACHER** (관리자 로그인)
            - 기본 인증: 사용자명 + 비밀번호
            - username과 password 필드가 필수입니다
            - 관리자만 로그인 가능

            **공통 사항:**
            - 성공 시 JWT 토큰 발급 (24시간 유효)
            - 다른 API 호출 시 `Authorization: Bearer {token}` 헤더 사용

            **요청 예시:**

            학생 로그인:
            ```json
            {
              "userType": "STUDENT",
              "name": "김철수",
              "phone": "010-1234-5678",
              "birthDate": "2008-03-15"
            }
            ```

            관리자 로그인:
            ```json
            {
              "userType": "TEACHER",
              "username": "admin",
              "password": "admin123"
            }
            ```
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "로그인 성공 - JWT 토큰과 사용자 정보 반환",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "학생 로그인 성공",
                                            summary = "학생 3-factor 인증 성공 응답",
                                            value = """
                                            {
                                              "result": "SUCCESS",
                                              "message": "로그인이 성공했습니다",
                                              "data": {
                                                "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
                                                "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                "userId": "123e4567-e89b-12d3-a456-426614174000",
                                                "username": null,
                                                "name": "김철수",
                                                "role": "STUDENT",
                                                "email": null,
                                                "phone": "010-1234-5678",
                                                "grade": 1,
                                                "birthDate": "2008-03-15",
                                                "academyName": null,
                                                "userType": "STUDENT"
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "관리자 로그인 성공",
                                            summary = "관리자 기본 인증 성공 응답",
                                            value = """
                                            {
                                              "result": "SUCCESS",
                                              "message": "로그인이 성공했습니다",
                                              "data": {
                                                "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
                                                "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
                                                "userId": "123e4567-e89b-12d3-a456-426614174001",
                                                "username": "admin",
                                                "name": "관리자",
                                                "role": "ADMIN",
                                                "email": "admin@example.com",
                                                "phone": null,
                                                "grade": null,
                                                "birthDate": null,
                                                "academyName": "이룸클래스",
                                                "userType": "TEACHER"
                                              }
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "입력 데이터 검증 오류 - 필수 필드 누락 또는 잘못된 userType",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "검증 오류 예시",
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "message": "학생 타입에서는 name이 필수입니다",
                                      "data": null
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", 
                    description = "인증 실패 - 잘못된 인증 정보",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "학생 인증 실패",
                                            summary = "학생 3-factor 인증 실패",
                                            value = """
                                            {
                                              "result": "ERROR",
                                              "message": "이름, 전화번호, 생년월일이 일치하는 학생을 찾을 수 없습니다",
                                              "data": null
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "관리자 인증 실패",
                                            summary = "관리자 기본 인증 실패",
                                            value = """
                                            {
                                              "result": "ERROR",
                                              "message": "잘못된 사용자명 또는 비밀번호입니다",
                                              "data": null
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500", 
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "서버 오류 예시",
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "message": "로그인 처리 중 오류가 발생했습니다",
                                      "data": null
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<UnifiedLoginResponse>> login(
            @Valid @RequestBody UnifiedLoginRequest loginRequest) {

        try {
            log.info("통합 로그인 API 호출: 사용자타입={}", loginRequest.userType());

            // 통합 인증 서비스 호출
            UnifiedLoginResponse loginResponse = unifiedAuthService.login(loginRequest);

            log.info("통합 로그인 API 성공: 사용자타입={}, 사용자={}, 역할={}",
                    loginRequest.userType(), loginResponse.name(), loginResponse.role());

            return ResponseEntity.ok(
                    ApiResponse.success("로그인이 성공했습니다", loginResponse));

        } catch (BadCredentialsException e) {
            log.warn("통합 로그인 실패: 사용자타입={}, 사유={}",
                    loginRequest.userType(), e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body((ApiResponse<UnifiedLoginResponse>) (ApiResponse<?>) ApiResponse.error(e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("통합 로그인 요청 오류: 사용자타입={}, 사유={}",
                    loginRequest.userType(), e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((ApiResponse<UnifiedLoginResponse>) (ApiResponse<?>) ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("통합 로그인 서버 오류: 사용자타입={}, 오류={}",
                    loginRequest.userType(), e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body((ApiResponse<UnifiedLoginResponse>) (ApiResponse<?>) ApiResponse
                            .error("로그인 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 학생 전용 로그인 (호환성 유지)
     * 
     * <p>
     * 기존 학생 로그인 엔드포인트와의 호환성을 위해 제공됩니다.
     * 새로운 클라이언트는 /auth/login 엔드포인트 사용을 권장합니다.
     * </p>
     * 
     * @param studentRequest 학생 로그인 요청
     * @return 학생 로그인 응답
     * @deprecated /auth/login 엔드포인트 사용 권장
     */
    @PostMapping("/student/login")
    @Operation(summary = "학생 전용 로그인 (호환성)", description = """
            학생 3-factor 인증을 위한 전용 엔드포인트입니다.

            ⚠️ **deprecated**: 새로운 클라이언트는 `/auth/login` 엔드포인트 사용을 권장합니다.

            **인증 방식:** 3-factor 인증 (이름 + 전화번호 + 생년월일)
            """, deprecated = true)
    @Deprecated
    public ResponseEntity<ApiResponse<StudentLoginResponse>> studentLogin(
            @Valid @RequestBody StudentLoginRequest studentRequest) {

        log.info("학생 전용 로그인 API 호출 (deprecated): 이름={}", studentRequest.name());

        try {
            // UnifiedLoginRequest로 변환
            UnifiedLoginRequest unifiedRequest = new UnifiedLoginRequest(
                    null, null, // username, password (학생 로그인에서는 null)
                    studentRequest.name(),
                    studentRequest.phone(),
                    studentRequest.birthDate(),
                    "STUDENT" // 강제로 학생 타입 설정
            );

            // 통합 인증 서비스 호출
            UnifiedLoginResponse unifiedResponse = unifiedAuthService.login(unifiedRequest);

            // 기존 StudentLoginResponse로 변환
            StudentLoginResponse studentResponse = unifiedResponse.toStudentLoginResponse();

            log.info("학생 전용 로그인 API 성공: 이름={}, ID={}",
                    studentResponse.name(), studentResponse.userId());

            return ResponseEntity.ok(
                    ApiResponse.success("학생 로그인이 성공했습니다", studentResponse));

        } catch (Exception e) {
            log.error("학생 전용 로그인 오류: 이름={}, 오류={}", studentRequest.name(), e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body((ApiResponse<StudentLoginResponse>) (ApiResponse<?>) ApiResponse
                            .error("학생 로그인에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 로그아웃
     * 
     * <p>
     * 리프레시 토큰을 무효화하여 완전한 로그아웃을 수행합니다.
     * 클라이언트에서도 저장된 토큰들을 삭제해야 합니다.
     * </p>
     * 
     * @param refreshRequest 무효화할 리프레시 토큰 (선택사항)
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = """
            현재 로그인된 사용자를 로그아웃합니다.

            **기능:**
            - 서버에 저장된 리프레시 토큰 무효화
            - 완전한 보안 로그아웃 처리

            **요청 방법:**
            1. 리프레시 토큰을 포함하여 요청 (권장)
            2. 빈 요청으로도 가능 (클라이언트 토큰만 삭제)

            **요청 예시:**
            ```json
            {
              "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
            }
            ```

            **주의사항:**
            - 로그아웃 후 모든 토큰이 무효화됩니다
            - 재로그인이 필요합니다
            - 클라이언트에서도 토큰을 삭제해야 합니다
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) RefreshTokenRequest refreshRequest) {

        log.info("로그아웃 API 호출");

        try {
            // 리프레시 토큰이 제공된 경우 무효화
            if (refreshRequest != null && refreshRequest.refreshToken() != null) {
                log.info("리프레시 토큰 무효화 처리 시작");

                try {
                    String refreshToken = refreshRequest.refreshToken();

                    // 1. 리프레시 토큰 유효성 확인 (JwtUtil을 직접 주입받아야 함)
                    // 현재는 UnifiedAuthService를 통해 처리

                    // 2. 리프레시 토큰으로 사용자 찾아서 무효화
                    User user = unifiedAuthService.getUserByRefreshToken(refreshToken);

                    if (user != null) {
                        unifiedAuthService.invalidateRefreshToken(user.getId());
                        log.info("리프레시 토큰 무효화 완료: userId={}, 사용자={}",
                                user.getId(), user.getName());
                    } else {
                        log.warn("리프레시 토큰에 해당하는 사용자를 찾을 수 없음");
                    }

                } catch (BadCredentialsException e) {
                    log.warn("유효하지 않은 리프레시 토큰으로 로그아웃 시도: {}", e.getMessage());
                    // 로그아웃은 실패하지 않도록 처리
                } catch (Exception e) {
                    log.error("리프레시 토큰 무효화 중 오류 발생: {}", e.getMessage(), e);
                    // 로그아웃은 실패하지 않도록 처리
                }
            }

            log.info("로그아웃 완료");

            return ResponseEntity.ok(
                    ApiResponse.success("로그아웃이 완료되었습니다"));

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
            // 로그아웃은 항상 성공으로 처리 (보안상 이유)
            return ResponseEntity.ok(
                    ApiResponse.success("로그아웃이 완료되었습니다"));
        }
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     * 
     * <p>
     * 유효한 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
     * 보안을 위해 새로운 리프레시 토큰도 함께 발급됩니다.
     * </p>
     * 
     * @param refreshRequest 리프레시 토큰 요청
     * @return 새로운 액세스 토큰과 사용자 정보
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = """
            리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.

            **특징:**
            - 액세스 토큰 만료 시 사용
            - 보안을 위해 새로운 리프레시 토큰도 함께 발급
            - 기존 리프레시 토큰은 자동으로 무효화

            **요청 예시:**
            ```json
            {
              "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
            }
            ```

            **주의사항:**
            - 리프레시 토큰은 7일간 유효합니다
            - 만료된 리프레시 토큰은 재로그인이 필요합니다
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 리프레시 토큰 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ApiResponse<UnifiedLoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshRequest) {

        try {
            log.info("리프레시 토큰 갱신 API 호출");

            // 리프레시 토큰으로 새로운 액세스 토큰 발급
            UnifiedLoginResponse refreshResponse = unifiedAuthService.refreshToken(refreshRequest.refreshToken());

            log.info("리프레시 토큰 갱신 성공: 사용자={}, 역할={}",
                    refreshResponse.name(), refreshResponse.role());

            return ResponseEntity.ok(
                    ApiResponse.success("토큰 갱신이 완료되었습니다", refreshResponse));

        } catch (BadCredentialsException e) {
            log.warn("리프레시 토큰 갱신 실패: 사유={}", e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body((ApiResponse<UnifiedLoginResponse>) (ApiResponse<?>) ApiResponse.error(e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("리프레시 토큰 요청 오류: 사유={}", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((ApiResponse<UnifiedLoginResponse>) (ApiResponse<?>) ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("리프레시 토큰 갱신 서버 오류: 오류={}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body((ApiResponse<UnifiedLoginResponse>) (ApiResponse<?>) ApiResponse
                            .error("토큰 갱신 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 현재 로그인된 사용자 정보 조회
     * 
     * <p>
     * JWT 토큰을 통해 현재 로그인된 사용자의 기본 정보를 조회합니다.
     * 토큰 유효성 검증에도 사용할 수 있습니다.
     * </p>
     * 
     * @return 로그인된 사용자 기본 정보
     */
    @GetMapping("/me")
    @Operation(summary = "현재 로그인 정보 조회", description = """
            현재 로그인된 사용자의 기본 정보를 조회합니다.

            **사용 방법:**
            - Authorization 헤더에 `Bearer {token}` 포함 필요
            - 토큰 유효성 검증에도 활용 가능
            - 학생과 관리자 모두 지원
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰이 없거나 만료된 경우")
    })
    public ResponseEntity<ApiResponse<UnifiedLoginResponse>> getCurrentUser() {
        // TODO: JWT에서 사용자 정보를 추출하여 반환
        // 현재는 예제 응답만 반환
        log.info("현재 사용자 정보 조회 API 호출");

        return ResponseEntity.ok(
                ApiResponse.success("현재 로그인 정보 조회 성공", null));
    }
}