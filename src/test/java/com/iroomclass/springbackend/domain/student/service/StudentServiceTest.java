package com.iroomclass.springbackend.domain.student.service;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.student.dto.request.StudentAuthRequest;
import com.iroomclass.springbackend.domain.student.dto.response.*;
import com.iroomclass.springbackend.domain.student.exception.StudentNotFoundException;
import com.iroomclass.springbackend.domain.student.repository.StudentExamResultRepository;
import com.iroomclass.springbackend.domain.student.repository.StudentExamSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/**
 * StudentService 단위 테스트
 * 
 * <p>학생 관련 비즈니스 로직을 테스트합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService 테스트")
class StudentServiceTest {

    @Mock
    private StudentAuthService studentAuthService;
    
    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private StudentExamSubmissionRepository studentExamSubmissionRepository;
    
    @Mock
    private StudentExamResultRepository studentExamResultRepository;

    @InjectMocks
    private StudentService studentService;

    private Student testStudent;
    private StudentAuthRequest validRequest;
    private UUID testExamId;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testStudent = Student.builder()
                .id(1L)
                .name("홍길동")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(2000, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = new StudentAuthRequest(
                "홍길동",
                LocalDate.of(2000, 1, 1),
                "010-1234-5678"
        );

        testExamId = UUID.randomUUID();
        testPageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("login 메서드 테스트")
    class LoginTest {

        @Test
        @DisplayName("유효한 인증 정보로 로그인 성공")
        void login_ValidRequest_Success() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);

            // When
            StudentLoginResponse result = studentService.login(validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testStudent.getId());
            assertThat(result.name()).isEqualTo(testStudent.getName());

            verify(studentAuthService).validateAndGetStudent(validRequest);
        }

        @Test
        @DisplayName("무효한 인증 정보로 로그인 실패")
        void login_InvalidRequest_ThrowsException() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willThrow(new StudentNotFoundException("학생을 찾을 수 없습니다"));

            // When & Then
            assertThatThrownBy(() -> studentService.login(validRequest))
                    .isInstanceOf(StudentNotFoundException.class)
                    .hasMessage("학생을 찾을 수 없습니다");

            verify(studentAuthService).validateAndGetStudent(validRequest);
        }
    }

    @Nested
    @DisplayName("getRecentSubmissions 메서드 테스트")
    class GetRecentSubmissionsTest {

        @Test
        @DisplayName("최근 시험 제출 내역 조회 성공")
        void getRecentSubmissions_ValidRequest_Success() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);

            List<RecentSubmissionDto> mockSubmissions = List.of(
                    new RecentSubmissionDto("수학 중간고사", LocalDateTime.now(), "1학년 수학 시험", 20L),
                    new RecentSubmissionDto("국어 중간고사", LocalDateTime.now().minusDays(1), "1학년 국어 시험", 15L)
            );
            Page<RecentSubmissionDto> mockPage = new PageImpl<>(mockSubmissions, testPageable, 2);

            given(studentExamSubmissionRepository.findRecentSubmissionsByStudentId(
                    testStudent.getId(), testPageable))
                    .willReturn(mockPage);

            // When
            Page<RecentSubmissionDto> result = studentService.getRecentSubmissions(validRequest, testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).examName()).isEqualTo("수학 중간고사");

            verify(studentAuthService).validateAndGetStudent(validRequest);
            verify(studentExamSubmissionRepository).findRecentSubmissionsByStudentId(
                    testStudent.getId(), testPageable);
        }

        @Test
        @DisplayName("제출 내역이 없는 경우 빈 페이지 반환")
        void getRecentSubmissions_NoSubmissions_ReturnsEmptyPage() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);

            Page<RecentSubmissionDto> emptyPage = new PageImpl<>(List.of(), testPageable, 0);
            given(studentExamSubmissionRepository.findRecentSubmissionsByStudentId(
                    testStudent.getId(), testPageable))
                    .willReturn(emptyPage);

            // When
            Page<RecentSubmissionDto> result = studentService.getRecentSubmissions(validRequest, testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getExamResultsSummary 메서드 테스트")
    class GetExamResultsSummaryTest {

        @Test
        @DisplayName("시험 결과 요약 조회 성공")
        void getExamResultsSummary_ValidRequest_Success() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);

            List<ExamResultSummaryDto> mockResults = List.of(
                    new ExamResultSummaryDto("수학 중간고사", 85, "COMPLETED", 
                            LocalDateTime.now(), 1),
                    new ExamResultSummaryDto("국어 중간고사", 92, "COMPLETED", 
                            LocalDateTime.now().minusDays(1), 1)
            );
            Page<ExamResultSummaryDto> mockPage = new PageImpl<>(mockResults, testPageable, 2);

            given(studentExamResultRepository.findExamResultsSummaryByStudentId(
                    testStudent.getId(), testPageable))
                    .willReturn(mockPage);

            // When
            Page<ExamResultSummaryDto> result = studentService.getExamResultsSummary(validRequest, testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).examName()).isEqualTo("수학 중간고사");
            assertThat(result.getContent().get(0).totalScore()).isEqualTo(85);

            verify(studentAuthService).validateAndGetStudent(validRequest);
            verify(studentExamResultRepository).findExamResultsSummaryByStudentId(
                    testStudent.getId(), testPageable);
        }
    }

    @Nested
    @DisplayName("getExamDetailResult 메서드 테스트")
    class GetExamDetailResultTest {

        @Test
        @DisplayName("시험 상세 결과 조회 성공")
        void getExamDetailResult_ValidRequest_Success() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);

            List<QuestionDetailDto> mockQuestions = List.of(
                    new QuestionDetailDto("2x + 3 = 7을 풀어보세요", "x = 2", 10, true, "정답입니다"),
                    new QuestionDetailDto("다음 중 옳은 것은?", "1번", 8, false, "정답은 3번입니다")
            );

            ExamDetailResultDto mockResult = new ExamDetailResultDto(
                    "수학 중간고사",
                    85,
                    "COMPLETED",
                    LocalDateTime.now(),
                    1,
                    mockQuestions,
                    1L,
                    1L
            );

            given(studentExamResultRepository.findDetailResultByStudentIdAndExamId(
                    testStudent.getId(), testExamId))
                    .willReturn(Optional.of(mockResult));

            // When
            ExamDetailResultDto result = studentService.getExamDetailResult(validRequest, testExamId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.examName()).isEqualTo("수학 중간고사");
            assertThat(result.totalScore()).isEqualTo(85);
            assertThat(result.questions()).hasSize(2);
            assertThat(result.subjectiveCount()).isEqualTo(1L);
            assertThat(result.multipleChoiceCount()).isEqualTo(1L);

            verify(studentAuthService).validateAndGetStudent(validRequest);
            verify(studentExamResultRepository).findDetailResultByStudentIdAndExamId(
                    testStudent.getId(), testExamId);
        }

        @Test
        @DisplayName("존재하지 않는 시험 결과 조회 시 예외 발생")
        void getExamDetailResult_NonExistentResult_ThrowsException() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);

            given(studentExamResultRepository.findDetailResultByStudentIdAndExamId(
                    testStudent.getId(), testExamId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentService.getExamDetailResult(validRequest, testExamId))
                    .isInstanceOf(StudentNotFoundException.class)
                    .hasMessage("해당 시험의 결과를 찾을 수 없습니다");

            verify(studentAuthService).validateAndGetStudent(validRequest);
            verify(studentExamResultRepository).findDetailResultByStudentIdAndExamId(
                    testStudent.getId(), testExamId);
        }
    }

    @Nested
    @DisplayName("getStudentInfo 메서드 테스트")
    class GetStudentInfoTest {

        @Test
        @DisplayName("학생 정보 조회 성공 (최신 학년 정보 포함)")
        void getStudentInfo_ValidRequest_Success() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);

            given(studentExamSubmissionRepository.findLatestGradeByStudentId(testStudent.getId()))
                    .willReturn(Optional.of(2));

            // When
            StudentInfoDto result = studentService.getStudentInfo(validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testStudent.getId());
            assertThat(result.name()).isEqualTo(testStudent.getName());
            assertThat(result.phone()).isEqualTo(testStudent.getPhone());
            assertThat(result.birthDate()).isEqualTo(testStudent.getBirthDate());
            assertThat(result.currentGrade()).isEqualTo(2);

            verify(studentAuthService).validateAndGetStudent(validRequest);
            verify(studentExamSubmissionRepository).findLatestGradeByStudentId(testStudent.getId());
        }

        @Test
        @DisplayName("응시 기록이 없는 학생 정보 조회 (학년 null)")
        void getStudentInfo_NoExamHistory_GradeIsNull() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);

            given(studentExamSubmissionRepository.findLatestGradeByStudentId(testStudent.getId()))
                    .willReturn(Optional.empty());

            // When
            StudentInfoDto result = studentService.getStudentInfo(validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.currentGrade()).isNull();
            assertThat(result.name()).isEqualTo(testStudent.getName());
        }
    }

    @Nested
    @DisplayName("logout 메서드 테스트")
    class LogoutTest {

        @Test
        @DisplayName("로그아웃 성공")
        void logout_ValidRequest_Success() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);

            // When & Then (예외가 발생하지 않아야 함)
            assertThatCode(() -> studentService.logout(validRequest))
                    .doesNotThrowAnyException();

            verify(studentAuthService).validateAndGetStudent(validRequest);
        }

        @Test
        @DisplayName("무효한 인증 정보로 로그아웃 실패")
        void logout_InvalidRequest_ThrowsException() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willThrow(new StudentNotFoundException("학생을 찾을 수 없습니다"));

            // When & Then
            assertThatThrownBy(() -> studentService.logout(validRequest))
                    .isInstanceOf(StudentNotFoundException.class);

            verify(studentAuthService).validateAndGetStudent(validRequest);
        }
    }

    @Nested
    @DisplayName("통계 메서드 테스트")
    class StatisticsTest {

        @Test
        @DisplayName("전체 제출 횟수 조회")
        void getTotalSubmissionCount_ReturnsCorrectCount() {
            // Given
            Long studentId = 1L;
            given(studentExamSubmissionRepository.countByStudentId(studentId))
                    .willReturn(5L);

            // When
            long result = studentService.getTotalSubmissionCount(studentId);

            // Then
            assertThat(result).isEqualTo(5L);
            verify(studentExamSubmissionRepository).countByStudentId(studentId);
        }

        @Test
        @DisplayName("평균 점수 조회")
        void getAverageScore_ReturnsCorrectAverage() {
            // Given
            Long studentId = 1L;
            given(studentExamResultRepository.findAverageScoreByStudentId(studentId))
                    .willReturn(87.5);

            // When
            Double result = studentService.getAverageScore(studentId);

            // Then
            assertThat(result).isEqualTo(87.5);
            verify(studentExamResultRepository).findAverageScoreByStudentId(studentId);
        }

        @Test
        @DisplayName("시험 기록이 없는 학생의 평균 점수 조회")
        void getAverageScore_NoExamHistory_ReturnsNull() {
            // Given
            Long studentId = 1L;
            given(studentExamResultRepository.findAverageScoreByStudentId(studentId))
                    .willReturn(null);

            // When
            Double result = studentService.getAverageScore(studentId);

            // Then
            assertThat(result).isNull();
            verify(studentExamResultRepository).findAverageScoreByStudentId(studentId);
        }
    }

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionTest {

        @Test
        @DisplayName("Repository 예외 발생 시 전파")
        void handleRepositoryException() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);
            
            given(studentExamSubmissionRepository.findRecentSubmissionsByStudentId(any(), any()))
                    .willThrow(new RuntimeException("데이터베이스 오류"));

            // When & Then
            assertThatThrownBy(() -> studentService.getRecentSubmissions(validRequest, testPageable))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("데이터베이스 오류");
        }

        @Test
        @DisplayName("null 페이지 요청 처리")
        void handleNullPageable() {
            // Given
            given(studentAuthService.validateAndGetStudent(validRequest))
                    .willReturn(testStudent);

            // When & Then
            assertThatThrownBy(() -> studentService.getRecentSubmissions(validRequest, null))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}