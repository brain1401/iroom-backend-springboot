package com.iroomclass.springbackend.domain.admin.exam.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ExamSheetCreateRequest DTO 검증 테스트
 * 
 * Bean Validation과 Compact Constructor 검증 규칙을 테스트합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@DisplayName("ExamSheetCreateRequest 검증 테스트")
class ExamSheetCreateRequestValidationTest {

    private Validator validator;
    
    // 기본 유효한 값들
    private static final String VALID_EXAM_NAME = "1학년 중간고사";
    private static final Integer VALID_GRADE = 1;
    private static final Integer VALID_TOTAL_QUESTIONS = 20;
    private static final Integer VALID_MULTIPLE_CHOICE_COUNT = 15;
    private static final Integer VALID_SUBJECTIVE_COUNT = 5;
    private static final List<Long> VALID_UNIT_IDS = List.of(1L, 2L, 3L);

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("유효한 요청 테스트")
    class ValidRequestTest {

        @Test
        @DisplayName("모든 필드가 유효한 경우 - 검증 통과")
        void createValidRequest_NoViolations() {
            // When
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    VALID_EXAM_NAME, VALID_GRADE, VALID_TOTAL_QUESTIONS,
                    VALID_MULTIPLE_CHOICE_COUNT, VALID_SUBJECTIVE_COUNT, VALID_UNIT_IDS
            );

            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("최소값 경계 테스트 - 모든 필드 최소값")
        void createMinBoundaryRequest_NoViolations() {
            // Given
            List<Long> minUnitIds = List.of(1L);

            // When
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    "시험", 1, 1, 0, 1, minUnitIds
            );

            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("최대값 경계 테스트 - 모든 필드 최대값")
        void createMaxBoundaryRequest_NoViolations() {
            // Given
            List<Long> maxUnitIds = List.of(1L, 2L, 3L, 4L, 5L);

            // When
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    "매우 긴 시험지 이름입니다", 3, 30, 15, 15, maxUnitIds
            );

            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("시험지 이름 검증 테스트")
    class ExamNameValidationTest {

        @Test
        @DisplayName("시험지 이름이 null인 경우 - 검증 실패")
        void createRequestWithNullExamName_HasViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().examName(null).build()
            );

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<ExamSheetCreateRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("examName");
            assertThat(violation.getMessage()).isEqualTo("시험지 이름은 필수입니다.");
        }

