package com.iroomclass.springbackend.domain.admin.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.admin.dto.AdminDashboardDto;
import com.iroomclass.springbackend.domain.admin.dto.AdminLoginRequest;
import com.iroomclass.springbackend.domain.admin.dto.AdminLoginResponse;
import com.iroomclass.springbackend.domain.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 관리자 API 컨트롤러
 * 
 * <p>관리자 로그인 및 대시보드 관련 API를 제공합니다.
 * RESTful 원칙에 따라 설계되었습니다.</p>
 */
@Slf4j
@Tag(name = "관리자 API", description = "관리자 로그인 및 대시보드 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 로그인
     * 
     * @param request 로그인 요청 정보
     * @return 로그인 결과
     */
    @Operation(
        summary = "관리자 로그인",
        description = "아이디와 비밀번호를 사용하여 관리자 로그인을 수행합니다"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
        @Valid @RequestBody AdminLoginRequest request
    ) {
        log.info("관리자 로그인 요청: {}", request.username());

        AdminLoginResponse response = adminService.login(request);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("로그인에 성공했습니다.", response));
    }
}
