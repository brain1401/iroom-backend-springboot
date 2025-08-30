package com.iroomclass.springbackend.domain.user.exam.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.info.entity.Admin;
import com.iroomclass.springbackend.domain.admin.info.repository.AdminRepository;
import com.iroomclass.springbackend.domain.user.exam.answer.entity.ExamAnswer;
import com.iroomclass.springbackend.domain.user.exam.answer.repository.ExamAnswerRepository;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult.ResultStatus;
import com.iroomclass.springbackend.domain.user.exam.result.entity.QuestionResult;
import com.iroomclass.springbackend.domain.user.exam.result.repository.ExamResultRepository;
import com.iroomclass.springbackend.domain.user.exam.result.repository.QuestionResultRepository;
import com.iroomclass.springbackend.domain.user.exam.result.service.ExamResultService;
import com.iroomclass.springbackend.domain.user.exam.result.service.QuestionResultService;

/**
 * 시험 결과 통합 테스트
 * 
 * 시험 채점 시스템의 전체적인 통합 테스트를 수행합니다.
 * 자동 채점, 수동 채점, 재채점 플로우를 검증합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExamResultIntegrationTest {
    
    @Autowired
    private ExamResultService examResultService;
    
    @Autowired
    private QuestionResultService questionResultService;
    
    @Autowired
    private ExamResultRepository examResultRepository;
    
    @Autowired
    private QuestionResultRepository questionResultRepository;
    
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
        // 테스트용 시험 제출 데이터 생성
        testSubmission = createTestSubmission();
        examSubmissionRepository.save(testSubmission);
        
        // 테스트용 관리자 데이터 생성
        testAdmin = createTestAdmin();
        adminRepository.save(testAdmin);
        
        // 테스트용 답안 데이터 생성
        testAnswers = createTestAnswers(testSubmission);
        examAnswerRepository.saveAll(testAnswers);
    }
    
    @Test
    @DisplayName("자동 채점 전체 플로우 테스트")
    void testAutoGradingFlow() {
        // Given: 시험 제출이 있는 상태
        
        // When: 자동 채점 시작
        ExamResult result = examResultService.startAutoGrading(testSubmission.getId());
        
        // Then: 채점 결과가 생성되었는지 확인
        assertThat(result).isNotNull();
        assertThat(result.getExamSubmission().getId()).isEqualTo(testSubmission.getId());
        assertThat(result.isAutoGrading()).isTrue();
        assertThat(result.getStatus()).isEqualTo(ResultStatus.IN_PROGRESS);
        
        // 문제별 결과가 생성되었는지 확인
        List<QuestionResult> questionResults = questionResultService.findByExamResultId(result.getId());
        assertThat(questionResults).hasSize(testAnswers.size());
        
        // 자동 채점이 완료되었는지 확인
        boolean allGraded = questionResultService.isAllQuestionsGraded(result.getId());
        if (allGraded) {
            // When: 채점 완료 처리
            examResultService.completeGrading(result.getId(), "자동 채점 완료");
            
            // Then: 상태가 완료로 변경되었는지 확인
            ExamResult completedResult = examResultService.findById(result.getId());
            assertThat(completedResult.getStatus()).isEqualTo(ResultStatus.COMPLETED);
            assertThat(completedResult.getTotalScore()).isNotNull();
            assertThat(completedResult.getGradingComment()).isEqualTo("자동 채점 완료");
        }
    }
    
    @Test
    @DisplayName("수동 채점 전체 플로우 테스트")
    void testManualGradingFlow() {
        // Given: 시험 제출이 있는 상태
        
        // When: 수동 채점 시작
        ExamResult result = examResultService.startManualGrading(testSubmission.getId(), testAdmin.getId());
        
        // Then: 채점 결과가 생성되었는지 확인
        assertThat(result).isNotNull();
        assertThat(result.getExamSubmission().getId()).isEqualTo(testSubmission.getId());
        assertThat(result.getGradedBy().getId()).isEqualTo(testAdmin.getId());
        assertThat(result.isAutoGrading()).isFalse();
        assertThat(result.getStatus()).isEqualTo(ResultStatus.IN_PROGRESS);
        
        // 문제별 결과가 생성되었는지 확인
        List<QuestionResult> questionResults = questionResultService.findByExamResultId(result.getId());
        assertThat(questionResults).hasSize(testAnswers.size());
        
        // 각 문제에 대해 수동 채점 처리
        for (QuestionResult qr : questionResults) {
            questionResultService.processManualGrading(qr.getId(), 5, true, "정답입니다");
        }
        
        // When: 채점 완료 처리
        examResultService.completeGrading(result.getId(), "수동 채점 완료");
        
        // Then: 상태가 완료로 변경되었는지 확인
        ExamResult completedResult = examResultService.findById(result.getId());
        assertThat(completedResult.getStatus()).isEqualTo(ResultStatus.COMPLETED);
        assertThat(completedResult.getTotalScore()).isEqualTo(testAnswers.size() * 5); // 각 문제 5점
        assertThat(completedResult.getGradingComment()).isEqualTo("수동 채점 완료");
    }
    
    @Test
    @DisplayName("재채점 전체 플로우 테스트")
    void testRegradingFlow() {
        // Given: 기존 채점이 완료된 상태
        ExamResult originalResult = examResultService.startAutoGrading(testSubmission.getId());
        examResultService.completeGrading(originalResult.getId(), "최초 채점");
        
        // When: 재채점 시작
        ExamResult regradingResult = examResultService.startRegrading(originalResult.getId(), testAdmin.getId());
        
        // Then: 재채점 결과가 생성되었는지 확인
        assertThat(regradingResult).isNotNull();
        assertThat(regradingResult.getId()).isNotEqualTo(originalResult.getId());
        assertThat(regradingResult.getVersion()).isEqualTo(originalResult.getVersion() + 1);
        assertThat(regradingResult.getGradedBy().getId()).isEqualTo(testAdmin.getId());
        assertThat(regradingResult.getStatus()).isEqualTo(ResultStatus.PENDING);
        
        // 기존 결과가 REGRADED 상태로 변경되었는지 확인
        ExamResult updatedOriginal = examResultService.findById(originalResult.getId());
        assertThat(updatedOriginal.getStatus()).isEqualTo(ResultStatus.REGRADED);
        
        // 재채점 문제별 결과가 생성되었는지 확인
        List<QuestionResult> regradingQuestionResults = questionResultService.findByExamResultId(regradingResult.getId());
        assertThat(regradingQuestionResults).hasSize(testAnswers.size());
    }
    
    @Test
    @DisplayName("채점 히스토리 조회 테스트")
    void testGradingHistory() {
        // Given: 여러 번의 채점이 수행된 상태
        ExamResult firstResult = examResultService.startAutoGrading(testSubmission.getId());
        examResultService.completeGrading(firstResult.getId(), "1차 채점");
        
        ExamResult secondResult = examResultService.startRegrading(firstResult.getId(), testAdmin.getId());
        examResultService.completeGrading(secondResult.getId(), "2차 재채점");
        
        // When: 채점 히스토리 조회
        List<ExamResult> history = examResultService.findAllResultsBySubmissionId(testSubmission.getId());
        
        // Then: 히스토리가 올바르게 조회되는지 확인
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getVersion()).isGreaterThan(history.get(1).getVersion()); // 최신순 정렬
        
        // 최신 결과 조회
        ExamResult latestResult = examResultService.findLatestResultBySubmissionId(testSubmission.getId());
        assertThat(latestResult.getId()).isEqualTo(secondResult.getId());
        assertThat(latestResult.getVersion()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("채점 상태별 조회 테스트")
    void testFindResultsByStatus() {
        // Given: 다양한 상태의 채점 결과들이 있는 상태
        ExamResult pendingResult = examResultService.startManualGrading(testSubmission.getId(), testAdmin.getId());
        
        ExamResult completedResult = examResultService.startAutoGrading(testSubmission.getId());
        examResultService.completeGrading(completedResult.getId(), "완료");
        
        // When: 상태별 조회
        Page<ExamResult> pendingResults = examResultService.findResultsByStatus(
            ResultStatus.IN_PROGRESS, PageRequest.of(0, 10)
        );
        Page<ExamResult> completedResults = examResultService.findResultsByStatus(
            ResultStatus.COMPLETED, PageRequest.of(0, 10)
        );
        
        // Then: 올바르게 필터링되는지 확인
        assertThat(pendingResults.getContent()).hasSize(1);
        assertThat(pendingResults.getContent().get(0).getStatus()).isEqualTo(ResultStatus.IN_PROGRESS);
        
        assertThat(completedResults.getContent()).hasSize(1);
        assertThat(completedResults.getContent().get(0).getStatus()).isEqualTo(ResultStatus.COMPLETED);
    }
    
    @Test
    @DisplayName("채점자별 조회 테스트")
    void testFindResultsByGrader() {
        // Given: 특정 채점자가 수행한 채점들이 있는 상태
        examResultService.startManualGrading(testSubmission.getId(), testAdmin.getId());
        examResultService.startAutoGrading(testSubmission.getId()); // 자동 채점 (채점자 없음)
        
        // When: 채점자별 조회
        Page<ExamResult> graderResults = examResultService.findResultsByGrader(
            testAdmin.getId(), PageRequest.of(0, 10)
        );
        Page<ExamResult> autoResults = examResultService.findAutoGradedResults(PageRequest.of(0, 10));
        
        // Then: 올바르게 필터링되는지 확인
        assertThat(graderResults.getContent()).hasSize(1);
        assertThat(graderResults.getContent().get(0).getGradedBy().getId()).isEqualTo(testAdmin.getId());
        
        assertThat(autoResults.getContent()).hasSize(1);
        assertThat(autoResults.getContent().get(0).isAutoGrading()).isTrue();
    }
    
    @Test
    @DisplayName("예외 상황 테스트 - 존재하지 않는 제출물")
    void testExceptionHandling_NonExistentSubmission() {
        // Given: 존재하지 않는 제출물 ID
        UUID nonExistentId = UUID.randomUUID();
        
        // When & Then: 예외가 발생하는지 확인
        assertThrows(IllegalArgumentException.class, () -> {
            examResultService.startAutoGrading(nonExistentId);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            examResultService.startManualGrading(nonExistentId, testAdmin.getId());
        });
    }
    
    @Test
    @DisplayName("예외 상황 테스트 - 존재하지 않는 채점자")
    void testExceptionHandling_NonExistentGrader() {
        // Given: 존재하지 않는 채점자 ID
        UUID nonExistentGraderId = UUID.randomUUID();
        
        // When & Then: 예외가 발생하는지 확인
        assertThrows(IllegalArgumentException.class, () -> {
            examResultService.startManualGrading(testSubmission.getId(), nonExistentGraderId);
        });
    }
    
    @Test
    @DisplayName("예외 상황 테스트 - 미완료 채점 완료 시도")
    void testExceptionHandling_IncompleteGradingCompletion() {
        // Given: 수동 채점이 시작되었지만 완료되지 않은 상태
        ExamResult result = examResultService.startManualGrading(testSubmission.getId(), testAdmin.getId());
        
        // When & Then: 미완료 상태에서 완료 처리 시 예외 발생
        assertThrows(IllegalStateException.class, () -> {
            examResultService.completeGrading(result.getId(), "강제 완료");
        });
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
                .submittedAnswer("답안 3")
                .answerType(ExamAnswer.AnswerType.MULTIPLE_CHOICE)
                .maxScore(5)
                .correctAnswer("정답")
                .build()
        );
    }
}