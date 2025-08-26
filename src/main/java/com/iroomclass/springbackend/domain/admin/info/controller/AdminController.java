package com.iroomclass.springbackend.domain.admin.info.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.admin.info.dto.AcademyNameResponse;
import com.iroomclass.springbackend.domain.admin.info.dto.ErrorResponse;
import com.iroomclass.springbackend.domain.admin.info.dto.LoginRequest;
import com.iroomclass.springbackend.domain.admin.info.entity.Admin;
import com.iroomclass.springbackend.domain.admin.info.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 컨트롤러
 * 
 * 관리자 로그인, 학원명 조회 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자", description = "관리자 로그인, 학원명 조회 API")
public class AdminController {
    
    private final AdminService adminService;
    
    /**
     * 관리자 로그인
     * 
     * @param request 로그인 요청 정보
     * @return 로그인 성공 여부
     */
    @PostMapping("/login")
    @Operation(
        summary = "관리자 로그인",
        description = "관리자 계정으로 로그인합니다. 아이디와 비밀번호를 입력받아 인증합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "로그인 실패 (잘못된 아이디/비밀번호)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        log.info("관리자 로그인 요청: {}", request.getUsername());
        
        try {
            Admin admin = adminService.login(request.getUsername(), request.getPassword());
            log.info("관리자 로그인 성공: {}", request.getUsername());
            return ResponseEntity.ok("로그인 성공");
        } catch (RuntimeException e) {
            log.warn("관리자 로그인 실패: {} - {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body("로그인 실패");
        }
    }
    
    /**
     * 학원명 조회
     * 
     * @return 학원명
     */
    @GetMapping("/academy-name")
    @Operation(
        summary = "학원명 조회",
        description = "시스템에 등록된 학원명을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = AcademyNameResponse.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "학원명을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<AcademyNameResponse> getAcademyName() {
        log.info("학원명 조회 요청");
        
        String academyName = adminService.getAcademyName();
        
        log.info("학원명 조회 성공: {}", academyName);
        
        return ResponseEntity.ok(new AcademyNameResponse(academyName));
    }
}