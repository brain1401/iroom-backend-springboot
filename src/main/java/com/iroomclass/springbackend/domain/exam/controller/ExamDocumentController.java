package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import jakarta.validation.Valid;

import com.iroomclass.springbackend.domain.exam.dto.exam.ExamDocumentCreateRequest;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamDocumentCreateResponse;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamDocumentDetailResponse;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamDocumentListResponse;
import com.iroomclass.springbackend.domain.exam.service.ExamDocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험지 문서 관리 컨트롤러
 * 
 * 시험지 문서 생성, 조회 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/exam-documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "시험지 문서 관리", description = "시험지 문서 생성, 조회 API")
public class ExamDocumentController {

    private final ExamDocumentService examDocumentService;

    /**
     * 시험지 문서 생성
     * 
     * @param request 시험지 문서 생성 요청
     * @return 생성된 시험지 문서 정보
     */
    @PostMapping
    @Operation(summary = "시험지 문서 생성", description = "시험지 초안을 기반으로 답안지, 문제지, 답안을 생성합니다. 기존 문서가 있다면 재생성됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험지 초안")
    })
    public ApiResponse<ExamDocumentCreateResponse> createExamDocuments(
            @Valid @RequestBody ExamDocumentCreateRequest request) {
        log.info("시험지 문서 생성 요청: 시험지 ID={}", request.examSheetId());

        ExamDocumentCreateResponse response = examDocumentService.createExamDocuments(request);

        log.info("시험지 문서 생성 성공: 시험지 ID={}, 문서={}개",
                response.examSheetId(), response.documentCount());

        return ApiResponse.success("시험 문서 관리 성공", response);
    }

    /**
     * 시험지별 문서 목록 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 해당 시험지의 문서 목록
     */
    @GetMapping("/sheet/{examSheetId}")
    @Operation(summary = "시험지별 문서 목록 조회", description = "특정 시험지의 모든 문서 목록을 조회합니다. 답안지, 문제지, 답안을 포함합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험지 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험지")
    })
    public ApiResponse<ExamDocumentListResponse> getExamDocumentsBySheet(
            @Parameter(description = "시험지 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examSheetId) {
        log.info("시험지 {} 문서 목록 조회 요청", examSheetId);

        ExamDocumentListResponse response = examDocumentService.getExamDocumentsBySheet(examSheetId);

        log.info("시험지 {} 문서 목록 조회 성공: {}개", examSheetId, response.totalCount());

        return ApiResponse.success("시험 문서 관리 성공", response);
    }

    /**
     * 시험지 문서 상세 조회
     * 
     * @param documentId 문서 ID
     * @return 시험지 문서 상세 정보
     */
    @GetMapping("/{documentId}")
    @Operation(summary = "시험지 문서 상세 조회", description = "특정 시험지 문서의 상세 정보를 조회합니다. HTML 형태의 문서 내용을 포함합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 문서 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험지 문서")
    })
    public ApiResponse<ExamDocumentDetailResponse> getExamDocumentDetail(
            @Parameter(description = "문서 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID documentId) {
        log.info("시험지 문서 {} 상세 조회 요청", documentId);

        ExamDocumentDetailResponse response = examDocumentService.getExamDocumentDetail(documentId);

        log.info("시험지 문서 {} 상세 조회 성공: 타입={}", documentId, response.documentTypeName());

        return ApiResponse.success("시험 문서 관리 성공", response);
    }

    /**
     * 시험지 문서 삭제 (시험지 목록에서 삭제)
     * 
     * @param examSheetId 시험지 ID
     * @return 삭제 성공 여부
     */
    @DeleteMapping("/sheet/{examSheetId}")
    @Operation(summary = "시험지 문서 삭제", description = "시험지와 연관된 모든 문서(답안지, 문제지, 답안)를 삭제합니다. 시험지 목록에서 제거됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험지 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험지")
    })
    public ApiResponse<String> deleteExamDocuments(
            @Parameter(description = "시험지 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examSheetId) {
        log.info("시험지 문서 삭제 요청: 시험지 ID={}", examSheetId);

        examDocumentService.deleteExamDocuments(examSheetId);

        log.info("시험지 문서 삭제 성공: 시험지 ID={}", examSheetId);

        return ApiResponse.success("성공", "삭제 성공");
    }
}
