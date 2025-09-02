package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.common.ApiResponseConstants;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.iroomclass.springbackend.domain.exam.dto.exam.ExamCreateRequest;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamCreateResponse;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamDetailResponse;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamListResponse;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamUpdateRequest;
import com.iroomclass.springbackend.domain.exam.service.ExamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 실제 시험 관리 컨트롤러
 * 
 * 시험 등록, 조회, 수정, 삭제 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "실제 시험 관리", description = "실제 시험 등록, 조회, 수정, 삭제 API")
public class ExamController {

    private final ExamService examService;

    /**
     * 시험 등록
     * 
     * @param request 시험 등록 요청
     * @return 생성된 시험 정보
     */
    @PostMapping
    @Operation(summary = "시험 등록", description = "시험지 초안을 기반으로 실제 시험을 등록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "시험 등록 성공", value = ApiResponseConstants.EXAM_CREATE_SUCCESS_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "입력 데이터 검증 실패", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험지 초안", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "시험지 초안 없음", value = ApiResponseConstants.EXAM_SHEET_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<ExamCreateResponse> createExam(@Valid @RequestBody ExamCreateRequest request) {
        log.info("시험 등록 요청: 시험지 ID={}, 학생 수={}", request.examSheetId(), request.studentCount());

        ExamCreateResponse response = examService.createExam(request);

        log.info("시험 등록 성공: ID={}, 이름={}", response.examId(), response.examName());

        return ApiResponse.success(ApiResponseConstants.EXAM_CREATE_SUCCESS, response);
    }

    /**
     * 전체 시험 목록 조회
     * 
     * @return 모든 시험 목록 (최신순)
     */
    @GetMapping
    @Operation(summary = "전체 시험 목록 조회", description = "모든 학년의 시험 목록을 최신순으로 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "시험 목록 조회 성공", value = ApiResponseConstants.EXAM_LIST_SUCCESS_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "서버 오류", value = ApiResponseConstants.SERVER_ERROR_EXAMPLE)))
    })
    public ApiResponse<ExamListResponse> getAllExams() {
        log.info("전체 시험 목록 조회 요청");

        ExamListResponse response = examService.getAllExams();

        log.info("전체 시험 목록 조회 성공: {}개", response.totalCount());

        return ApiResponse.success(ApiResponseConstants.EXAM_LIST_SUCCESS, response);
    }

    /**
     * 학년별 시험 목록 조회
     * 
     * @param grade 학년
     * @return 해당 학년의 시험 목록
     */
    @GetMapping("/grade/{grade}")
    @Operation(summary = "학년별 시험 목록 조회", description = "특정 학년의 시험 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "학년별 시험 조회 성공", value = ApiResponseConstants.EXAM_LIST_SUCCESS_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 학년", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "잘못된 파라미터", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE)))
    })
    public ApiResponse<ExamListResponse> getExamsByGrade(
            @Parameter(description = "학년", example = "1") @PathVariable int grade) {
        log.info("학년별 시험 목록 조회 요청: {}학년", grade);

        ExamListResponse response = examService.getExamsByGrade(grade);

        log.info("학년별 시험 목록 조회 성공: {}학년, {}개", grade, response.totalCount());

        return ApiResponse.success(ApiResponseConstants.EXAM_LIST_BY_GRADE_SUCCESS, response);
    }

    /**
     * 시험 상세 조회
     * 
     * @param examId 시험 ID
     * @return 시험 상세 정보
     */
    @GetMapping("/{examId}")
    @Operation(summary = "시험 상세 조회", description = "특정 시험의 상세 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "시험 상세 조회 성공", value = ApiResponseConstants.EXAM_DETAIL_SUCCESS_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험 ID", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "파라미터 타입 오류", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "시험 없음", value = ApiResponseConstants.EXAM_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<ExamDetailResponse> getExamDetail(
            @Parameter(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examId) {
        log.info("시험 상세 조회 요청: ID={}", examId);

        ExamDetailResponse response = examService.getExamDetail(examId);

        log.info("시험 상세 조회 성공: ID={}, 이름={}", examId, response.examName());

        return ApiResponse.success(ApiResponseConstants.EXAM_DETAIL_SUCCESS, response);
    }

    /**
     * 시험 수정
     * 
     * @param examId  시험 ID
     * @param request 수정 요청
     * @return 수정된 시험 정보
     */
    @PutMapping("/{examId}")
    @Operation(summary = "시험 수정", description = "시험 정보를 수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class), examples = @ExampleObject(name = "시험 수정 성공", value = ApiResponseConstants.EXAM_DETAIL_SUCCESS_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "입력 검증 실패", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "시험 없음", value = ApiResponseConstants.EXAM_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<ExamDetailResponse> updateExam(
            @Parameter(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examId,
            @Valid @RequestBody ExamUpdateRequest request) {
        log.info("시험 수정 요청: ID={}", examId);

        ExamDetailResponse response = examService.updateExam(examId, request);

        log.info("시험 수정 성공: ID={}", examId);

        return ApiResponse.success(ApiResponseConstants.EXAM_UPDATE_SUCCESS, response);
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험 ID", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "파라미터 타입 오류", value = ApiResponseConstants.BAD_REQUEST_EXAMPLE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험", content = @Content(schema = @Schema(implementation = ApiResponse.ErrorResponse.class), examples = @ExampleObject(name = "시험 없음", value = ApiResponseConstants.EXAM_NOT_FOUND_EXAMPLE)))
    })
    public ApiResponse<Void> deleteExam(
            @Parameter(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examId) {
        log.info("시험 삭제 요청: ID={}", examId);

        examService.deleteExam(examId);

        log.info("시험 삭제 성공: ID={}", examId);

        return ApiResponse.success(ApiResponseConstants.EXAM_DELETE_SUCCESS);
    }
}
