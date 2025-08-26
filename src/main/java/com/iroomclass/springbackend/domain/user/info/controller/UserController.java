package com.iroomclass.springbackend.domain.user.info.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.user.info.dto.UserLoginRequest;
import com.iroomclass.springbackend.domain.user.info.dto.UserLoginResponse;
import com.iroomclass.springbackend.domain.user.info.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 로그인 컨트롤러
 * 
 * 학생이 로그인할 수 있는 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/user/info")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "학생 로그인", description = "학생 로그인 API")
public class UserController {
    
    private final UserService userService;
    
    /**
     * 학생 로그인
     * 
     * @param request 학생 로그인 요청 (이름, 전화번호)
     * @return 로그인 성공 정보
     */
    @PostMapping("/login")
    @Operation(
        summary = "학생 로그인",
        description = "이름과 전화번호로 학생 로그인을 수행합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 학생")
    })
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        log.info("학생 로그인 요청: 이름={}, 전화번호={}", request.getName(), request.getPhone());
        
        UserLoginResponse response = userService.login(request);
        
        log.info("학생 로그인 성공: 이름={}, ID={}", response.getName(), response.getUserId());
        
        return ResponseEntity.ok(response);
    }
}
