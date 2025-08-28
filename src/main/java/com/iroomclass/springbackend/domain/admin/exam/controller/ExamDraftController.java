package com.iroomclass.springbackend.domain.admin.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDraftCreateRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDraftCreateResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDraftDetailResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDraftListResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDraftUpdateRequest;
import com.iroomclass.springbackend.domain.admin.exam.service.ExamDraftService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험지 초안 관리 컨트롤러
 * 
 * 시험지 초안 생성, 조회, 수정, 삭제 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/exam-draft")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 - 시험지 초안 관리", description = "시험지 초안 생성, 조회, 수정, 삭제 API")
public class ExamDraftController {

    private final ExamDraftService examDraftService;

    /**
     * 시험지 초안 생성
     * 
     * @param request 시험지 초안 생성 요청
     * @return 생성된 시험지 초안 정보
     */
    @PostMapping
    @Operation(summary = "시험지 초안 생성", description = "학년, 단원, 문제 개수를 선택하여 시험지 초안을 생성합니다. 선택된 단원들에서 랜덤으로 문제를 선택합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 단원")
    })
    public ApiResponse<ExamDraftCreateResponse> createExamDraft(@Valid @RequestBody ExamDraftCreateRequest request) {
        log.info("시험지 초안 생성 요청: 학년={}, 단원={}개, 문제={}개",
                request.grade(), request.unitIds().size(), request.totalQuestions());

        ExamDraftCreateResponse response = examDraftService.createExamDraft(request);

        log.info("시험지 초안 생성 성공: ID={}, 이름={}", response.examDraftId(), response.examName());

        return ApiResponse.success("성공", response);
    }

    /**
     * 전체 시험지 초안 목록 조회
     * 
     * @return 모든 시험지 초안 목록 (최신순)
     */
    @GetMapping
    @Operation(summary = "전체 시험지 초안 목록 조회", description = "모든 학년의 시험지 초안 목록을 최신순으로 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<ExamDraftListResponse> getAllExamDrafts() {
        log.info("전체 시험지 초안 목록 조회 요청");

        ExamDraftListResponse response = examDraftService.getAllExamDrafts();

        log.info("전체 시험지 초안 목록 조회 성공: {}개", response.totalCount());

        return ApiResponse.success("성공", response);
    }

    /**
     * 학년별 시험지 초안 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험지 초안 목록
     */
    @GetMapping("/grade/{grade}")
    @Operation(summary = "학년별 시험지 초안 목록 조회", description = "특정 학년의 모든 시험지 초안 목록을 조회합니다. 최신순으로 정렬됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 학년")
    })
    public ApiResponse<ExamDraftListResponse> getExamDraftsByGrade(
            @Parameter(description = "학년 (1/2/3)", example = "2") @PathVariable int grade) {
        log.info("학년 {} 시험지 초안 목록 조회 요청", grade);

        ExamDraftListResponse response = examDraftService.getExamDraftsByGrade(grade);

        log.info("학년 {} 시험지 초안 목록 조회 성공: {}개", grade, response.totalCount());

        return ApiResponse.success("성공", response);
    }

    /**
     * 시험지 초안 상세 조회
     * 
     * @param examDraftId 시험지 초안 ID
     * @return 시험지 초안 상세 정보
     */
    @GetMapping("/{examDraftId}")
    @Operation(summary = "시험지 초안 상세 조회", description = "특정 시험지 초안의 상세 정보를 조회합니다. 선택된 단원들과 문제들을 포함합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험지 초안 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험지 초안")
    })
    public ApiResponse<ExamDraftDetailResponse> getExamDraftDetail(
            @Parameter(description = "시험지 초안 ID", example = "1") @PathVariable Long examDraftId) {
        log.info("시험지 초안 {} 상세 조회 요청", examDraftId);

        ExamDraftDetailResponse response = examDraftService.getExamDraftDetail(examDraftId);

        log.info("시험지 초안 {} 상세 조회 성공: 단원={}개, 문제={}개",
                examDraftId, response.units().size(), response.questions().size());

        return ApiResponse.success("성공", response);
    }

    /**
     * 시험지 초안 수정 (문제 교체)
     * 
     * @param examDraftId 시험지 초안 ID
     * @param request     수정 요청
     * @return 수정된 시험지 초안 정보
     */
    @PutMapping("/{examDraftId}")
    @Operation(summary = "시험지 초안 수정 (문제 교체)", description = "시험지 초안의 특정 문제를 같은 단원의 다른 문제로 교체합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험지 초안 또는 문제")
    })
    public ApiResponse<ExamDraftDetailResponse> updateExamDraft(
            @Parameter(description = "시험지 초안 ID", example = "1") @PathVariable Long examDraftId,
            @Valid @RequestBody ExamDraftUpdateRequest request) {
        log.info("시험지 초안 {} 수정 요청: 문제={}번 교체", examDraftId, request.seqNo());

        ExamDraftDetailResponse response = examDraftService.updateExamDraft(examDraftId, request);

        log.info("시험지 초안 {} 수정 성공: 문제={}번 교체 완료", examDraftId, request.seqNo());

        return ApiResponse.success("성공", response);
    }
}
