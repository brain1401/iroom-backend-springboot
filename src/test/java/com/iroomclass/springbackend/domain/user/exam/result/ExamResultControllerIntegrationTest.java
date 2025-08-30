package com.iroomclass.springbackend.domain.user.exam.result;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroomclass.springbackend.domain.admin.info.entity.Admin;
import com.iroomclass.springbackend.domain.admin.info.repository.AdminRepository;
import com.iroomclass.springbackend.domain.user.exam.answer.entity.ExamAnswer;
import com.iroomclass.springbackend.domain.user.exam.answer.repository.ExamAnswerRepository;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.user.exam.result.dto.CompleteGradingRequest;
import com.iroomclass.springbackend.domain.user.exam.result.dto.StartGradingRequest;
import com.iroomclass.springbackend.domain.user.exam.result.dto.StartRegradingRequest;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult;
import com.iroomclass.springbackend.domain.user.exam.result.repository.ExamResultRepository;
import com.iroomclass.springbackend.domain.user.exam.result.service.ExamResultService;

/**
 * 시험 결과 컨트롤러 통합 테스트
 * 
 * REST API 엔드포인트의 동작을 검증합니다.
 * HTTP 요청/응답, 데이터 검증, 예외 처리 등을 테스트합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
class ExamResultControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ExamResultService examResultService;
    
    @Autowired
    private ExamResultRepository examResultRepository;
    
    @Autowired
    private ExamSubmissionRepository examSubmissionRepository;
    
    @Autowired
    private ExamAnswerRepository examAnswerRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    private ExamSubmission testSubmission;
    private Admin testAdmin;
    private List<ExamAnswer> testAnswers;
    
    @BeforeEach
    void setUp() {
        // 테스트용 데이터 생성
        testSubmission = createTestSubmission();
        examSubmissionRepository.save(testSubmission);
        
        testAdmin = createTestAdmin();
        adminRepository.save(testAdmin);
        
        testAnswers = createTestAnswers(testSubmission);
        examAnswerRepository.saveAll(testAnswers);
    }
    
    @Test
    @DisplayName("자동 채점 시작 API 테스트")
    void testStartAutoGrading() throws Exception {
        // Given
        StartGradingRequest request = new StartGradingRequest(
            testSubmission.getId(),
            null,
            true
        );
        
        // When & Then
        mockMvc.perform(post("/api/exam-results/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("자동 채점이 시작되었습니다")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.submissionId", is(testSubmission.getId().toString())))
                .andExpect(jsonPath("$.data.isAutoGrading", is(true)))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")));
    }
    
    @Test
    @DisplayName("수동 채점 시작 API 테스트")
    void testStartManualGrading() throws Exception {
        // Given
        StartGradingRequest request = new StartGradingRequest(
            testSubmission.getId(),
            testAdmin.getId(),
            false
        );
        
        // When & Then
        mockMvc.perform(post("/api/exam-results/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("수동 채점이 시작되었습니다")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.submissionId", is(testSubmission.getId().toString())))
                .andExpect(jsonPath("$.data.isAutoGrading", is(false)))
                .andExpect(jsonPath("$.data.gradedBy.id", is(testAdmin.getId().toString())))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")));
    }
    
    @Test
    @DisplayName("재채점 시작 API 테스트")
    void testStartRegrading() throws Exception {
        // Given: 기존 채점 결과
        ExamResult originalResult = examResultService.startAutoGrading(testSubmission.getId());
        examResultService.completeGrading(originalResult.getId(), "최초 채점");
        
        StartRegradingRequest request = new StartRegradingRequest(
            originalResult.getId(),
            testAdmin.getId()
        );
        
        // When & Then
        mockMvc.perform(post("/api/exam-results/regrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("재채점이 시작되었습니다")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.version", is(2)))
                .andExpect(jsonPath("$.data.gradedBy.id", is(testAdmin.getId().toString())))
                .andExpect(jsonPath("$.data.status", is("PENDING")));
    }
    
    @Test
    @DisplayName("채점 완료 API 테스트")
    void testCompleteGrading() throws Exception {
        // Given: 진행 중인 채점
        ExamResult result = examResultService.startAutoGrading(testSubmission.getId());
        
        CompleteGradingRequest request = new CompleteGradingRequest(
            result.getId(),
            "채점 완료 코멘트"
        );
        
        // When & Then
        mockMvc.perform(put("/api/exam-results/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("채점이 완료되었습니다")));
    }
    
    @Test
    @DisplayName("시험 결과 조회 API 테스트")
    void testGetExamResult() throws Exception {
        // Given: 완료된 채점 결과
        ExamResult result = examResultService.startAutoGrading(testSubmission.getId());
        examResultService.completeGrading(result.getId(), "테스트 완료");
        
        // When & Then
        mockMvc.perform(get("/api/exam-results/{resultId}", result.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("시험 결과 조회 성공")))
                .andExpect(jsonPath("$.data.id", is(result.getId().toString())))
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.gradingComment", is("테스트 완료")))
                .andExpect(jsonPath("$.data.questionResults", hasSize(testAnswers.size())));
    }
    
    @Test
    @DisplayName("제출 ID로 최신 채점 결과 조회 API 테스트")
    void testGetLatestResultBySubmissionId() throws Exception {
        // Given: 여러 채점 결과
        ExamResult firstResult = examResultService.startAutoGrading(testSubmission.getId());
        examResultService.completeGrading(firstResult.getId(), "1차 채점");
        
        ExamResult secondResult = examResultService.startRegrading(firstResult.getId(), testAdmin.getId());
        examResultService.completeGrading(secondResult.getId(), "2차 재채점");
        
        // When & Then
        mockMvc.perform(get("/api/exam-results/submission/{submissionId}/latest", testSubmission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("최신 채점 결과 조회 성공")))
                .andExpect(jsonPath("$.data.id", is(secondResult.getId().toString())))
                .andExpect(jsonPath("$.data.version", is(2)))
                .andExpect(jsonPath("$.data.gradingComment", is("2차 재채점")));
    }
    
    @Test
    @DisplayName("채점 히스토리 조회 API 테스트")
    void testGetResultHistoryBySubmissionId() throws Exception {
        // Given: 여러 채점 결과
        ExamResult firstResult = examResultService.startAutoGrading(testSubmission.getId());
        examResultService.completeGrading(firstResult.getId(), "1차 채점");
        
        ExamResult secondResult = examResultService.startRegrading(firstResult.getId(), testAdmin.getId());
        examResultService.completeGrading(secondResult.getId(), "2차 재채점");
        
        // When & Then
        mockMvc.perform(get("/api/exam-results/submission/{submissionId}/history", testSubmission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("채점 히스토리 조회 성공")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].version", is(2))) // 최신순 정렬
                .andExpect(jsonPath("$.data[1].version", is(1)));
    }
    
    @Test
    @DisplayName("채점 상태별 조회 API 테스트")
    void testGetExamResultsByStatus() throws Exception {
        // Given: 다양한 상태의 결과들
        ExamResult pendingResult = examResultService.startManualGrading(testSubmission.getId(), testAdmin.getId());
        ExamResult completedResult = examResultService.startAutoGrading(testSubmission.getId());
        examResultService.completeGrading(completedResult.getId(), "완료");
        
        // When & Then: 완료된 결과 조회
        mockMvc.perform(get("/api/exam-results")
                .param("status", "COMPLETED")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("채점 결과 목록 조회 성공")))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].status", is("COMPLETED")));
        
        // When & Then: 진행 중인 결과 조회
        mockMvc.perform(get("/api/exam-results")
                .param("status", "IN_PROGRESS")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].status", is("IN_PROGRESS")));
    }
    
    @Test
    @DisplayName("자동 채점 결과 조회 API 테스트")
    void testGetAutoGradedResults() throws Exception {
        // Given
        examResultService.startAutoGrading(testSubmission.getId());
        examResultService.startManualGrading(testSubmission.getId(), testAdmin.getId());
        
        // When & Then
        mockMvc.perform(get("/api/exam-results")
                .param("isAutoGrading", "true")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].isAutoGrading", is(true)));
    }
    
    @Test
    @DisplayName("재채점 결과 조회 API 테스트")
    void testGetRegradedResults() throws Exception {
        // Given: 재채점 결과
        ExamResult originalResult = examResultService.startAutoGrading(testSubmission.getId());
        examResultService.completeGrading(originalResult.getId(), "최초 채점");
        examResultService.startRegrading(originalResult.getId(), testAdmin.getId());
        
        // When & Then
        mockMvc.perform(get("/api/exam-results/regraded")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("재채점 결과 조회 성공")))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].isRegraded", is(true)));
    }
    
    @Test
    @DisplayName("채점 상태별 통계 조회 API 테스트")
    void testGetStatusStatistics() throws Exception {
        // Given: 다양한 상태의 결과들
        examResultService.startManualGrading(testSubmission.getId(), testAdmin.getId()); // IN_PROGRESS
        ExamResult completed = examResultService.startAutoGrading(testSubmission.getId());
        examResultService.completeGrading(completed.getId(), "완료"); // COMPLETED
        
        // When & Then
        mockMvc.perform(get("/api/exam-results/statistics/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("채점 상태별 통계 조회 성공")))
                .andExpect(jsonPath("$.data.IN_PROGRESS", is(1)))
                .andExpect(jsonPath("$.data.COMPLETED", is(1)))
                .andExpect(jsonPath("$.data.AUTO_GRADED", is(1)));
    }
    
    @Test
    @DisplayName("시험 결과 삭제 API 테스트")
    void testDeleteExamResult() throws Exception {
        // Given
        ExamResult result = examResultService.startAutoGrading(testSubmission.getId());
        
        // When & Then
        mockMvc.perform(delete("/api/exam-results/{resultId}", result.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("시험 결과가 삭제되었습니다")));
    }
    
    @Test
    @DisplayName("잘못된 요청 데이터 검증 테스트")
    void testValidationErrors() throws Exception {
        // Given: 잘못된 요청 데이터
        StartGradingRequest invalidRequest = new StartGradingRequest(
            null, // submissionId가 null
            testAdmin.getId(),
            false
        );
        
        // When & Then
        mockMvc.perform(post("/api/exam-results/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("존재하지 않는 리소스 조회 테스트")
    void testNotFoundErrors() throws Exception {
        // Given: 존재하지 않는 ID
        UUID nonExistentId = UUID.randomUUID();
        
        // When & Then
        mockMvc.perform(get("/api/exam-results/{resultId}", nonExistentId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("자동 채점 요청 검증 테스트")
    void testAutoGradingRequestValidation() throws Exception {
        // Given: 자동 채점인데 채점자 ID가 있는 경우
        StartGradingRequest invalidRequest = new StartGradingRequest(
            testSubmission.getId(),
            testAdmin.getId(), // 자동 채점인데 채점자 ID 존재
            true
        );
        
        // When & Then
        mockMvc.perform(post("/api/exam-results/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("수동 채점 요청 검증 테스트")
    void testManualGradingRequestValidation() throws Exception {
        // Given: 수동 채점인데 채점자 ID가 없는 경우
        StartGradingRequest invalidRequest = new StartGradingRequest(
            testSubmission.getId(),
            null, // 수동 채점인데 채점자 ID 없음
            false
        );
        
        // When & Then
        mockMvc.perform(post("/api/exam-results/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    /**
     * 테스트용 시험 제출 생성
     */
    private ExamSubmission createTestSubmission() {
        return ExamSubmission.builder()
            .userId(UUID.randomUUID())
            .examId(UUID.randomUUID())
            .submittedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * 테스트용 관리자 생성
     */
    private Admin createTestAdmin() {
        return Admin.builder()
            .name("테스트 선생님")
            .email("teacher@test.com")
            .password("password123")
            .build();
    }
    
    /**
     * 테스트용 답안들 생성
     */
    private List<ExamAnswer> createTestAnswers(ExamSubmission submission) {
        return List.of(
            ExamAnswer.builder()
                .examSubmission(submission)
                .questionId(UUID.randomUUID())
                .questionOrder(1)
                .submittedAnswer("답안 1")
                .answerType(ExamAnswer.AnswerType.TEXT)
                .maxScore(5)
                .build(),
            ExamAnswer.builder()
                .examSubmission(submission)
                .questionId(UUID.randomUUID())
                .questionOrder(2)
                .submittedAnswer("답안 2")
                .answerType(ExamAnswer.AnswerType.TEXT)
                .maxScore(5)
                .build(),
            ExamAnswer.builder()
                .examSubmission(submission)
                .questionId(UUID.randomUUID())
                .questionOrder(3)
                .submittedAnswer("정답")
                .answerType(ExamAnswer.AnswerType.MULTIPLE_CHOICE)
                .maxScore(5)
                .correctAnswer("정답")
                .build()
        );
    }
}