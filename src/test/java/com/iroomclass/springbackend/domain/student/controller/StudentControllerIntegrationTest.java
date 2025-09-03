package com.iroomclass.springbackend.domain.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.exam.entity.*;
import com.iroomclass.springbackend.domain.exam.repository.*;
import com.iroomclass.springbackend.domain.student.dto.request.StudentAuthRequest;
import com.iroomclass.springbackend.domain.student.dto.response.*;
import com.iroomclass.springbackend.domain.unit.entity.Unit;
import com.iroomclass.springbackend.domain.unit.repository.UnitRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StudentController 통합 테스트
 * 
 * <p>
 * 실제 데이터베이스와 Spring Boot 컨텍스트를 사용하여
 * StudentController의 모든 엔드포인트를 테스트합니다.
 * </p>
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("StudentController 통합 테스트")
class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositories for test data setup
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ExamSheetRepository examSheetRepository;

    @Autowired
    private ExamSheetQuestionRepository examSheetQuestionRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamSubmissionRepository examSubmissionRepository;

    @Autowired
    private ExamResultRepository examResultRepository;

    @Autowired
    private ExamResultQuestionRepository examResultQuestionRepository;

    // Test data
    private Student testStudent;
    private StudentAuthRequest validAuthRequest;
    private StudentAuthRequest invalidAuthRequest;
    private Exam testExam;
    private ExamSubmission testSubmission;
    private ExamResult testResult;

    @BeforeEach
    void setUp() {
        // 테스트 학생 생성
        testStudent = Student.builder()
                .name("홍길동")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(2000, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testStudent = studentRepository.save(testStudent);

        // 유효한 인증 요청
        validAuthRequest = new StudentAuthRequest(
                "홍길동",
                LocalDate.of(2000, 1, 1),
                "010-1234-5678");

        // 무효한 인증 요청
        invalidAuthRequest = new StudentAuthRequest(
                "김철수",
                LocalDate.of(1999, 12, 31),
                "010-9999-9999");

        setupExamTestData();
    }

    private void setupExamTestData() {
        // Unit 생성 (Question이 참조하는 데이터)
        Unit testUnit = Unit.builder()
                .id(UUID.randomUUID())
                .grade(1)
                .unitName("테스트 단원")
                .unitCode("TEST_UNIT_001")
                .description("테스트용 단원")
                .displayOrder(1)
                .build();
        unitRepository.save(testUnit);

        // Question 생성
        Question question1 = Question.builder()
                .id(UUID.randomUUID())
                .unit(testUnit)
                .questionText("2 + 2 = ?")
                .answerText("4")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .choices("[\"1\", \"2\", \"3\", \"4\"]")
                .correctChoice(4)
                .points(10)
                .difficulty(Difficulty.EASY)
                .build();

        Question question2 = Question.builder()
                .id(UUID.randomUUID())
                .unit(testUnit)
                .questionText("다음 중 소수는?")
                .answerText("7")
                .questionType(QuestionType.SUBJECTIVE)
                .points(15)
                .difficulty(Difficulty.MEDIUM)
                .build();

        questionRepository.saveAll(List.of(question1, question2));

        // ExamSheet 생성
        ExamSheet examSheet = ExamSheet.builder()
                .id(UUID.randomUUID())
                .examName("수학 중간고사")
                .grade(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        examSheetRepository.save(examSheet);

        // ExamSheetQuestion 생성
        ExamSheetQuestion esq1 = ExamSheetQuestion.builder()
                .id(UUID.randomUUID())
                .examSheet(examSheet)
                .question(question1)
                .questionOrder(1)
                .points(10)
                .build();

        ExamSheetQuestion esq2 = ExamSheetQuestion.builder()
                .id(UUID.randomUUID())
                .examSheet(examSheet)
                .question(question2)
                .questionOrder(2)
                .points(15)
                .build();

        examSheetQuestionRepository.saveAll(List.of(esq1, esq2));

        // Exam 생성
        testExam = Exam.builder()
                .id(UUID.randomUUID())
                .examSheet(examSheet)
                .examName("수학 중간고사")
                .grade(1)
                .content("1학년 수학 중간고사입니다")
                .qrCodeUrl("https://example.com/qr/123")
                .createdAt(LocalDateTime.now())
                .build();
        examRepository.save(testExam);

        // ExamSubmission 생성
        testSubmission = ExamSubmission.builder()
                .id(UUID.randomUUID())
                .exam(testExam)
                .student(testStudent)
                .submittedAt(LocalDateTime.now())
                .build();
        examSubmissionRepository.save(testSubmission);

        // ExamResult 생성
        testResult = ExamResult.builder()
                .id(UUID.randomUUID())
                .examSubmission(testSubmission)
                .examSheet(examSheet)
                .gradedAt(LocalDateTime.now())
                .totalScore(20)
                .status(ResultStatus.COMPLETED)
                .scoringComment("잘했습니다")
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        examResultRepository.save(testResult);

        // ExamResultQuestion 생성
        ExamResultQuestion erq1 = ExamResultQuestion.builder()
                .id(UUID.randomUUID())
                .examResult(testResult)
                .question(question1)
                .studentAnswer("4")
                .score(10)
                .feedback("정답입니다")
                .isCorrect(true)
                .build();

        ExamResultQuestion erq2 = ExamResultQuestion.builder()
                .id(UUID.randomUUID())
                .examResult(testResult)
                .question(question2)
                .studentAnswer("7")
                .score(10)
                .feedback("부분적으로 정답입니다")
                .isCorrect(false)
                .build();

        examResultQuestionRepository.saveAll(List.of(erq1, erq2));
    }

    @Nested
    @DisplayName("POST /api/student/login - 학생 로그인")
    class LoginEndpointTest {

        @Test
        @Order(1)
        @DisplayName("유효한 인증 정보로 로그인 성공")
        void login_ValidRequest_Success() throws Exception {
            // When & Then
            MvcResult result = mockMvc.perform(post("/api/student/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validAuthRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("학생 로그인 성공"))
                    .andExpected(jsonPath("$.data.id").value(testStudent.getId().intValue()))
                    .andExpect(jsonPath("$.data.name").value("홍길동"))
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            ApiResponse<StudentLoginResponse> response = objectMapper.readValue(
                    responseJson, objectMapper.getTypeFactory()
                            .constructParametricType(ApiResponse.class, StudentLoginResponse.class));

            assertThat(response.result()).isEqualTo("SUCCESS");
            assertThat(response.data().name()).isEqualTo("홍길동");
        }

        @Test
        @Order(2)
        @DisplayName("무효한 인증 정보로 로그인 실패")
        void login_InvalidRequest_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/student/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidAuthRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpected(jsonPath("$.result").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @Order(3)
        @DisplayName("잘못된 요청 형식으로 Validation 오류")
        void login_InvalidFormat_BadRequest() throws Exception {
            // Given
            StudentAuthRequest invalidFormatRequest = new StudentAuthRequest(
                    "", // 빈 이름
                    null, // null 생년월일
                    "invalid-phone" // 잘못된 전화번호 형식
            );

            // When & Then
            mockMvc.perform(post("/api/student/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidFormatRequest)))
                    .andDo(print())
                    .andExpected(status().isBadRequest())
                    .andExpected(jsonPath("$.result").value("ERROR"));
        }
    }

    @Nested
    @DisplayName("POST /api/student/recent-submissions - 최근 제출 내역")
    class RecentSubmissionsEndpointTest {

        @Test
        @Order(4)
        @DisplayName("최근 제출 내역 조회 성공")
        void getRecentSubmissions_ValidRequest_Success() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/student/recent-submissions")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validAuthRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpected(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("최근 시험 제출 내역 조회 성공"))
                    .andExpected(jsonPath("$.data.content").isArray())
                    .andExpected(jsonPath("$.data.totalElements").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/student/exam-results - 시험 결과 요약")
    class ExamResultsEndpointTest {

        @Test
        @Order(5)
        @DisplayName("시험 결과 요약 조회 성공")
        void getExamResultsSummary_ValidRequest_Success() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/student/exam-results")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validAuthRequest)))
                    .andDo(print())
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.result").value("SUCCESS"))
                    .andExpected(jsonPath("$.message").value("시험 결과 요약 조회 성공"))
                    .andExpected(jsonPath("$.data.content").isArray())
                    .andExpected(jsonPath("$.data.totalElements").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/student/exam-detail/{examId} - 시험 상세 결과")
    class ExamDetailEndpointTest {

        @Test
        @Order(6)
        @DisplayName("시험 상세 결과 조회 성공")
        void getExamDetailResult_ValidRequest_Success() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/student/exam-detail/{examId}", testExam.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validAuthRequest)))
                    .andDo(print())
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.result").value("SUCCESS"))
                    .andExpected(jsonPath("$.message").value("시험 상세 결과 조회 성공"))
                    .andExpected(jsonPath("$.data.examName").exists())
                    .andExpected(jsonPath("$.data.totalScore").exists())
                    .andExpected(jsonPath("$.data.questions").isArray());
        }

        @Test
        @Order(7)
        @DisplayName("존재하지 않는 시험 ID로 조회 실패")
        void getExamDetailResult_NonExistentExam_NotFound() throws Exception {
            // Given
            UUID nonExistentExamId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(post("/api/student/exam-detail/{examId}", nonExistentExamId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validAuthRequest)))
                    .andDo(print())
                    .andExpected(status().isNotFound())
                    .andExpected(jsonPath("$.result").value("ERROR"));
        }

        @Test
        @Order(8)
        @DisplayName("잘못된 UUID 형식으로 요청 실패")
        void getExamDetailResult_InvalidUUID_BadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/student/exam-detail/invalid-uuid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validAuthRequest)))
                    .andDo(print())
                    .andExpected(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/student/info - 학생 정보")
    class StudentInfoEndpointTest {

        @Test
        @Order(9)
        @DisplayName("학생 정보 조회 성공")
        void getStudentInfo_ValidRequest_Success() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/student/info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validAuthRequest)))
                    .andDo(print())
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.result").value("SUCCESS"))
                    .andExpected(jsonPath("$.message").value("학생 정보 조회 성공"))
                    .andExpected(jsonPath("$.data.id").value(testStudent.getId().intValue()))
                    .andExpected(jsonPath("$.data.name").value("홍길동"))
                    .andExpected(jsonPath("$.data.phone").value("010-1234-5678"))
                    .andExpected(jsonPath("$.data.birthDate").value("2000-01-01"))
                    .andExpected(jsonPath("$.data.currentGrade").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/student/logout - 로그아웃")
    class LogoutEndpointTest {

        @Test
        @Order(10)
        @DisplayName("로그아웃 성공")
        void logout_ValidRequest_Success() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/student/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validAuthRequest)))
                    .andDo(print())
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.result").value("SUCCESS"))
                    .andExpected(jsonPath("$.message").value("학생 로그아웃 성공"))
                    .andExpected(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("공통 예외 처리 테스트")
    class CommonExceptionTest {

        @Test
        @Order(11)
        @DisplayName("존재하지 않는 엔드포인트 요청")
        void requestNonExistentEndpoint_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/student/non-existent")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @Order(12)
        @DisplayName("지원하지 않는 HTTP 메서드 요청")
        void requestUnsupportedMethod_MethodNotAllowed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/student/login")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @Order(13)
        @DisplayName("잘못된 JSON 형식 요청")
        void requestInvalidJson_BadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/student/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid json }"))
                    .andDo(print())
                    .andExpected(status().isBadRequest())
                    .andExpected(jsonPath("$.result").value("ERROR"));
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTest {

        @Test
        @Order(14)
        @DisplayName("대량 요청 처리 성능 테스트")
        void bulkRequests_Performance() throws Exception {
            // Given
            int requestCount = 100;
            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < requestCount; i++) {
                mockMvc.perform(post("/api/student/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                        .andExpected(status().isOk());
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(duration).isLessThan(10000); // 10초 이내 완료
        }
    }

    @Nested
    @DisplayName("보안 테스트")
    class SecurityTest {

        @Test
        @Order(15)
        @DisplayName("SQL Injection 공격 시도")
        void sqlInjectionAttempt_Handled() throws Exception {
            // Given
            StudentAuthRequest sqlInjectionRequest = new StudentAuthRequest(
                    "'; DROP TABLE student; --",
                    LocalDate.of(2000, 1, 1),
                    "010-1234-5678");

            // When & Then
            mockMvc.perform(post("/api/student/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sqlInjectionRequest)))
                    .andDo(print())
                    .andExpected(status().isNotFound()); // SQL Injection이 아닌 정상적인 NOT FOUND

            // 테이블이 여전히 존재하는지 확인
            assertThat(studentRepository.count()).isGreaterThan(0);
        }

        @Test
        @Order(16)
        @DisplayName("XSS 공격 시도")
        void xssAttempt_Handled() throws Exception {
            // Given
            StudentAuthRequest xssRequest = new StudentAuthRequest(
                    "<script>alert('XSS')</script>",
                    LocalDate.of(2000, 1, 1),
                    "010-1234-5678");

            // When & Then
            mockMvc.perform(post("/api/student/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(xssRequest)))
                    .andDo(print())
                    .andExpected(status().isNotFound()); // 정상적인 처리
        }
    }

    @AfterEach
    void tearDown() {
        // 테스트 데이터 정리는 @Transactional에 의해 자동으로 rollback됨
    }
}