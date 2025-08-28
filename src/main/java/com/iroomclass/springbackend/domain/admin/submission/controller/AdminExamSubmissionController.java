package com.iroomclass.springbackend.domain.admin.submission.controller;

import java.util.List;

import com.iroomclass.springbackend.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.admin.submission.dto.ExamSubmissionDetailResponse;
import com.iroomclass.springbackend.domain.admin.submission.dto.ExamSubmissionListResponse;
import com.iroomclass.springbackend.domain.admin.submission.service.AdminExamSubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 시험 제출 관리 컨트롤러
 * 
 * 관리자가 시험 제출 현황을 조회하고 관리할 수 있는 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/admin/exam-submission")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 - 시험 제출 관리", description = "관리자 시험 제출 조회, 관리 API")
public class AdminExamSubmissionController {
    
    private final AdminExamSubmissionService adminExamSubmissionService;
    
    /**
     * 시험별 제출 목록 조회
     * 
     * @param examId 시험 ID
     * @return 해당 시험의 제출 목록
     */
    @GetMapping("/exam/{examId}")
    @Operation(
        summary = "시험별 제출 목록 조회",
        description = "특정 시험의 모든 학생 제출 현황을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험 ID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험")
    })
    public ApiResponse<ExamSubmissionListResponse> getExamSubmissions(
            @Parameter(description = "시험 ID", example = "1") 
            @PathVariable Long examId) {
        log.info("관리자 - 시험별 제출 목록 조회 요청: 시험 ID={}", examId);
        
        ExamSubmissionListResponse response = adminExamSubmissionService.getExamSubmissions(examId);
        
        log.info("관리자 - 시험별 제출 목록 조회 성공: 시험={}, 제출={}개", response.examName(), response.totalCount());
        
        return ApiResponse.success("성공", response);
    }
    
    /**
     * 시험 제출 상세 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 시험 제출 상세 정보
     */
    @GetMapping("/{submissionId}")
    @Operation(
        summary = "시험 제출 상세 조회",
        description = "특정 시험 제출의 상세 정보를 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 제출 ID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험 제출")
    })
    public ApiResponse<ExamSubmissionDetailResponse> getExamSubmissionDetail(
            @Parameter(description = "시험 제출 ID", example = "1") 
            @PathVariable Long submissionId) {
        log.info("관리자 - 시험 제출 상세 조회 요청: 제출 ID={}", submissionId);
        
        ExamSubmissionDetailResponse response = adminExamSubmissionService.getExamSubmissionDetail(submissionId);
        
        log.info("관리자 - 시험 제출 상세 조회 성공: 학생={}, 시험={}", response.studentName(), response.examName());
        
        return ApiResponse.success("성공", response);
    }
    
    /**
     * 학생별 제출 목록 조회
     * 
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 해당 학생의 제출 목록
     */
    @GetMapping("/student")
    @Operation(
        summary = "학생별 제출 목록 조회",
        description = "특정 학생의 모든 시험 제출 이력을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력값")
    })
    public ApiResponse<List<ExamSubmissionDetailResponse>> getStudentSubmissions(
            @Parameter(description = "학생 이름", example = "김철수") 
            @RequestParam String studentName,
            @Parameter(description = "학생 전화번호", example = "010-1234-5678") 
            @RequestParam String studentPhone) {
        log.info("관리자 - 학생별 제출 목록 조회 요청: 학생={}, 전화번호={}", studentName, studentPhone);
        
        List<ExamSubmissionDetailResponse> response = adminExamSubmissionService.getStudentSubmissions(studentName, studentPhone);
        
        log.info("관리자 - 학생별 제출 목록 조회 성공: 학생={}, 제출={}개", studentName, response.size());
        
        return ApiResponse.success("성공", response);
    }
    
    /**
     * 시험별 제출 학생 수 조회
     * 
     * @param examId 시험 ID
     * @return 해당 시험의 제출 학생 수
     */
    @GetMapping("/exam/{examId}/count")
    @Operation(
        summary = "시험별 제출 학생 수 조회",
        description = "특정 시험에 제출한 학생 수를 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험 ID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험")
    })
    public ApiResponse<Long> getExamSubmissionCount(
            @Parameter(description = "시험 ID", example = "1") 
            @PathVariable Long examId) {
        log.info("관리자 - 시험별 제출 학생 수 조회 요청: 시험 ID={}", examId);
        
        long count = adminExamSubmissionService.getExamSubmissionCount(examId);
        
        log.info("관리자 - 시험별 제출 학생 수 조회 성공: 시험 ID={}, 제출 학생 수={}", examId, count);
        
        return ApiResponse.success("성공", count);
    }
}
