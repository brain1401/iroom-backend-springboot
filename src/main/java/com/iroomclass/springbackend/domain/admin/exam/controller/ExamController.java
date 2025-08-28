package com.iroomclass.springbackend.domain.admin.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.iroomclass.springbackend.domain.admin.exam.dto.ExamCreateRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamCreateResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDetailResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamListResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamUpdateRequest;
import com.iroomclass.springbackend.domain.admin.exam.service.ExamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 실제 시험 관리 컨트롤러
 * 
 * 시험 등록, 조회, 수정, 삭제 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 - 실제 시험 관리", description = "실제 시험 등록, 조회, 수정, 삭제 API")
public class ExamController {

    private final ExamService examService;

    /**
     * 시험 등록
     * 
     * @param request 시험 등록 요청
     * @return 생성된 시험 정보
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "시험 등록", description = "시험지 초안을 기반으로 실제 시험을 등록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험지 초안")
    })
    public ApiResponse<ExamCreateResponse> createExam(@Valid @RequestBody ExamCreateRequest request) {
        log.info("시험 등록 요청: 시험지 초안 ID={}, 학생 수={}", request.examDraftId(), request.studentCount());

        ExamCreateResponse response = examService.createExam(request);

        log.info("시험 등록 성공: ID={}, 이름={}", response.examId(), response.examName());

        return ApiResponse.success("시험 등록 성공", response);
    }

    /**
     * 전체 시험 목록 조회
     * 
     * @return 모든 시험 목록 (최신순)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 시험 목록 조회", description = "모든 학년의 시험 목록을 최신순으로 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<ExamListResponse> getAllExams() {
        log.info("전체 시험 목록 조회 요청");

        ExamListResponse response = examService.getAllExams();

        log.info("전체 시험 목록 조회 성공: {}개", response.totalCount());

        return ApiResponse.success("전체 시험 목록 조회 성공", response);
    }

    /**
     * 학년별 시험 목록 조회
     * 
     * @param grade 학년
     * @return 해당 학년의 시험 목록
     */
    @GetMapping("/grade/{grade}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "학년별 시험 목록 조회", description = "특정 학년의 시험 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 학년")
    })
    public ApiResponse<ExamListResponse> getExamsByGrade(
            @Parameter(description = "학년", example = "1") @PathVariable int grade) {
        log.info("학년별 시험 목록 조회 요청: {}학년", grade);

        ExamListResponse response = examService.getExamsByGrade(grade);

        log.info("학년별 시험 목록 조회 성공: {}학년, {}개", grade, response.totalCount());

        return ApiResponse.success("학년별 시험 목록 조회 성공", response);
    }

    /**
     * 시험 상세 조회
     * 
     * @param examId 시험 ID
     * @return 시험 상세 정보
     */
    @GetMapping("/{examId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "시험 상세 조회", description = "특정 시험의 상세 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험")
    })
    public ApiResponse<ExamDetailResponse> getExamDetail(
            @Parameter(description = "시험 ID", example = "1") @PathVariable Long examId) {
        log.info("시험 상세 조회 요청: ID={}", examId);

        ExamDetailResponse response = examService.getExamDetail(examId);

        log.info("시험 상세 조회 성공: ID={}, 이름={}", examId, response.examName());

        return ApiResponse.success("시험 상세 조회 성공", response);
    }

    /**
     * 시험 수정
     * 
     * @param examId  시험 ID
     * @param request 수정 요청
     * @return 수정된 시험 정보
     */
    @PutMapping("/{examId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "시험 수정", description = "시험 정보를 수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험")
    })
    public ApiResponse<ExamDetailResponse> updateExam(
            @Parameter(description = "시험 ID", example = "1") @PathVariable Long examId,
            @Valid @RequestBody ExamUpdateRequest request) {
        log.info("시험 수정 요청: ID={}", examId);

        ExamDetailResponse response = examService.updateExam(examId, request);

        log.info("시험 수정 성공: ID={}", examId);

        return ApiResponse.success("시험 수정 성공", response);
    }

    /**
     * 시험 삭제
     * 
     * @param examId 시험 ID
     * @return 삭제 성공 여부
     */
    @DeleteMapping("/{examId}")
    @Operation(summary = "시험 삭제", description = "시험을 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험")
    })
    public ApiResponse<Void> deleteExam(
            @Parameter(description = "시험 ID", example = "1") @PathVariable Long examId) {
        log.info("시험 삭제 요청: ID={}", examId);

        examService.deleteExam(examId);

        log.info("시험 삭제 성공: ID={}", examId);

        return ApiResponse.success("시험 삭제 성공");
    }
}
