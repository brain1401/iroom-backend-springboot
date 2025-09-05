package com.iroomclass.springbackend.domain.teacher.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.teacher.dto.LoginRequest;
import com.iroomclass.springbackend.domain.teacher.dto.LoginResponse;
import com.iroomclass.springbackend.domain.teacher.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 선생님 관리 컨트롤러
 */
@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "선생님 관리 API", description = """
        선생님 관리 관련 API입니다.

        주요 기능:
        - 선생님 로그인 인증
        - 선생님 대시보드 관련 기능 (추후 추가)
        """)
public class TeacherController {

    private final TeacherService teacherService;

    /**
     * 선생님 로그인 API
     * 
     * @param request 로그인 요청 정보
     * @return 로그인 결과
     */
    @PostMapping("/login")
    @Operation(summary = "선생님 로그인", description = """
            선생님 사용자명과 비밀번호로 로그인을 처리합니다.

            로그인 성공 시:
            - 선생님 기본 정보 반환
            - HTTP 200 상태 코드

            로그인 실패 시:
            - HTTP 401 Unauthorized 상태 코드
            - 오류 메시지 반환
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 실패 - 사용자명 또는 비밀번호 불일치", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 - 입력 데이터 검증 실패", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("선생님 로그인 API 호출: username={}", request.username());

        LoginResponse response = teacherService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("로그인에 성공했습니다", response));
    }
}