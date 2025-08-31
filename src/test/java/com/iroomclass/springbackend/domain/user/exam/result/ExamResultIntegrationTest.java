package com.iroomclass.springbackend.domain.user.exam.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.admin.question.entity.Question;
import com.iroomclass.springbackend.domain.admin.question.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;
import com.iroomclass.springbackend.domain.admin.unit.entity.UnitCategory;
import com.iroomclass.springbackend.domain.admin.unit.entity.UnitSubcategory;
import com.iroomclass.springbackend.domain.admin.unit.repository.UnitRepository;
import com.iroomclass.springbackend.domain.admin.unit.repository.UnitCategoryRepository;
import com.iroomclass.springbackend.domain.admin.unit.repository.UnitSubcategoryRepository;
import com.iroomclass.springbackend.domain.user.info.entity.User;
import com.iroomclass.springbackend.domain.user.info.repository.UserRepository;
import com.iroomclass.springbackend.domain.user.exam.answer.entity.StudentAnswerSheet;
import com.iroomclass.springbackend.domain.user.exam.answer.repository.StudentAnswerSheetRepository;
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
 * AI 기반 시험 채점 시스템의 전체적인 통합 테스트를 수행합니다.
 * AI 자동 채점, AI 재채점 플로우를 검증합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    private UserRepository userRepository;
    
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private ExamSubmissionRepository examSubmissionRepository;
    
    @Autowired
    private StudentAnswerSheetRepository studentAnswerSheetRepository;
    
    @Autowired
    private com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetRepository examSheetRepository;
    
    @Autowired
    private UnitRepository unitRepository;
    
    @Autowired
    private UnitCategoryRepository unitCategoryRepository;
    
    @Autowired
    private UnitSubcategoryRepository unitSubcategoryRepository;
    
    /**
     * 테스트용 데이터 생성 헬퍼 메서드
     */
    @Transactional
    private TestData createTestData(String uniqueSuffix) {
        // 더 강한 유니크성을 위해 시간과 랜덤값 추가
        String strongUniqueSuffix = uniqueSuffix + "_" + System.nanoTime() + "_" + UUID.randomUUID().toString().substring(0, 8);
        // 테스트 학생 생성 (이름 길이 제한 고려)
        User testStudent = User.builder()
            .name("학생" + strongUniqueSuffix.substring(0, Math.min(15, strongUniqueSuffix.length())))
            .phone("010-1234-" + strongUniqueSuffix.substring(Math.max(0, strongUniqueSuffix.length() - 4)))
            .grade(3)
            .birthDate(LocalDate.of(2000, 1, 1))
            .build();
        testStudent = userRepository.save(testStudent);
        
        // 테스트 시험지 생성
        com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet testExamSheet = 
            com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet.builder()
                .examName("시험지" + strongUniqueSuffix.substring(0, Math.min(10, strongUniqueSuffix.length())))
                .grade(3)
                .totalQuestions(1)
                .multipleChoiceCount(0)
                .subjectiveCount(1)
                .build();
        testExamSheet = examSheetRepository.save(testExamSheet);
        
        // 테스트 시험 생성
        Exam testExam = Exam.builder()
            .examName("시험" + strongUniqueSuffix.substring(0, Math.min(10, strongUniqueSuffix.length())))
            .grade(3)
            .content("테스트 시험 내용")
            .studentCount(1)
            .qrCodeUrl("http://test.com/qr/" + strongUniqueSuffix.substring(0, Math.min(10, strongUniqueSuffix.length())))
            .examSheet(testExamSheet)
            .build();
        testExam = examRepository.save(testExam);
        
        // Unit 계층 구조 생성 (고유 식별자 사용)
        UnitCategory testCategory = UnitCategory.builder()
            .categoryName("수와연산" + strongUniqueSuffix.substring(0, Math.min(10, strongUniqueSuffix.length())))
            .displayOrder(1)
            .description("테스트 대분류")
            .build();
        testCategory = unitCategoryRepository.save(testCategory);
        
        UnitSubcategory testSubcategory = UnitSubcategory.builder()
            .category(testCategory)
            .subcategoryName("정수유리수" + strongUniqueSuffix.substring(0, Math.min(10, strongUniqueSuffix.length())))
            .displayOrder(1)
            .description("테스트 중분류")
            .build();
        testSubcategory = unitSubcategoryRepository.save(testSubcategory);
        
        Unit testUnit = Unit.builder()
            .subcategory(testSubcategory)
            .grade(3)
            .unitName("정수" + strongUniqueSuffix.substring(0, Math.min(10, strongUniqueSuffix.length())))
            .unitCode("TEST_" + strongUniqueSuffix.substring(0, Math.min(20, strongUniqueSuffix.length())))
            .description("테스트 세부단원")
            .displayOrder(1)
            .build();
        testUnit = unitRepository.save(testUnit);
        
        // 테스트 문제 생성 (객관식)
        Question testQuestion = Question.builder()
            .questionText("문제" + strongUniqueSuffix.substring(0, Math.min(20, strongUniqueSuffix.length())))
            .answerText("정답은 1번입니다")
            .points(5)
            .difficulty(Question.Difficulty.하)
            .questionType(Question.QuestionType.MULTIPLE_CHOICE)
            .choices("{\"1\":\"선택지1\",\"2\":\"선택지2\",\"3\":\"선택지3\",\"4\":\"선택지4\",\"5\":\"선택지5\"}")
            .correctChoice(1)
            .unit(testUnit)
            .build();
        testQuestion = questionRepository.save(testQuestion);
        
        // 테스트 시험 제출 생성
        ExamSubmission testSubmission = ExamSubmission.builder()
            .user(testStudent)
            .exam(testExam)
            .build();
        testSubmission = examSubmissionRepository.save(testSubmission);
        
        // 테스트 답안 생성 (객관식 정답)
        StudentAnswerSheet answer = StudentAnswerSheet.builder()
            .examSubmission(testSubmission)
            .question(testQuestion)
            .answerText("1번을 선택했습니다")
            .selectedChoice(1)
            .answerImageUrl("http://test.com/answer/" + strongUniqueSuffix.substring(0, Math.min(10, strongUniqueSuffix.length())))
            .build();
        List<StudentAnswerSheet> testAnswers = List.of(studentAnswerSheetRepository.save(answer));
        
        return new TestData(testStudent, testExam, testQuestion, testSubmission, testAnswers);
    }
    
    /**
     * 테스트 데이터 컨테이너 클래스
     */
    private record TestData(
        User student,
        Exam exam,
        Question question,
        ExamSubmission submission,
        List<StudentAnswerSheet> answers
    ) {}
    
    @Test
    @DisplayName("AI 자동 채점 전체 플로우 테스트")
    void testAutoGradingFlow() {
        // Given: 테스트 데이터 생성
        TestData testData = createTestData("autoGrading_" + System.currentTimeMillis());
        
        // When: AI 자동 채점 시작
        ExamResult result = examResultService.startAutoGrading(testData.submission().getId());
        
        // Then: 채점 결과가 생성되었는지 확인
        assertThat(result).isNotNull();
        assertThat(result.getExamSubmission().getId()).isEqualTo(testData.submission().getId());
        assertThat(result.isAutoGrading()).isTrue();
        assertThat(result.getStatus()).isEqualTo(ResultStatus.IN_PROGRESS);
        assertThat(result.getVersion()).isEqualTo(1);
        
        // 문제별 결과가 생성되었는지 확인
        List<QuestionResult> questionResults = questionResultService.findByExamResultId(result.getId());
        assertThat(questionResults).hasSize(testData.answers().size());
        
        // When: AI 채점 완료
        examResultService.completeGrading(result.getId(), "AI 자동 채점 완료");
        
        // Then: 채점이 완료되었는지 확인
        ExamResult completedResult = examResultService.findById(result.getId());
        assertThat(completedResult.getStatus()).isEqualTo(ResultStatus.COMPLETED);
        assertThat(completedResult.getTotalScore()).isNotNull();
        assertThat(completedResult.getGradingComment()).isEqualTo("AI 자동 채점 완료");
    }
    
    @Test
    @DisplayName("AI 재채점 플로우 테스트")
    void testAIRegradingFlow() {
        // Given: 테스트 데이터 생성 및 초기 채점
        TestData testData = createTestData("regrading_" + System.currentTimeMillis());
        ExamResult originalResult = examResultService.startAutoGrading(testData.submission().getId());
        examResultService.completeGrading(originalResult.getId(), "첫 번째 AI 채점");
        
        // When: AI 재채점 시작
        ExamResult regradingResult = examResultService.startRegrading(originalResult.getId());
        
        // Then: 재채점 결과가 생성되었는지 확인
        assertThat(regradingResult).isNotNull();
        assertThat(regradingResult.getExamSubmission().getId()).isEqualTo(testData.submission().getId());
        assertThat(regradingResult.getVersion()).isEqualTo(2); // 버전이 증가했는지 확인
        assertThat(regradingResult.getStatus()).isEqualTo(ResultStatus.IN_PROGRESS);
        assertThat(regradingResult.isAutoGrading()).isTrue();
        
        // When: 재채점 완료
        examResultService.completeGrading(regradingResult.getId(), "AI 재채점 완료");
        
        // Then: 재채점이 완료되었는지 확인
        ExamResult completedResult = examResultService.findById(regradingResult.getId());
        assertThat(completedResult.getStatus()).isEqualTo(ResultStatus.REGRADED);
        assertThat(completedResult.getGradingComment()).isEqualTo("AI 재채점 완료");
    }
    
    @Test
    @DisplayName("버전 관리 테스트")
    void testVersionManagement() {
        // Given: 테스트 데이터 생성
        TestData testData = createTestData("version_" + System.currentTimeMillis());
        
        // 첫 번째 AI 채점
        ExamResult firstResult = examResultService.startAutoGrading(testData.submission().getId());
        examResultService.completeGrading(firstResult.getId(), "첫 번째 AI 채점");
        
        // When: AI 재채점 (두 번째 버전)
        ExamResult secondResult = examResultService.startRegrading(firstResult.getId());
        examResultService.completeGrading(secondResult.getId(), "두 번째 AI 채점");
        
        // Then: 버전 관리가 올바르게 되고 있는지 확인
        assertThat(firstResult.getVersion()).isEqualTo(1);
        assertThat(secondResult.getVersion()).isEqualTo(2);
        
        // 최신 버전 조회 테스트
        ExamResult latestResult = examResultService.findLatestResultBySubmissionId(testData.submission().getId());
        assertThat(latestResult.getId()).isEqualTo(secondResult.getId());
        assertThat(latestResult.getVersion()).isEqualTo(2);
        
        // 모든 버전 조회 테스트
        List<ExamResult> allResults = examResultService.findAllResultsBySubmissionId(testData.submission().getId());
        assertThat(allResults).hasSize(2);
        assertThat(allResults.get(0).getVersion()).isEqualTo(2); // 최신 버전이 먼저
        assertThat(allResults.get(1).getVersion()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("AI 자동 채점 결과 조회 테스트")
    void testFindAutoGradedResults() {
        // Given: 테스트 데이터 생성 및 AI 자동 채점
        TestData testData = createTestData("autoGradedResults_" + System.currentTimeMillis());
        ExamResult aiResult = examResultService.startAutoGrading(testData.submission().getId());
        examResultService.completeGrading(aiResult.getId(), "AI 자동 채점 완료");
        
        // When: AI 자동 채점 결과 조회
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ExamResult> autoGradedResults = examResultService.findAutoGradedResults(pageRequest);
        
        // Then: 조회 결과 검증
        assertThat(autoGradedResults).isNotNull();
        assertThat(autoGradedResults.getContent()).isNotEmpty();
        assertThat(autoGradedResults.getContent().get(0).isAutoGrading()).isTrue();
    }
    
    @Test
    @DisplayName("예외 상황 테스트")
    void testExceptionCases() {
        // 존재하지 않는 제출물로 채점 시도
        UUID nonExistentId = UUID.randomUUID();
        assertThrows(RuntimeException.class, () -> {
            examResultService.startAutoGrading(nonExistentId);
        });
    }
    
    @Test
    @DisplayName("채점 상태별 통계 테스트")
    void testGradingStatistics() {
        // Given: 테스트 데이터 생성 및 채점 완료
        TestData testData = createTestData("statistics_" + System.currentTimeMillis());
        ExamResult pendingResult = examResultService.startAutoGrading(testData.submission().getId());
        examResultService.completeGrading(pendingResult.getId(), "AI 채점 완료");
        
        // When: 상태별 개수 조회
        long completedCount = examResultService.countByStatus(ResultStatus.COMPLETED);
        long autoGradedCount = examResultService.countAutoGradedResults();
        
        // Then: 개수가 정확한지 확인
        assertThat(completedCount).isGreaterThan(0);
        assertThat(autoGradedCount).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("페이징 조회 테스트")
    void testPagingQueries() {
        // Given: 테스트 데이터 생성 및 여러 채점 결과 생성
        TestData testData = createTestData("paging_" + System.currentTimeMillis());
        ExamResult result1 = examResultService.startAutoGrading(testData.submission().getId());
        examResultService.completeGrading(result1.getId(), "첫 번째 AI 채점");
        
        ExamResult result2 = examResultService.startRegrading(result1.getId());
        examResultService.completeGrading(result2.getId(), "두 번째 AI 채점");
        
        // When: 페이징 조회
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ExamResult> results = examResultService.findAutoGradedResults(pageRequest);
        
        // Then: 페이징이 정상 작동하는지 확인
        assertThat(results).isNotNull();
        assertThat(results.getContent()).isNotEmpty();
        assertThat(results.getTotalElements()).isGreaterThanOrEqualTo(2);
    }
}