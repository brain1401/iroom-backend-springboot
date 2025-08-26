package com.iroomclass.springbackend.domain.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.admin.dto.AcademyNameResponse;
import com.iroomclass.springbackend.domain.admin.dto.ErrorResponse;
import com.iroomclass.springbackend.domain.admin.dto.LoginRequest;
import com.iroomclass.springbackend.domain.admin.entity.Admin;
import com.iroomclass.springbackend.domain.admin.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 컨트롤러
 * 
 * 관리자 로그인과 학원 정보 조회 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자", description = "관리자 로그인 및 학원 정보 관리 API")
public class AdminController {
    
    private final AdminService adminService;
    
    /**
     * 관리자 로그인
     * 
     * 관리자가 아이디와 비밀번호로 로그인합니다.
     * 
     * @param loginRequest 로그인 요청 정보 (아이디, 비밀번호)
     * @return 로그인 성공 시 관리자 정보, 실패 시 에러 메시지
     */
    @PostMapping("/login")
    @Operation(
        summary = "관리자 로그인",
        description = "관리자가 아이디와 비밀번호로 로그인합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                schema = @Schema(implementation = Admin.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "로그인 실패 (아이디 또는 비밀번호 오류)",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<?> login(
        @Parameter(description = "로그인 요청 정보", required = true)
        @RequestBody LoginRequest loginRequest
    ) {
        log.info("관리자 로그인 요청: 아이디 = {}", loginRequest.getUsername());
        
        try {
            // 로그인 처리
            Admin admin = adminService.login(loginRequest.getUsername(), loginRequest.getPassword());
            
            if (admin != null) {
                log.info("관리자 로그인 성공: 아이디 = {}", loginRequest.getUsername());
                return ResponseEntity.ok(admin);
            } else {
                log.warn("관리자 로그인 실패: 아이디 = {}", loginRequest.getUsername());
                return ResponseEntity.status(401)
                    .body(new ErrorResponse("아이디 또는 비밀번호가 올바르지 않습니다."));
            }
            
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(new ErrorResponse("로그인 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 학원명 조회
     * 
     * 모든 페이지에서 표시할 학원명을 가져옵니다.
     * 
     * @return 학원명 정보
     */
    @GetMapping("/academy-name")
    @Operation(
        summary = "학원명 조회",
        description = "모든 페이지에서 표시할 학원명을 가져옵니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "학원명 조회 성공",
            content = @Content(
                schema = @Schema(implementation = AcademyNameResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<?> getAcademyName() {
        log.info("학원명 조회 요청");
        
        try {
            String academyName = adminService.getAcademyName();
            log.info("학원명 조회 성공: {}", academyName);
            
            return ResponseEntity.ok(new AcademyNameResponse(academyName));
            
        } catch (Exception e) {
            log.error("학원명 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(new ErrorResponse("학원명 조회 중 오류가 발생했습니다."));
        }
    }
}