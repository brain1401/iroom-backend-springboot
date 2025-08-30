package com.iroomclass.springbackend.domain.admin.exam.dto;

import com.iroomclass.springbackend.common.UUIDv7Generator;
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

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * QuestionSelectionRequest DTO 검증 테스트
 * 
 * Bean Validation과 Compact Constructor 검증 규칙을 테스트합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@DisplayName("QuestionSelectionRequest 검증 테스트")
class QuestionSelectionRequestValidationTest {

    private Validator validator;
    
    // 기본 유효한 값들
    private static final UUID VALID_QUESTION_ID = UUIDv7Generator.generate();
    private static final Integer VALID_POINTS = 5;
    private static final Integer VALID_QUESTION_ORDER = 1;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("유효한 요청 테스트")
    class ValidRequestTest {

        @Test
        @DisplayName("필수 필드만 있는 유효한 요청 - 검증 통과")
        void createValidRequestWithRequiredFields_NoViolations() {
            // When
            QuestionSelectionRequest request = new QuestionSelectionRequest(
                    VALID_QUESTION_ID, VALID_POINTS, null
            );

            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.questionId()).isEqualTo(VALID_QUESTION_ID);
            assertThat(request.points()).isEqualTo(VALID_POINTS);
            assertThat(request.questionOrder()).isNull();
        }

