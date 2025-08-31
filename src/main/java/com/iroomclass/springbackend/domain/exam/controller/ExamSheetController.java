package com.iroomclass.springbackend.domain.exam.controller;

import com.iroomclass.springbackend.common.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.iroomclass.springbackend.domain.exam.dto.ExamSheetCreateRequest;
import com.iroomclass.springbackend.domain.exam.dto.ExamSheetCreateResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamSheetDetailResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamSheetListResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamSheetQuestionManageResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamSheetPreviewResponse;
import com.iroomclass.springbackend.domain.exam.dto.ExamSheetUpdateRequest;
import com.iroomclass.springbackend.domain.exam.dto.QuestionReplaceRequest;
import com.iroomclass.springbackend.domain.exam.dto.QuestionSelectionRequest;
import com.iroomclass.springbackend.domain.exam.dto.SelectableQuestionsResponse;
import com.iroomclass.springbackend.domain.exam.service.ExamSheetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험지 관리 컨트롤러
 * 
 * 시험지 생성, 조회, 수정, 삭제 API를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@RestController
@RequestMapping("/admin/exam-sheets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 - 시험지 관리", description = "시험지 생성, 조회, 수정, 삭제 API")
public class ExamSheetController {

    private final ExamSheetService examSheetService;

