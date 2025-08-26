package com.iroomclass.springbackend.domain.user.exam.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.user.exam.dto.ExamSubmissionCreateRequest;
import com.iroomclass.springbackend.domain.user.exam.dto.ExamSubmissionCreateResponse;
import com.iroomclass.springbackend.domain.user.exam.dto.ExamSubmissionDetailResponse;
import com.iroomclass.springbackend.domain.user.exam.dto.ExamSubmissionListResponse;
import com.iroomclass.springbackend.domain.user.exam.service.ExamSubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 제출 관리 컨트롤러 (학생용)
 * 
 * 학생이 시험을 제출하고 조회할 수 있는 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/user/exam-submission")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "학생 시험 제출 관리", description = "학생 시험 제출 생성, 조회 API")
public class ExamSubmissionController {
    
    private final ExamSubmissionService examSubmissionService;
    
    /**
     * 시험 제출 생성
     * 
     * @param request 시험 제출 생성 요청
     * @return 생성된 시험 제출 정보
     */
    @PostMapping
    @Operation(
        summary = "시험 제출 생성",
        description = "학생이 시험을 제출할 때 사용됩니다. 중복 제출은 방지됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "제출 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값 또는 이미 제출한 시험"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 시험")
    })
    public ResponseEntity<ExamSubmissionCreateResponse> createExamSubmission(@RequestBody ExamSubmissionCreateRequest request) {
        log.info("시험 제출 생성 요청: 시험={}, 학생={}, 전화번호={}", 
            request.getExamId(), request.getStudentName(), request.getStudentPhone());
        
        ExamSubmissionCreateResponse response = examSubmissionService.createExamSubmission(request);
        
        log.info("시험 제출 생성 성공: ID={}, 학생={}, 시험={}", 
            response.getSubmissionId(), response.getStudentName(), response.getExamName());
        
        return ResponseEntity.ok(response);
    }
    
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 시험 ID"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 시험")
    })
    public ResponseEntity<ExamSubmissionListResponse> getExamSubmissions(
            @Parameter(description = "시험 ID", example = "1") 
            @PathVariable Long examId) {
        log.info("시험별 제출 목록 조회 요청: 시험 ID={}", examId);
        
        ExamSubmissionListResponse response = examSubmissionService.getExamSubmissions(examId);
        
        log.info("시험별 제출 목록 조회 성공: 시험={}, 제출={}개", response.getExamName(), response.getTotalCount());
        
        return ResponseEntity.ok(response);
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 제출 ID"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 시험 제출")
    })
    public ResponseEntity<ExamSubmissionDetailResponse> getExamSubmissionDetail(
            @Parameter(description = "시험 제출 ID", example = "1") 
            @PathVariable Long submissionId) {
        log.info("시험 제출 상세 조회 요청: 제출 ID={}", submissionId);
        
        ExamSubmissionDetailResponse response = examSubmissionService.getExamSubmissionDetail(submissionId);
        
        log.info("시험 제출 상세 조회 성공: 학생={}, 시험={}", response.getStudentName(), response.getExamName());
        
        return ResponseEntity.ok(response);
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값")
    })
    public ResponseEntity<List<ExamSubmissionDetailResponse>> getStudentSubmissions(
            @Parameter(description = "학생 이름", example = "김철수") 
            @RequestParam String studentName,
            @Parameter(description = "학생 전화번호", example = "010-1234-5678") 
            @RequestParam String studentPhone) {
        log.info("학생별 제출 목록 조회 요청: 학생={}, 전화번호={}", studentName, studentPhone);
        
        List<ExamSubmissionDetailResponse> response = examSubmissionService.getStudentSubmissions(studentName, studentPhone);
        
        log.info("학생별 제출 목록 조회 성공: 학생={}, 제출={}개", studentName, response.size());
        
        return ResponseEntity.ok(response);
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 시험 ID"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 시험")
    })
    public ResponseEntity<Long> getExamSubmissionCount(
            @Parameter(description = "시험 ID", example = "1") 
            @PathVariable Long examId) {
        log.info("시험별 제출 학생 수 조회 요청: 시험 ID={}", examId);
        
        long count = examSubmissionService.getExamSubmissionCount(examId);
        
        log.info("시험별 제출 학생 수 조회 성공: 시험 ID={}, 제출 학생 수={}", examId, count);
        
        return ResponseEntity.ok(count);
    }
}