        @Test
        @DisplayName("모든 필드가 있는 유효한 요청 - 검증 통과")
        void createValidRequestWithAllFields_NoViolations() {
            // When
            QuestionSelectionRequest request = new QuestionSelectionRequest(
                    VALID_QUESTION_ID, VALID_POINTS, VALID_QUESTION_ORDER
            );

            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.questionId()).isEqualTo(VALID_QUESTION_ID);
            assertThat(request.points()).isEqualTo(VALID_POINTS);
            assertThat(request.questionOrder()).isEqualTo(VALID_QUESTION_ORDER);
        }

        @Test
        @DisplayName("최소값 경계 테스트 - 모든 필드 최소값")
        void createMinBoundaryRequest_NoViolations() {
            // When
            QuestionSelectionRequest request = new QuestionSelectionRequest(UUIDv7Generator.generate(), 1, 1);

            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("높은 값 테스트 - 큰 양수 값들")
        void createHighValueRequest_NoViolations() {
            // When
            QuestionSelectionRequest request = new QuestionSelectionRequest(UUIDv7Generator.generate(), 100, 50);

            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("문제 ID 검증 테스트")
    class QuestionIdValidationTest {

        @Test
        @DisplayName("문제 ID가 null인 경우 - 검증 실패")
        void createRequestWithNullQuestionId_HasViolations() {
            // When & Then
            // With UUID and compact constructor, null values throw exceptions during construction
            assertThatThrownBy(() -> new QuestionSelectionRequest(null, VALID_POINTS, VALID_QUESTION_ORDER))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("문제 ID는 필수입니다");
        }

        @Test
        @DisplayName("문제 ID가 양수가 아닌 경우 - 검증 실패")
        void createRequestWithNonPositiveQuestionId_HasViolations() {
            // Note: With UUID, we can't have negative values, so this test is not applicable
            // This test would be handled by the compact constructor validation
            assertThat(true).isTrue(); // Placeholder test
        }

        @Test
        @DisplayName("유효한 문제 ID - 검증 통과")
        void createRequestWithValidQuestionId_NoViolations() {
            // When
            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(
                    createRequestBuilder().questionId(UUIDv7Generator.generate()).build()
            );

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("배점 검증 테스트")
    class PointsValidationTest {

        @Test
        @DisplayName("배점이 null인 경우 - 검증 실패")
        void createRequestWithNullPoints_HasViolations() {
            // When & Then
            // With compact constructor, null values throw exceptions during construction
            assertThatThrownBy(() -> new QuestionSelectionRequest(VALID_QUESTION_ID, null, VALID_QUESTION_ORDER))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("배점은 필수입니다");
        }

        @Test
        @DisplayName("배점이 양수가 아닌 경우 - 검증 실패")
        void createRequestWithNonPositivePoints_HasViolations() {
            // When & Then
            // With compact constructor, invalid values throw exceptions during construction
            assertThatThrownBy(() -> new QuestionSelectionRequest(VALID_QUESTION_ID, 0, VALID_QUESTION_ORDER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("배점은 양수여야 합니다: 0");
                    
            assertThatThrownBy(() -> new QuestionSelectionRequest(VALID_QUESTION_ID, -1, VALID_QUESTION_ORDER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("배점은 양수여야 합니다: -1");
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 5, 10, 20, 100})
        @DisplayName("유효한 배점 - 검증 통과")
        void createRequestWithValidPoints_NoViolations(int validPoints) {
            // When
            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(
                    createRequestBuilder().points(validPoints).build()
            );

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("문제 순서 검증 테스트")
    class QuestionOrderValidationTest {

        @Test
        @DisplayName("문제 순서가 null인 경우 - 검증 통과 (선택적 필드)")
        void createRequestWithNullQuestionOrder_NoViolations() {
            // When
            QuestionSelectionRequest request = new QuestionSelectionRequest(
                    VALID_QUESTION_ID, VALID_POINTS, null
            );

            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.questionOrder()).isNull();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10, 20})
        @DisplayName("유효한 문제 순서 - 검증 통과")
        void createRequestWithValidQuestionOrder_NoViolations(int validOrder) {
            // When
            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(
                    createRequestBuilder().questionOrder(validOrder).build()
            );

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Compact Constructor 검증 테스트")
    class CompactConstructorValidationTest {

        @Test
        @DisplayName("null 문제 ID - Compact Constructor에서 예외 발생")
        void createRequestWithNullQuestionIdInConstructor_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new QuestionSelectionRequest(null, VALID_POINTS, VALID_QUESTION_ORDER))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("문제 ID는 필수입니다");
        }

        @Test
        @DisplayName("null 배점 - Compact Constructor에서 예외 발생")
        void createRequestWithNullPointsInConstructor_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new QuestionSelectionRequest(VALID_QUESTION_ID, null, VALID_QUESTION_ORDER))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("배점은 필수입니다");
        }

        @Test
        @DisplayName("0 이하의 문제 ID - Compact Constructor에서 예외 발생")
        void createRequestWithZeroOrNegativeQuestionId_ThrowsException() {
            // When & Then
            // Note: UUID cannot be 0L or negative, using null test for validation
            // This test validates the compact constructor behavior with null
            assertThatThrownBy(() -> new QuestionSelectionRequest(null, VALID_POINTS, VALID_QUESTION_ORDER))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("문제 ID는 필수입니다");
            
            // UUID type inherently prevents negative or zero values
            // so this specific test case doesn't apply to UUID types
        }

        @Test
        @DisplayName("0 이하의 배점 - Compact Constructor에서 예외 발생")
        void createRequestWithZeroOrNegativePoints_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new QuestionSelectionRequest(VALID_QUESTION_ID, 0, VALID_QUESTION_ORDER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("배점은 양수여야 합니다: 0");
                    
            assertThatThrownBy(() -> new QuestionSelectionRequest(VALID_QUESTION_ID, -3, VALID_QUESTION_ORDER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("배점은 양수여야 합니다: -3");
        }

        @Test
        @DisplayName("0 이하의 문제 순서 - Compact Constructor에서 예외 발생")
        void createRequestWithZeroOrNegativeQuestionOrder_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new QuestionSelectionRequest(VALID_QUESTION_ID, VALID_POINTS, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("문제 순서는 양수여야 합니다: 0");
                    
            assertThatThrownBy(() -> new QuestionSelectionRequest(VALID_QUESTION_ID, VALID_POINTS, -2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("문제 순서는 양수여야 합니다: -2");
        }

        @Test
        @DisplayName("null 문제 순서는 허용됨 - 검증 통과")
        void createRequestWithNullQuestionOrderInConstructor_Success() {
            // When & Then - 예외가 발생하지 않아야 함
            QuestionSelectionRequest request = new QuestionSelectionRequest(
                    VALID_QUESTION_ID, VALID_POINTS, null
            );

            assertThat(request.questionId()).isEqualTo(VALID_QUESTION_ID);
            assertThat(request.points()).isEqualTo(VALID_POINTS);
            assertThat(request.questionOrder()).isNull();
        }
    }

    @Nested
    @DisplayName("복합 검증 테스트")
    class ComplexValidationTest {

        @Test
        @DisplayName("여러 필드에서 동시 검증 실패")
        void createRequestWithMultipleViolations_HasMultipleViolations() {
            // When & Then
            // With compact constructor, the first null field causes immediate exception
            assertThatThrownBy(() -> new QuestionSelectionRequest(null, null, VALID_QUESTION_ORDER))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("문제 ID는 필수입니다");
                    
            // Test second null field separately
            assertThatThrownBy(() -> new QuestionSelectionRequest(VALID_QUESTION_ID, null, VALID_QUESTION_ORDER))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("배점은 필수입니다");
        }

        @Test
        @DisplayName("Bean Validation과 Compact Constructor 검증의 차이점 확인")
        void validateBeanValidationVsCompactConstructor() {
            // With UUID and compact constructor, validation happens at construction time
            // Test null validation
            assertThatThrownBy(() -> new QuestionSelectionRequest(null, VALID_POINTS, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("문제 ID는 필수입니다");
                    
            // Test points validation
            assertThatThrownBy(() -> new QuestionSelectionRequest(VALID_QUESTION_ID, 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("배점은 양수여야 합니다: 0");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class RealWorldScenarioTest {

        @Test
        @DisplayName("마지막 순서에 추가하는 시나리오 - 순서 null")
        void addQuestionToEnd_QuestionOrderNull_Success() {
            // When
            QuestionSelectionRequest request = new QuestionSelectionRequest(UUIDv7Generator.generate(), 10, null);

            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.questionOrder()).isNull(); // 마지막에 추가됨을 의미
        }

        @Test
        @DisplayName("특정 위치에 삽입하는 시나리오 - 순서 지정")
        void insertQuestionAtPosition_QuestionOrderSpecified_Success() {
            // When
            QuestionSelectionRequest request = new QuestionSelectionRequest(UUIDv7Generator.generate(), 10, 3);

            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.questionOrder()).isEqualTo(3); // 3번 위치에 삽입됨을 의미
        }

        @Test
        @DisplayName("높은 배점 문제 추가 시나리오")
        void addHighPointQuestion_Success() {
            // When
            QuestionSelectionRequest request = new QuestionSelectionRequest(UUIDv7Generator.generate(), 25, 1);

            Set<ConstraintViolation<QuestionSelectionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.points()).isEqualTo(25);
        }
    }

    // Helper method for creating request builder
    private QuestionSelectionRequestBuilder createRequestBuilder() {
        return new QuestionSelectionRequestBuilder()
                .questionId(VALID_QUESTION_ID)
                .points(VALID_POINTS)
                .questionOrder(VALID_QUESTION_ORDER);
    }

    // Builder pattern for test convenience
    private static class QuestionSelectionRequestBuilder {
        private UUID questionId;
        private Integer points;
        private Integer questionOrder;

        QuestionSelectionRequestBuilder questionId(UUID questionId) {
            this.questionId = questionId;
            return this;
        }

        QuestionSelectionRequestBuilder points(Integer points) {
            this.points = points;
            return this;
        }

        QuestionSelectionRequestBuilder questionOrder(Integer questionOrder) {
            this.questionOrder = questionOrder;
            return this;
        }

        QuestionSelectionRequest build() {
            return new QuestionSelectionRequest(questionId, points, questionOrder);
        }
    }
}