    /**
     * 시험지 생성
     * 
     * @param request 시험지 생성 요청
     * @return 생성된 시험지 정보
     */
    @PostMapping
    @Operation(summary = "시험지 생성", description = "학년, 단원, 문제 개수를 선택하여 시험지를 생성합니다. 선택된 단원들에서 랜덤으로 문제를 선택합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 단원")
    })
    public ApiResponse<ExamSheetCreateResponse> createExamSheet(@Valid @RequestBody ExamSheetCreateRequest request) {
        log.info("시험지 생성 요청: 학년={}, 단원={}개, 문제={}개",
                request.grade(), request.unitIds().size(), request.totalQuestions());

        ExamSheetCreateResponse response = examSheetService.createExamSheet(request);

        log.info("시험지 생성 성공: ID={}, 이름={}", response.examSheetId(), response.examName());

        return ApiResponse.success("성공", response);
    }
    
    /**
     * 시험지 목록 필터링 조회 (고급)
     * 
     * 학년별, 상태별, 기간별 등 다양한 조건으로 시험지를 필터링하여 조회합니다.
     * 
     * @param grade 학년 필터 (선택사항)
     * @param minQuestions 최소 문제 개수 필터 (선택사항) 
     * @param maxQuestions 최대 문제 개수 필터 (선택사항)
     * @param createdAfter 생성일 이후 필터 (yyyy-MM-dd 형식, 선택사항)
     * @param createdBefore 생성일 이전 필터 (yyyy-MM-dd 형식, 선택사항)
     * @param examNameKeyword 시험지명 키워드 검색 (선택사항)
     * @param sortBy 정렬 기준 (createdAt, examName, totalQuestions, grade)
     * @param sortDirection 정렬 방향 (asc, desc)
     * @return 필터링된 시험지 목록
     */
    @GetMapping("/search")
    @Operation(
        summary = "시험지 고급 필터링 조회",
        description = """
            다양한 조건을 조합하여 시험지 목록을 필터링합니다.
            
            지원 필터:
            - 학년별 필터링 (1, 2, 3학년)
            - 문제 개수 범위 필터링 (최소/최대 문제 개수)
            - 생성일 기간 필터링 (특정 기간 내 생성된 시험지)
            - 시험지명 키워드 검색 (부분 일치 검색)
            - 다양한 정렬 옵션 (생성일, 이름, 문제수, 학년)
            
            활용 예시:
            - 최근 1개월 내 생성된 2학년 시험지
            - 문제 개수가 15-25개인 중간고사 시험지
            - "기말" 키워드가 포함된 모든 시험지
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 필터 조건")
    })
    public ApiResponse<ExamSheetListResponse> getFilteredExamSheets(
            @Parameter(description = "학년 필터", example = "2")
            @RequestParam(required = false) Integer grade,
            
            @Parameter(description = "최소 문제 개수", example = "10")  
            @RequestParam(required = false) Integer minQuestions,
            
            @Parameter(description = "최대 문제 개수", example = "30")
            @RequestParam(required = false) Integer maxQuestions,
            
            @Parameter(description = "생성일 이후 (yyyy-MM-dd)", example = "2024-01-01")
            @RequestParam(required = false) String createdAfter,
            
            @Parameter(description = "생성일 이전 (yyyy-MM-dd)", example = "2024-12-31")
            @RequestParam(required = false) String createdBefore,
            
            @Parameter(description = "시험지명 키워드", example = "중간고사")
            @RequestParam(required = false) String examNameKeyword,
            
            @Parameter(description = "정렬 기준", example = "createdAt", 
                      schema = @Schema(allowableValues = {"createdAt", "examName", "totalQuestions", "grade"}))
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "정렬 방향", example = "desc",
                      schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("시험지 필터링 조회 요청: grade={}, 문제수={}-{}, 기간={}-{}, 키워드={}, 정렬={}:{}", 
                grade, minQuestions, maxQuestions, createdAfter, createdBefore, 
                examNameKeyword, sortBy, sortDirection);
        
        ExamSheetListResponse response = examSheetService.getFilteredExamSheets(
                grade, minQuestions, maxQuestions, createdAfter, createdBefore,
                examNameKeyword, sortBy, sortDirection);
        
        log.info("시험지 필터링 조회 성공: {}개 조회됨", response.totalCount());
        
        return ApiResponse.success("시험지 필터링 조회 성공", response);
    }

    /**
     * 전체 시험지 목록 조회
     * 
     * @return 모든 시험지 목록 (최신순)
     */
    @GetMapping("/all")
    @Operation(summary = "전체 시험지 목록 조회", description = "모든 학년의 시험지 목록을 최신순으로 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<ExamSheetListResponse> getAllExamSheets() {
        log.info("전체 시험지 목록 조회 요청");

        ExamSheetListResponse response = examSheetService.getAllExamSheets();

        log.info("전체 시험지 목록 조회 성공: {}개", response.totalCount());

        return ApiResponse.success("성공", response);
    }

    /**
     * 학년별 시험지 목록 조회
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 해당 학년의 시험지 목록
     */
    @GetMapping("/grade/{grade}")
    @Operation(summary = "학년별 시험지 목록 조회", description = "특정 학년의 모든 시험지 목록을 조회합니다. 최신순으로 정렬됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 학년")
    })
    public ApiResponse<ExamSheetListResponse> getExamSheetsByGrade(
            @Parameter(description = "학년 (1/2/3)", example = "2") @PathVariable int grade) {
        log.info("학년 {} 시험지 목록 조회 요청", grade);

        ExamSheetListResponse response = examSheetService.getExamSheetsByGrade(grade);

        log.info("학년 {} 시험지 목록 조회 성공: {}개", grade, response.totalCount());

        return ApiResponse.success("성공", response);
    }

    /**
     * 시험지 상세 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 시험지 상세 정보
     */
    @GetMapping("/{examSheetId}")
    @Operation(summary = "시험지 상세 조회", description = "특정 시험지의 상세 정보를 조회합니다. 선택된 단원들과 문제들을 포함합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 시험지 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험지")
    })
    public ApiResponse<ExamSheetDetailResponse> getExamSheetDetail(
            @Parameter(description = "시험지 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examSheetId) {
        log.info("시험지 {} 상세 조회 요청", examSheetId);

        ExamSheetDetailResponse response = examSheetService.getExamSheetDetail(examSheetId);

        log.info("시험지 {} 상세 조회 성공: 단원={}개, 문제={}개",
                examSheetId, response.units().size(), response.questions().size());

        return ApiResponse.success("성공", response);
    }

    /**
     * 시험지 수정 (문제 교체)
     * 
     * @param examSheetId 시험지 ID
     * @param request     수정 요청
     * @return 수정된 시험지 정보
     */
    @PutMapping("/{examSheetId}")
    @Operation(summary = "시험지 수정 (문제 교체)", description = "시험지의 특정 문제를 같은 단원의 다른 문제로 교체합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 시험지 또는 문제")
    })
    public ApiResponse<ExamSheetDetailResponse> updateExamSheet(
            @Parameter(description = "시험지 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examSheetId,
            @Valid @RequestBody ExamSheetUpdateRequest request) {
        log.info("시험지 {} 수정 요청: 문제={}번 교체", examSheetId, request.seqNo());

        ExamSheetDetailResponse response = examSheetService.updateExamSheet(examSheetId, request);

        log.info("시험지 {} 수정 성공: 문제={}번 교체 완료", examSheetId, request.seqNo());

        return ApiResponse.success("성공", response);
    }
    
    /**
     * 시험지 문제 교체 (직접 선택)
     * 
     * 문제 직접 선택 시스템에서 특정 문제를 다른 문제로 교체합니다.
     * 기존 문제의 순서와 배점은 그대로 유지됩니다.
     * 
     * @param examSheetId 시험지 ID
     * @param request 문제 교체 요청
     * @return 교체 완료된 시험지 정보
     */
    @PostMapping("/{examSheetId}/questions/replace")
    @Operation(
        summary = "시험지 문제 교체",
        description = """
            특정 문제를 다른 문제로 교체합니다.
            
            문제 직접 선택 시스템에서 사용됩니다.
            기존 문제의 순서와 배점은 그대로 유지되며, 문제 내용만 교체됩니다.
            
            제약 조건:
            - 기존 문제는 시험지에 포함되어 있어야 함
            - 새로운 문제는 존재해야 하고, 시험지에 중복되지 않아야 함
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "교체 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (중복 문제, 존재하지 않는 문제 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험지 또는 문제를 찾을 수 없음")
    })
    public ApiResponse<ExamSheetDetailResponse> replaceQuestion(
            @Parameter(description = "시험지 ID", example = "1", required = true) 
            @PathVariable UUID examSheetId,
            @Valid @RequestBody QuestionReplaceRequest request) {
        
        log.info("시험지 {} 문제 교체 요청: {} → {}", 
                examSheetId, request.oldQuestionId(), request.newQuestionId());
        
        ExamSheetDetailResponse response = examSheetService.replaceQuestion(examSheetId, request);
        
        log.info("시험지 {} 문제 교체 성공: {} → {}", 
                examSheetId, request.oldQuestionId(), request.newQuestionId());
        
        return ApiResponse.success("문제 교체가 완료되었습니다", response);
    }

    /**
     * 선택 가능한 문제 목록 조회
     * 
     * 시험지에 추가할 수 있는 문제들을 단원별, 난이도별, 유형별로 필터링하여 조회합니다.
     * 이미 시험지에 포함된 문제들은 표시되지만 선택 불가능으로 표시됩니다.
     * 
     * @param examSheetId 시험지 ID
     * @param unitId 단원 ID (선택)
     * @param difficulty 난이도 (선택) - "하", "중", "상"
     * @param questionType 문제 유형 (선택) - "MULTIPLE_CHOICE", "SUBJECTIVE"
     * @return 선택 가능한 문제 목록
     */
    @GetMapping("/{examSheetId}/selectable-questions")
    @Operation(
        summary = "선택 가능한 문제 목록 조회",
        description = """
            시험지에 추가할 수 있는 문제들을 조회합니다.
            
            필터링 옵션:
            - unitId: 특정 단원의 문제만 조회
            - difficulty: 난이도별 필터링 (하/중/상)
            - questionType: 문제 유형별 필터링 (MULTIPLE_CHOICE/SUBJECTIVE)
            
            이미 시험지에 포함된 문제는 alreadySelected=true로 표시됩니다.
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 매개변수"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험지를 찾을 수 없음")
    })
    public ApiResponse<SelectableQuestionsResponse> getSelectableQuestions(
            @Parameter(description = "시험지 ID", example = "1", required = true) 
            @PathVariable UUID examSheetId,
            @Parameter(description = "단원 ID", example = "550e8400-e29b-41d4-a716-446655440000") 
            @RequestParam(required = false) UUID unitId,
            @Parameter(description = "난이도", example = "중", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"하", "중", "상"})) 
            @RequestParam(required = false) String difficulty,
            @Parameter(description = "문제 유형", example = "SUBJECTIVE", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"MULTIPLE_CHOICE", "SUBJECTIVE"})) 
            @RequestParam(required = false) String questionType) {
        
        log.info("시험지 {} 선택 가능한 문제 목록 조회 요청: 단원={}, 난이도={}, 유형={}", 
                examSheetId, unitId, difficulty, questionType);
        
        SelectableQuestionsResponse response = examSheetService.getSelectableQuestions(
                examSheetId, unitId, difficulty, questionType);
        
        log.info("시험지 {} 선택 가능한 문제 목록 조회 성공: {}개 문제", 
                examSheetId, response.totalCount());
        
        return ApiResponse.success("선택 가능한 문제 목록 조회 성공", response);
    }

    /**
     * 시험지에 문제 추가
     * 
     * 문제 직접 선택 시스템에서 특정 문제를 시험지에 추가합니다.
     * 문제 순서와 배점을 지정할 수 있습니다.
     * 
     * @param examSheetId 시험지 ID
     * @param request 문제 선택 요청
     * @return 업데이트된 시험지 문제 관리 정보
     */
    @PostMapping("/{examSheetId}/questions")
    @Operation(
        summary = "시험지에 문제 추가",
        description = """
            선택한 문제를 시험지에 추가합니다.
            
            기능:
            - 시험지 마지막 순서에 문제 추가
            - 중복 문제 추가 방지
            - 배점 자동 계산 또는 수동 설정 가능
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (중복 문제, 존재하지 않는 문제 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험지 또는 문제를 찾을 수 없음")
    })
    public ApiResponse<ExamSheetQuestionManageResponse> addQuestionToExamSheet(
            @Parameter(description = "시험지 ID", example = "1", required = true) 
            @PathVariable UUID examSheetId,
            @Valid @RequestBody QuestionSelectionRequest request) {
        
        log.info("시험지 {} 문제 추가 요청: 문제={}, 배점={}, 순서={}", 
                examSheetId, request.questionId(), request.points());
        
        ExamSheetQuestionManageResponse response = examSheetService.addQuestionToExamSheet(
                examSheetId, request);
        
        log.info("시험지 {} 문제 추가 성공: 현재 문제 수={}/{}", 
                examSheetId, response.currentQuestionCount(), response.targetQuestionCount());
        
        return ApiResponse.success("문제가 시험지에 추가되었습니다", response);
    }

    /**
     * 시험지에서 문제 제거
     * 
     * 문제 직접 선택 시스템에서 시험지에 포함된 특정 문제를 제거합니다.
     * 제거 후 문제 순서가 자동으로 재조정됩니다.
     * 
     * @param examSheetId 시험지 ID
     * @param questionId 제거할 문제 ID
     * @return 업데이트된 시험지 문제 관리 정보
     */
    @DeleteMapping("/{examSheetId}/questions/{questionId}")
    @Operation(
        summary = "시험지에서 문제 제거",
        description = """
            시험지에서 특정 문제를 제거합니다.
            
            기능:
            - 문제 제거 후 나머지 문제들의 순서 자동 재조정
            - 총 배점 자동 재계산
            - 문제 유형별 개수 자동 업데이트
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "제거 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험지 또는 문제를 찾을 수 없음")
    })
    public ApiResponse<ExamSheetQuestionManageResponse> removeQuestionFromExamSheet(
            @Parameter(description = "시험지 ID", example = "1", required = true) 
            @PathVariable UUID examSheetId,
            @Parameter(description = "제거할 문제 ID", example = "123", required = true) 
            @PathVariable UUID questionId) {
        
        log.info("시험지 {} 문제 제거 요청: 문제={}", examSheetId, questionId);
        
        ExamSheetQuestionManageResponse response = examSheetService.removeQuestionFromExamSheet(
                examSheetId, questionId);
        
        log.info("시험지 {} 문제 제거 성공: 현재 문제 수={}/{}", 
                examSheetId, response.currentQuestionCount(), response.targetQuestionCount());
        
        return ApiResponse.success("문제가 시험지에서 제거되었습니다", response);
    }

    /**
     * 시험지 문제 관리 현황 조회
     * 
     * 현재 시험지에 포함된 문제들의 상세 정보와 관리 상태를 조회합니다.
     * 문제 순서, 배점, 유형별 개수 등을 확인할 수 있습니다.
     * 
     * @param examSheetId 시험지 ID
     * @return 시험지 문제 관리 현황
     */
    @GetMapping("/{examSheetId}/question-management")
    @Operation(
        summary = "시험지 문제 관리 현황 조회",
        description = """
            시험지의 문제 관리 현황을 상세히 조회합니다.
            
            제공 정보:
            - 현재 문제 수 vs 목표 문제 수
            - 문제 유형별 개수 (객관식/주관식)
            - 총 배점 및 문제별 배점
            - 각 문제의 상세 정보 (단원, 난이도, 선택 방식)
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험지를 찾을 수 없음")
    })
    public ApiResponse<ExamSheetQuestionManageResponse> getExamSheetQuestionManagement(
            @Parameter(description = "시험지 ID", example = "1", required = true) 
            @PathVariable UUID examSheetId) {
        
        log.info("시험지 {} 문제 관리 현황 조회 요청", examSheetId);
        
        ExamSheetQuestionManageResponse response = examSheetService.getExamSheetQuestionManagement(
                examSheetId);
        
        log.info("시험지 {} 문제 관리 현황 조회 성공: {}/{} 문제, {}점", 
                examSheetId, response.currentQuestionCount(), response.targetQuestionCount(), 
                response.totalPoints());
        
        return ApiResponse.success("시험지 문제 관리 현황 조회 성공", response);
    }
    
    /**
     * 시험지 미리보기 조회
     * 
     * 시험지의 전체 구성을 미리보기합니다.
     * 포함된 모든 문제들과 통계 정보를 함께 제공합니다.
     * 
     * @param examSheetId 시험지 ID
     * @return 시험지 미리보기 정보
     */
    @GetMapping("/{examSheetId}/preview")
    @Operation(
        summary = "시험지 미리보기",
        description = """
            시험지의 전체 구성을 미리보기합니다.
            
            제공 정보:
            - 시험지 기본 정보 (이름, 학년, 문제 개수)
            - 포함된 모든 문제의 상세 정보 (순서, 배점, 유형, 난이도)
            - 문제 타입별 통계 (객관식/주관식 배점, 난이도별 분포)
            - 단원별 문제 분포 현황
            
            활용:
            - 시험지 최종 검토
            - 출제 균형 확인
            - 배점 분포 검증
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "미리보기 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험지를 찾을 수 없음")
    })
    public ApiResponse<ExamSheetPreviewResponse> getExamSheetPreview(
            @Parameter(description = "시험지 ID", example = "1", required = true) 
            @PathVariable UUID examSheetId) {
        
        log.info("시험지 {} 미리보기 조회 요청", examSheetId);
        
        ExamSheetPreviewResponse response = examSheetService.getExamSheetPreview(examSheetId);
        
        log.info("시험지 {} 미리보기 조회 성공: {} 문제, {}점, 객관식 {}문제/주관식 {}문제", 
                examSheetId, response.totalQuestions(), response.totalPoints(),
                response.multipleChoiceCount(), response.subjectiveCount());
        
        return ApiResponse.success("시험지 미리보기 조회 성공", response);
    }
    
    /**
     * 시험지 문제 교체
     * 
     * 시험지에서 기존 문제를 다른 문제로 교체합니다.
     * 문제 순서는 유지되며, 배점은 조정할 수 있습니다.
     * 
     * @param examSheetId 시험지 ID
     * @param request 문제 교체 요청
     * @return 업데이트된 시험지 문제 관리 정보
     */
    @PutMapping("/{examSheetId}/questions/replace")
    @Operation(
        summary = "시험지 문제 교체",
        description = """
            시험지에서 기존 문제를 다른 문제로 교체합니다.
            
            기능:
            - 기존 문제의 순서와 위치 유지
            - 배점 조정 가능 (미지정 시 기존 배점 유지)
            - 문제 타입 검증 (객관식 ↔ 주관식 교체 허용)
            - 중복 문제 추가 방지
            - 교체 사유 기록 (선택사항)
            
            제약사항:
            - 동일한 문제로의 교체는 불가
            - 이미 시험지에 포함된 문제로의 교체는 불가
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "교체 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (중복 문제, 동일 문제 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험지 또는 문제를 찾을 수 없음")
    })
    public ApiResponse<ExamSheetQuestionManageResponse> replaceQuestionInExamSheet(
            @Parameter(description = "시험지 ID", example = "1", required = true) 
            @PathVariable UUID examSheetId,
            @Valid @RequestBody QuestionReplaceRequest request) {
        
        log.info("시험지 {} 문제 교체 요청: 기존 문제 {} → 새 문제 {}, 배점: {}", 
                examSheetId, request.oldQuestionId(), request.newQuestionId(), request.points());
        
        ExamSheetQuestionManageResponse response = examSheetService.replaceQuestionInExamSheet(
                examSheetId, request);
        
        log.info("시험지 {} 문제 교체 성공: 기존 {} → 새 {}, 현재 {}/{} 문제", 
                examSheetId, request.oldQuestionId(), request.newQuestionId(),
                response.currentQuestionCount(), response.targetQuestionCount());
        
        return ApiResponse.success("시험지 문제 교체 성공", response);
    }
}