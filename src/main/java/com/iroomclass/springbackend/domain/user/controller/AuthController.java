package com.iroomclass.springbackend.domain.user.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.user.dto.LoginRequest;
import com.iroomclass.springbackend.domain.user.dto.LoginResponse;
import com.iroomclass.springbackend.domain.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
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
 * 인증 요청 컨트롤러
 * 
 * <p>로그인, 로그아웃 등 인증 관련 API 엔드포인트를 제공합니다.
 * JWT 기반 인증을 사용하며, 관리자만 로그인이 가능합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증 API", description = "로그인/로그아웃 및 JWT 토큰 관리 API")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 관리자 로그인
     * 
     * <p>관리자 사용자명과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.
     * 발급된 토큰은 다른 API 호출 시 Authorization 헤더에 포함해야 합니다.</p>
     * 
     * @param loginRequest 로그인 요청 데이터
     * @return 로그인 성공 시 JWT 토큰과 관리자 정보
     */
    @PostMapping("/login")
    @Operation(
        summary = "관리자 로그인",
        description = """
            관리자 인증을 통해 JWT 토큰을 발급받습니다.
            
            **사용 방법:**
            1. 관리자 사용자명과 비밀번호로 로그인
            2. 성공 시 반환된 JWT 토큰을 저장
            3. 다른 API 호출 시 `Authorization: Bearer {token}` 헤더 사용
            
            **주의사항:**
            - 학생은 로그인할 수 없습니다
            - 토큰은 24시간 동안 유효합니다
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "로그인 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증 실패 - 잘못된 사용자명 또는 비밀번호"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "입력 데이터 검증 오류"
        )
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("관리자 로그인 API 호출: {}", loginRequest.username());
            
            LoginResponse loginResponse = authService.login(loginRequest);
            
            log.info("관리자 로그인 API 성공: {} (역할: {})", 
                    loginResponse.username(), loginResponse.role());
            
            return ResponseEntity.ok(
                ApiResponse.success("로그인이 성공했습니다", loginResponse)
            );
            
        } catch (BadCredentialsException e) {
            log.warn("관리자 로그인 실패: {} - {}", loginRequest.username(), e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body((ApiResponse<LoginResponse>) (ApiResponse<?>) ApiResponse.error(e.getMessage()));
            
        } catch (Exception e) {
            log.error("관리자 로그인 오류: {} - {}", loginRequest.username(), e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body((ApiResponse<LoginResponse>) (ApiResponse<?>) ApiResponse.error("로그인 처리 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 로그아웃
     * 
     * <p>JWT 토큰 기반 인증에서는 서버측에서 토큰을 무효화할 필요가 없습니다.
     * 클라이언트에서 토큰을 삭제하면 로그아웃이 완료됩니다.</p>
     * 
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = """
            현재 로그인된 관리자를 로그아웃합니다.
            
            **주의사항:**
            - JWT 토큰은 서버측에서 무효화되지 않습니다
            - 클라이언트에서 토큰을 삭제해야 합니다
            - 보안상 중요한 작업 후에는 반드시 로그아웃하세요
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "로그아웃 성공"
        )
    })
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT 기반 인증에서는 서버측에서 할 일이 없음
        // 클라이언트에서 토큰 삭제만 하면 됨
        
        log.info("로그아웃 API 호출");
        
        return ResponseEntity.ok(
            ApiResponse.success("로그아웃이 완료되었습니다")
        );
    }
    
    /**
     * 현재 로그인된 관리자 정보 조회
     * 
     * <p>JWT 토큰을 통해 현재 로그인된 관리자의 기본 정보를 조회합니다.
     * 토큰 유효성 검증에도 사용할 수 있습니다.</p>
     * 
     * @return 로그인된 관리자 기본 정보
     */
    @GetMapping("/me")
    @Operation(
        summary = "현재 로그인 정보 조회",
        description = """
            현재 로그인된 관리자의 기본 정보를 조회합니다.
            
            **사용 방법:**
            - Authorization 헤더에 `Bearer {token}` 포함 필요
            - 토큰 유효성 검증에도 활용 가능
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증 토큰이 없거나 만료된 경우"
        )
    })
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser() {
        // TODO: JWT에서 사용자 정보를 추출하여 반환
        // 현재는 예제 응답만 반환
        return ResponseEntity.ok(
            ApiResponse.success("현재 로그인 정보 조회 성공", null)
        );
    }
}