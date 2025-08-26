package com.iroomclass.springbackend.domain.user.exam.answer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.user.exam.answer.dto.ExamAnswerCreateRequest;
import com.iroomclass.springbackend.domain.user.exam.answer.dto.ExamAnswerListResponse;
import com.iroomclass.springbackend.domain.user.exam.answer.dto.ExamAnswerResponse;
import com.iroomclass.springbackend.domain.user.exam.answer.dto.ExamAnswerUpdateRequest;
import com.iroomclass.springbackend.domain.user.exam.answer.service.ExamAnswerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 답안 관리 컨트롤러
 * 
 * 학생이 시험 답안을 생성, 수정, 조회할 수 있는 API를 제공합니다.
 * AI 이미지 인식과 연동하여 답안을 처리합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/user/exam-answer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "학생 시험 답안 관리", description = "시험 답안 생성, 수정, 조회 API")
public class ExamAnswerController {
    
    private final ExamAnswerService examAnswerService;
    
    /**
     * 답안 생성 (AI 이미지 인식 포함)
     * 
     * @param request 답안 생성 요청
     * @return 생성된 답안 정보
     */
    @PostMapping
    @Operation(
        summary = "답안 생성",
        description = "답안 이미지를 업로드하고 AI 인식을 수행합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값 또는 이미 존재하는 답안"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 시험 제출 또는 문제")
    })
    public ResponseEntity<ExamAnswerResponse> createExamAnswer(@RequestBody ExamAnswerCreateRequest request) {
        log.info("답안 생성 요청: 제출 ID={}, 문제 ID={}", request.getExamSubmissionId(), request.getQuestionId());
        
        ExamAnswerResponse response = examAnswerService.createExamAnswer(request);
        
        log.info("답안 생성 성공: 답안 ID={}", response.getAnswerId());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 답안 수정 (재촬영)
     * 
     * @param answerId 답안 ID
     * @param newImageUrl 새로운 이미지 URL
     * @return 수정된 답안 정보
     */
    @PutMapping("/{answerId}/retake")
    @Operation(
        summary = "답안 재촬영",
        description = "답안 이미지를 다시 촬영하고 AI 인식을 재수행합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재촬영 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 답안 ID"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 답안")
    })
    public ResponseEntity<ExamAnswerResponse> retakeExamAnswer(
            @Parameter(description = "답안 ID", example = "1") 
            @PathVariable Long answerId,
            @Parameter(description = "새로운 이미지 URL", example = "/uploads/answers/answer_1_retake.jpg") 
            @RequestParam String newImageUrl) {
        log.info("답안 재촬영 요청: 답안 ID={}, 새 이미지 URL={}", answerId, newImageUrl);
        
        ExamAnswerResponse response = examAnswerService.retakeExamAnswer(answerId, newImageUrl);
        
        log.info("답안 재촬영 성공: 답안 ID={}", response.getAnswerId());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 답안 수정 (텍스트 수정)
     * 
     * @param request 답안 수정 요청
     * @return 수정된 답안 정보
     */
    @PutMapping("/update")
    @Operation(
        summary = "답안 텍스트 수정",
        description = "AI 인식 결과를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 답안")
    })
    public ResponseEntity<ExamAnswerResponse> updateExamAnswer(@RequestBody ExamAnswerUpdateRequest request) {
        log.info("답안 수정 요청: 답안 ID={}", request.getAnswerId());
        
        ExamAnswerResponse response = examAnswerService.updateExamAnswer(request);
        
        log.info("답안 수정 성공: 답안 ID={}", response.getAnswerId());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 답안 목록 조회
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 해당 시험 제출의 모든 답안 목록
     */
    @GetMapping("/submission/{examSubmissionId}")
    @Operation(
        summary = "답안 목록 조회",
        description = "시험 제출의 모든 답안을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 시험 제출 ID")
    })
    public ResponseEntity<ExamAnswerListResponse> getExamAnswers(
            @Parameter(description = "시험 제출 ID", example = "1") 
            @PathVariable Long examSubmissionId) {
        log.info("답안 목록 조회 요청: 시험 제출 ID={}", examSubmissionId);
        
        ExamAnswerListResponse response = examAnswerService.getExamAnswers(examSubmissionId);
        
        log.info("답안 목록 조회 성공: 시험 제출 ID={}, 답안 수={}", examSubmissionId, response.getTotalCount());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 특정 문제 답안 조회
     * 
     * @param examSubmissionId 시험 제출 ID
     * @param questionId 문제 ID
     * @return 해당 문제의 답안 정보
     */
    @GetMapping("/submission/{examSubmissionId}/question/{questionId}")
    @Operation(
        summary = "특정 문제 답안 조회",
        description = "특정 문제의 답안을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 답안")
    })
    public ResponseEntity<ExamAnswerResponse> getExamAnswer(
            @Parameter(description = "시험 제출 ID", example = "1") 
            @PathVariable Long examSubmissionId,
            @Parameter(description = "문제 ID", example = "3") 
            @PathVariable Long questionId) {
        log.info("특정 문제 답안 조회 요청: 시험 제출 ID={}, 문제 ID={}", examSubmissionId, questionId);
        
        ExamAnswerResponse response = examAnswerService.getExamAnswer(examSubmissionId, questionId);
        
        log.info("특정 문제 답안 조회 성공: 답안 ID={}, 문제 ID={}", response.getAnswerId(), questionId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 답안 상태 확인
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 답안 상태 요약 정보
     */
    @GetMapping("/submission/{examSubmissionId}/status")
    @Operation(
        summary = "답안 상태 확인",
        description = "답안의 현재 상태를 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 시험 제출 ID")
    })
    public ResponseEntity<ExamAnswerService.AnswerStatusSummary> getAnswerStatusSummary(
            @Parameter(description = "시험 제출 ID", example = "1") 
            @PathVariable Long examSubmissionId) {
        log.info("답안 상태 확인 요청: 시험 제출 ID={}", examSubmissionId);
        
        ExamAnswerService.AnswerStatusSummary response = examAnswerService.getAnswerStatusSummary(examSubmissionId);
        
        log.info("답안 상태 확인 완료: 총 {}개, 정답 {}개", 
            response.getTotalCount(), response.getCorrectCount());
        
        return ResponseEntity.ok(response);
    }
}