        @Test
        @DisplayName("시험지 이름이 빈 문자열인 경우 - 검증 실패")
        void createRequestWithEmptyExamName_HasViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().examName("").build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("시험지 이름은 필수입니다.");
        }

        @Test
        @DisplayName("시험지 이름이 공백만 있는 경우 - 검증 실패")
        void createRequestWithBlankExamName_HasViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().examName("   ").build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("시험지 이름은 필수입니다.");
        }
    }

    @Nested
    @DisplayName("학년 검증 테스트")
    class GradeValidationTest {

        @Test
        @DisplayName("학년이 null인 경우 - 검증 실패")
        void createRequestWithNullGrade_HasViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().grade(null).build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("학년은 필수입니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("학년이 1 미만인 경우 - 검증 실패")
        void createRequestWithInvalidLowGrade_HasViolations(int invalidGrade) {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().grade(invalidGrade).build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("학년은 1 이상이어야 합니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {4, 5, 10})
        @DisplayName("학년이 3 초과인 경우 - 검증 실패")
        void createRequestWithInvalidHighGrade_HasViolations(int invalidGrade) {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().grade(invalidGrade).build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("학년은 3 이하여야 합니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3})
        @DisplayName("유효한 학년 - 검증 통과")
        void createRequestWithValidGrade_NoViolations(int validGrade) {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().grade(validGrade).build()
            );

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("총 문제 개수 검증 테스트")
    class TotalQuestionsValidationTest {

        @Test
        @DisplayName("총 문제 개수가 null인 경우 - 검증 실패")
        void createRequestWithNullTotalQuestions_HasViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().totalQuestions(null).build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("총 문제 개수는 필수입니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("총 문제 개수가 1 미만인 경우 - 검증 실패")
        void createRequestWithInvalidLowTotalQuestions_HasViolations(int invalidCount) {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder()
                            .totalQuestions(invalidCount)
                            .multipleChoiceCount(0)
                            .subjectiveCount(Math.max(0, invalidCount))
                            .build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("총 문제 개수는 1 이상이어야 합니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {31, 50, 100})
        @DisplayName("총 문제 개수가 30 초과인 경우 - 검증 실패")
        void createRequestWithInvalidHighTotalQuestions_HasViolations(int invalidCount) {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder()
                            .totalQuestions(invalidCount)
                            .multipleChoiceCount(15)
                            .subjectiveCount(invalidCount - 15)
                            .build()
            );

            // Then
            assertThat(violations).hasSize(2); // totalQuestions 범위 초과 + subjectiveCount 범위 초과
            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .containsAnyOf("총 문제 개수는 30 이하여야 합니다.", "주관식 문제 개수는 30 이하여야 합니다.");
        }
    }

    @Nested
    @DisplayName("문제 타입별 개수 검증 테스트")
    class QuestionTypeCountValidationTest {

        @Test
        @DisplayName("객관식 문제 개수가 null인 경우 - 검증 실패")
        void createRequestWithNullMultipleChoiceCount_HasViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().multipleChoiceCount(null).build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("객관식 문제 개수는 필수입니다.");
        }

        @Test
        @DisplayName("주관식 문제 개수가 null인 경우 - 검증 실패")
        void createRequestWithNullSubjectiveCount_HasViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().subjectiveCount(null).build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("주관식 문제 개수는 필수입니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -5, -10})
        @DisplayName("객관식 문제 개수가 음수인 경우 - 검증 실패")
        void createRequestWithNegativeMultipleChoiceCount_HasViolations(int negativeCount) {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder()
                            .multipleChoiceCount(negativeCount)
                            .totalQuestions(VALID_SUBJECTIVE_COUNT)
                            .build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("객관식 문제 개수는 0 이상이어야 합니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {31, 50, 100})
        @DisplayName("객관식 문제 개수가 30 초과인 경우 - 검증 실패")
        void createRequestWithTooHighMultipleChoiceCount_HasViolations(int highCount) {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder()
                            .multipleChoiceCount(highCount)
                            .totalQuestions(highCount + VALID_SUBJECTIVE_COUNT)
                            .build()
            );

            // Then
            assertThat(violations).hasSize(2); // multipleChoiceCount 범위 초과 + totalQuestions 범위 초과
            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .containsAnyOf("객관식 문제 개수는 30 이하여야 합니다.", "총 문제 개수는 30 이하여야 합니다.");
        }
    }

    @Nested
    @DisplayName("단원 ID 목록 검증 테스트")
    class UnitIdsValidationTest {

        @Test
        @DisplayName("단원 ID 목록이 null인 경우 - 검증 실패")
        void createRequestWithNullUnitIds_HasViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().unitIds(null).build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("선택된 단원은 최소 1개 이상이어야 합니다.");
        }

        @Test
        @DisplayName("단원 ID 목록이 빈 리스트인 경우 - 검증 실패")
        void createRequestWithEmptyUnitIds_HasViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().unitIds(new ArrayList<>()).build()
            );

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("선택된 단원은 최소 1개 이상이어야 합니다.");
        }

        @Test
        @DisplayName("단원 ID 목록이 유효한 경우 - 검증 통과")
        void createRequestWithValidUnitIds_NoViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder().unitIds(List.of(1L, 2L, 3L)).build()
            );

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Compact Constructor 검증 테스트")
    class CompactConstructorValidationTest {

        @Test
        @DisplayName("문제 개수 합계와 총 문제 개수 불일치 - 예외 발생")
        void createRequestWithMismatchedQuestionCount_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new ExamSheetCreateRequest(
                    VALID_EXAM_NAME, VALID_GRADE, 20, 10, 5, VALID_UNIT_IDS
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("객관식 문제 개수(10) + 주관식 문제 개수(5) = 15가 총 문제 개수(20)와 일치하지 않습니다.");
        }

        @Test
        @DisplayName("모든 문제 개수가 0인 경우 - 예외 발생")
        void createRequestWithZeroQuestions_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new ExamSheetCreateRequest(
                    VALID_EXAM_NAME, VALID_GRADE, 0, 0, 0, VALID_UNIT_IDS
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("객관식 또는 주관식 문제 중 최소 1개는 있어야 합니다.");
        }

        @Test
        @DisplayName("객관식만 있는 경우 - 검증 통과")
        void createRequestWithOnlyMultipleChoice_Success() {
            // When & Then - 예외가 발생하지 않아야 함
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    VALID_EXAM_NAME, VALID_GRADE, 10, 10, 0, VALID_UNIT_IDS
            );

            assertThat(request.totalQuestions()).isEqualTo(10);
            assertThat(request.multipleChoiceCount()).isEqualTo(10);
            assertThat(request.subjectiveCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("주관식만 있는 경우 - 검증 통과")
        void createRequestWithOnlySubjective_Success() {
            // When & Then - 예외가 발생하지 않아야 함
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    VALID_EXAM_NAME, VALID_GRADE, 8, 0, 8, VALID_UNIT_IDS
            );

            assertThat(request.totalQuestions()).isEqualTo(8);
            assertThat(request.multipleChoiceCount()).isEqualTo(0);
            assertThat(request.subjectiveCount()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("복합 검증 테스트")
    class ComplexValidationTest {

        @Test
        @DisplayName("여러 필드에서 동시 검증 실패")
        void createRequestWithMultipleViolations_HasMultipleViolations() {
            // When
            Set<ConstraintViolation<ExamSheetCreateRequest>> violations = validator.validate(
                    createRequestBuilder()
                            .examName("")
                            .grade(null)
                            .totalQuestions(-1)
                            .multipleChoiceCount(-1)
                            .subjectiveCount(null)
                            .unitIds(new ArrayList<>())
                            .build()
            );

            // Then
            assertThat(violations).hasSize(6);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "시험지 이름은 필수입니다.",
                            "학년은 필수입니다.",
                            "총 문제 개수는 1 이상이어야 합니다.",
                            "객관식 문제 개수는 0 이상이어야 합니다.",
                            "주관식 문제 개수는 필수입니다.",
                            "선택된 단원은 최소 1개 이상이어야 합니다."
                    );
        }
    }

    // Helper method for creating request builder
    private ExamSheetCreateRequestBuilder createRequestBuilder() {
        return new ExamSheetCreateRequestBuilder()
                .examName(VALID_EXAM_NAME)
                .grade(VALID_GRADE)
                .totalQuestions(VALID_TOTAL_QUESTIONS)
                .multipleChoiceCount(VALID_MULTIPLE_CHOICE_COUNT)
                .subjectiveCount(VALID_SUBJECTIVE_COUNT)
                .unitIds(VALID_UNIT_IDS);
    }

    // Builder pattern for test convenience
    private static class ExamSheetCreateRequestBuilder {
        private String examName;
        private Integer grade;
        private Integer totalQuestions;
        private Integer multipleChoiceCount;
        private Integer subjectiveCount;
        private List<Long> unitIds;

        ExamSheetCreateRequestBuilder examName(String examName) {
            this.examName = examName;
            return this;
        }

        ExamSheetCreateRequestBuilder grade(Integer grade) {
            this.grade = grade;
            return this;
        }

        ExamSheetCreateRequestBuilder totalQuestions(Integer totalQuestions) {
            this.totalQuestions = totalQuestions;
            return this;
        }

        ExamSheetCreateRequestBuilder multipleChoiceCount(Integer multipleChoiceCount) {
            this.multipleChoiceCount = multipleChoiceCount;
            return this;
        }

        ExamSheetCreateRequestBuilder subjectiveCount(Integer subjectiveCount) {
            this.subjectiveCount = subjectiveCount;
            return this;
        }

        ExamSheetCreateRequestBuilder unitIds(List<Long> unitIds) {
            this.unitIds = unitIds;
            return this;
        }

        ExamSheetCreateRequest build() {
            return new ExamSheetCreateRequest(examName, grade, totalQuestions, 
                    multipleChoiceCount, subjectiveCount, unitIds);
        }
    }
}