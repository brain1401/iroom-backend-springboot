package com.iroomclass.springbackend.domain.admin.question.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iroomclass.springbackend.domain.admin.question.dto.QuestionDetailResponse;
import com.iroomclass.springbackend.domain.admin.question.dto.QuestionListResponse;
import com.iroomclass.springbackend.domain.admin.question.dto.QuestionSearchResponse;
import com.iroomclass.springbackend.domain.admin.question.dto.QuestionStatisticsResponse;
import com.iroomclass.springbackend.domain.admin.question.service.QuestionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 문제 관리 컨트롤러
 * 
 * 문제 목록 조회, 상세 조회, 통계, 검색 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 - 문제 관리", description = "문제 목록 조회, 상세 조회, 통계, 검색 API")
public class QuestionController {
    
    private final QuestionService questionService;
    
    /**
     * 단원별 문제 목록 조회
     * 
     * @param unitId 단원 ID
     * @return 해당 단원의 문제 목록
     */
    @GetMapping("/unit/{unitId}")
    @Operation(
        summary = "단원별 문제 목록 조회",
        description = "특정 단원에 속한 모든 문제의 목록을 조회합니다. 문제 ID와 난이도 정보를 포함합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 단원 ID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "단원을 찾을 수 없음")
    })
    public ApiResponse<QuestionListResponse> getQuestionsByUnit(
            @Parameter(description = "단원 ID", example = "1") 
            @PathVariable Long unitId) {
        log.info("단원 {} 문제 목록 조회 요청", unitId);
        
        QuestionListResponse response = questionService.getQuestionsByUnit(unitId);
        
        log.info("단원 {} 문제 목록 조회 성공: {}개 문제", unitId, response.getTotalQuestions());
        
        return ApiResponse.success("조회 성공", response);
    }
    
    /**
     * 난이도별 문제 목록 조회
     * 
     * @param unitId 단원 ID
     * @param difficulty 난이도 (하, 중, 상)
     * @return 해당 단원의 특정 난이도 문제 목록
     */
    @GetMapping("/unit/{unitId}/difficulty")
    @Operation(
        summary = "난이도별 문제 목록 조회",
        description = "특정 단원의 특정 난이도(하/중/상) 문제 목록을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 단원 ID 또는 난이도"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "단원을 찾을 수 없음")
    })
    public ApiResponse<QuestionListResponse> getQuestionsByUnitAndDifficulty(
            @Parameter(description = "단원 ID", example = "1") 
            @PathVariable Long unitId,
            @Parameter(description = "난이도 (하/중/상)", example = "중") 
            @RequestParam String difficulty) {
        log.info("단원 {} 난이도 {} 문제 목록 조회 요청", unitId, difficulty);
        
        QuestionListResponse response = questionService.getQuestionsByUnitAndDifficulty(unitId, difficulty);
        
        log.info("단원 {} 난이도 {} 문제 목록 조회 성공: {}개 문제", unitId, difficulty, response.getTotalQuestions());
        
        return ApiResponse.success("조회 성공", response);
    }
    
    /**
     * 문제 상세 조회
     * 
     * @param questionId 문제 ID
     * @return 문제 상세 정보
     */
    @GetMapping("/{questionId}")
    @Operation(
        summary = "문제 상세 조회",
        description = "특정 문제의 상세 정보를 조회합니다. 문제 내용(HTML), 정답, 단원 정보를 포함합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 문제 ID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문제를 찾을 수 없음")
    })
    public ApiResponse<QuestionDetailResponse> getQuestionDetail(
            @Parameter(description = "문제 ID", example = "1") 
            @PathVariable Long questionId) {
        log.info("문제 {} 상세 조회 요청", questionId);
        
        QuestionDetailResponse response = questionService.getQuestionDetail(questionId);
        
        log.info("문제 {} 상세 조회 성공", questionId);
        
        return ApiResponse.success("조회 성공", response);
    }
    
    /**
     * 단원별 문제 통계 조회
     * 
     * @param unitId 단원 ID
     * @return 단원별 문제 통계
     */
    @GetMapping("/unit/{unitId}/statistics")
    @Operation(
        summary = "단원별 문제 통계 조회",
        description = "특정 단원의 문제 통계를 조회합니다. 전체 문제 수와 난이도별 문제 수를 제공합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 단원 ID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "단원을 찾을 수 없음")
    })
    public ApiResponse<QuestionStatisticsResponse> getQuestionStatisticsByUnit(
            @Parameter(description = "단원 ID", example = "1") 
            @PathVariable Long unitId) {
        log.info("단원 {} 문제 통계 조회 요청", unitId);
        
        QuestionStatisticsResponse response = questionService.getQuestionStatisticsByUnit(unitId);
        
        log.info("단원 {} 문제 통계 조회 성공: 총 {}개 (하: {}, 중: {}, 상: {})", 
            unitId, response.getTotalQuestions(), response.getEasyCount(), 
            response.getMediumCount(), response.getHardCount());
        
        return ApiResponse.success("조회 성공", response);
    }
    
    /**
     * 문제 검색
     * 
     * @param keyword 검색 키워드
     * @return 키워드가 포함된 문제 목록
     */
    @GetMapping("/search")
    @Operation(
        summary = "문제 검색",
        description = "키워드로 문제를 검색합니다. 문제 내용(HTML)에서 키워드를 포함하는 문제들을 찾습니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검색 키워드가 비어있음")
    })
    public ApiResponse<QuestionSearchResponse> searchQuestions(
            @Parameter(description = "검색 키워드", example = "정수") 
            @RequestParam String keyword) {
        log.info("문제 검색 요청: {}", keyword);
        
        QuestionSearchResponse response = questionService.searchQuestions(keyword);
        
        log.info("문제 검색 성공: {}개 결과", response.getTotalResults());
        
        return ApiResponse.success("조회 성공", response);
    }
}
