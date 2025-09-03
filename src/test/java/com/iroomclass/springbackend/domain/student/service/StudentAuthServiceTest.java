package com.iroomclass.springbackend.domain.student.service;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import com.iroomclass.springbackend.domain.auth.repository.StudentRepository;
import com.iroomclass.springbackend.domain.student.dto.request.StudentAuthRequest;
import com.iroomclass.springbackend.domain.student.exception.StudentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/**
 * StudentAuthService 단위 테스트
 * 
 * <p>학생 인증 서비스의 핵심 기능들을 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentAuthService 테스트")
class StudentAuthServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentAuthService studentAuthService;

    private Student testStudent;
    private StudentAuthRequest validRequest;
    private StudentAuthRequest invalidRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 학생 데이터 설정
        testStudent = Student.builder()
                .id(1L)
                .name("홍길동")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(2000, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 유효한 인증 요청
        validRequest = new StudentAuthRequest(
                "홍길동",
                LocalDate.of(2000, 1, 1),
                "010-1234-5678"
        );

        // 무효한 인증 요청
        invalidRequest = new StudentAuthRequest(
                "김철수",
                LocalDate.of(1999, 12, 31),
                "010-9999-9999"
        );
    }

    @Nested
    @DisplayName("validateAndGetStudent 메서드 테스트")
    class ValidateAndGetStudentTest {

        @Test
        @DisplayName("유효한 학생 인증 정보로 학생 조회 성공")
        void validateAndGetStudent_ValidRequest_Success() {
            // Given
            given(studentRepository.findByNameAndPhoneAndBirthDate(
                    validRequest.name(), validRequest.phone(), validRequest.birthDate()))
                    .willReturn(Optional.of(testStudent));

            // When
            Student result = studentAuthService.validateAndGetStudent(validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testStudent.getId());
            assertThat(result.getName()).isEqualTo(testStudent.getName());
            assertThat(result.getPhone()).isEqualTo(testStudent.getPhone());
            assertThat(result.getBirthDate()).isEqualTo(testStudent.getBirthDate());

            // Repository 메서드 호출 검증
            verify(studentRepository).findByNameAndPhoneAndBirthDate(
                    validRequest.name(), validRequest.phone(), validRequest.birthDate());
        }

        @Test
        @DisplayName("존재하지 않는 학생 인증 정보로 예외 발생")
        void validateAndGetStudent_InvalidRequest_ThrowsException() {
            // Given
            given(studentRepository.findByNameAndPhoneAndBirthDate(
                    any(), any(), any()))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentAuthService.validateAndGetStudent(invalidRequest))
                    .isInstanceOf(StudentNotFoundException.class)
                    .hasMessage("입력하신 정보와 일치하는 학생을 찾을 수 없습니다. 이름, 생년월일, 전화번호를 다시 확인해 주세요.");

            // Repository 메서드 호출 검증
            verify(studentRepository).findByNameAndPhoneAndBirthDate(
                    invalidRequest.name(), invalidRequest.phone(), invalidRequest.birthDate());
        }

        @Test
        @DisplayName("null 요청으로 예외 발생")
        void validateAndGetStudent_NullRequest_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> studentAuthService.validateAndGetStudent(null))
                    .isInstanceOf(NullPointerException.class);

            // Repository 메서드 호출되지 않음 검증
            verifyNoInteractions(studentRepository);
        }
    }

    @Nested
    @DisplayName("isValidStudent 메서드 테스트")
    class IsValidStudentTest {

        @Test
        @DisplayName("유효한 학생 인증 정보로 true 반환")
        void isValidStudent_ValidRequest_ReturnsTrue() {
            // Given
            given(studentRepository.findByNameAndPhoneAndBirthDate(
                    validRequest.name(), validRequest.phone(), validRequest.birthDate()))
                    .willReturn(Optional.of(testStudent));

            // When
            boolean result = studentAuthService.isValidStudent(validRequest);

            // Then
            assertThat(result).isTrue();

            // Repository 메서드 호출 검증
            verify(studentRepository).findByNameAndPhoneAndBirthDate(
                    validRequest.name(), validRequest.phone(), validRequest.birthDate());
        }

        @Test
        @DisplayName("무효한 학생 인증 정보로 false 반환")
        void isValidStudent_InvalidRequest_ReturnsFalse() {
            // Given
            given(studentRepository.findByNameAndPhoneAndBirthDate(
                    any(), any(), any()))
                    .willReturn(Optional.empty());

            // When
            boolean result = studentAuthService.isValidStudent(invalidRequest);

            // Then
            assertThat(result).isFalse();

            // Repository 메서드 호출 검증
            verify(studentRepository).findByNameAndPhoneAndBirthDate(
                    invalidRequest.name(), invalidRequest.phone(), invalidRequest.birthDate());
        }

        @Test
        @DisplayName("null 요청으로 false 반환")
        void isValidStudent_NullRequest_ReturnsFalse() {
            // When
            boolean result = studentAuthService.isValidStudent(null);

            // Then
            assertThat(result).isFalse();

            // Repository 메서드 호출되지 않음 검증
            verifyNoInteractions(studentRepository);
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("Repository에서 예외 발생 시 전파")
        void validateAndGetStudent_RepositoryException_PropagatesException() {
            // Given
            given(studentRepository.findByNameAndPhoneAndBirthDate(any(), any(), any()))
                    .willThrow(new RuntimeException("데이터베이스 연결 오류"));

            // When & Then
            assertThatThrownBy(() -> studentAuthService.validateAndGetStudent(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("데이터베이스 연결 오류");
        }

        @Test
        @DisplayName("이름에 공백이 포함된 경우 정상 처리")
        void validateAndGetStudent_NameWithSpaces_Success() {
            // Given
            Student studentWithSpaceName = Student.builder()
                    .id(2L)
                    .name("홍 길 동")
                    .phone("010-1234-5678")
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .build();

            StudentAuthRequest requestWithSpaceName = new StudentAuthRequest(
                    "홍 길 동",
                    LocalDate.of(2000, 1, 1),
                    "010-1234-5678"
            );

            given(studentRepository.findByNameAndPhoneAndBirthDate(
                    "홍 길 동", "010-1234-5678", LocalDate.of(2000, 1, 1)))
                    .willReturn(Optional.of(studentWithSpaceName));

            // When
            Student result = studentAuthService.validateAndGetStudent(requestWithSpaceName);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("홍 길 동");
        }

        @Test
        @DisplayName("전화번호 형식이 다른 경우 검증")
        void validateAndGetStudent_DifferentPhoneFormat_HandledCorrectly() {
            // Given
            StudentAuthRequest requestWithDashlessPhone = new StudentAuthRequest(
                    "홍길동",
                    LocalDate.of(2000, 1, 1),
                    "01012345678"  // 하이픈 없는 형식
            );

            given(studentRepository.findByNameAndPhoneAndBirthDate(
                    "홍길동", "01012345678", LocalDate.of(2000, 1, 1)))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentAuthService.validateAndGetStudent(requestWithDashlessPhone))
                    .isInstanceOf(StudentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTest {

        @Test
        @DisplayName("대량 요청 처리 성능 테스트")
        void validateAndGetStudent_BulkRequests_PerformanceTest() {
            // Given
            given(studentRepository.findByNameAndPhoneAndBirthDate(any(), any(), any()))
                    .willReturn(Optional.of(testStudent));

            // When
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 1000; i++) {
                studentAuthService.validateAndGetStudent(validRequest);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(duration).isLessThan(5000); // 5초 이내 완료
            
            // Repository 호출 횟수 검증
            verify(studentRepository, times(1000)).findByNameAndPhoneAndBirthDate(any(), any(), any());
        }
    }
}