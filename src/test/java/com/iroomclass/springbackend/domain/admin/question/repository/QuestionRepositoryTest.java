package com.iroomclass.springbackend.domain.admin.question.repository;

import com.iroomclass.springbackend.domain.admin.question.entity.Question;
import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;
import com.iroomclass.springbackend.domain.admin.unit.entity.UnitCategory;
import com.iroomclass.springbackend.domain.admin.unit.entity.UnitSubcategory;
import com.iroomclass.springbackend.common.UUIDv7Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QuestionRepository 테스트
 * 
 * 문제 Repository의 다양한 필터링 메서드들을 테스트합니다.
 * 단원별, 난이도별, 문제 유형별 조회 메서드의 정확성을 검증합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("QuestionRepository 테스트")
class QuestionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuestionRepository questionRepository;

    // 테스트 데이터
    private Unit unit1;
    private Unit unit2;
    private Unit unit3;
    private Question question1;
    private Question question2;
    private Question question3;
    private Question question4;
    private Question question5;

    @BeforeEach
    void setUp() {
        // UnitCategory 테스트 데이터 생성 (대분류)
        UnitCategory category1 = UnitCategory.builder()
                .categoryName("수와 연산")
                .displayOrder(1)
                .description("수와 연산 관련 대분류")
                .build();
        
        UnitCategory category2 = UnitCategory.builder()
                .categoryName("도형과 공간")
                .displayOrder(2)
                .description("도형과 공간 관련 대분류")
                .build();
        
        entityManager.persistAndFlush(category1);
        entityManager.persistAndFlush(category2);

        // UnitSubcategory 테스트 데이터 생성 (중분류)
        UnitSubcategory subcategory1 = UnitSubcategory.builder()
                .category(category1)
                .subcategoryName("정수와 유리수")
                .displayOrder(1)
                .description("정수와 유리수 관련 중분류")
                .build();
        
        UnitSubcategory subcategory2 = UnitSubcategory.builder()
                .category(category2)
                .subcategoryName("평면도형")
                .displayOrder(1)
                .description("평면도형 관련 중분류")
                .build();
        
        entityManager.persistAndFlush(subcategory1);
        entityManager.persistAndFlush(subcategory2);

        // Unit 테스트 데이터 생성 (세부단원)
        unit1 = Unit.builder()
                .subcategory(subcategory1)
                .unitName("1. 수와 연산")
                .unitCode("UNIT_001")
                .grade(1)
                .displayOrder(1)
                .description("수와 연산 관련 단원")
                .build();
        
        unit2 = Unit.builder()
                .subcategory(subcategory2)
                .unitName("2. 도형과 공간")
                .unitCode("UNIT_002")
                .grade(1)
                .displayOrder(2)
                .description("도형과 공간 관련 단원")
                .build();
        
        unit3 = Unit.builder()
                .subcategory(subcategory1)
                .unitName("3. 분수와 소수")
                .unitCode("UNIT_003")
                .grade(2)
                .displayOrder(1)
                .description("분수와 소수 관련 단원")
                .build();
        
        entityManager.persistAndFlush(unit1);
        entityManager.persistAndFlush(unit2);
        entityManager.persistAndFlush(unit3);
        
        // Question 테스트 데이터 생성
        question1 = Question.builder()
                .unit(unit1)
                .difficulty(Question.Difficulty.하)
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .questionText("[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"2 + 3 = ?\"}]}]")
                .answerText("5")
                .build();
        
        question2 = Question.builder()
                .unit(unit1)
                .difficulty(Question.Difficulty.중)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .questionText("[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"정수의 성질에 대해 설명하시오\"}]}]")
                .answerText("정수는 양수, 0, 음수를 포함한다")
                .build();
        
        question3 = Question.builder()
                .unit(unit1)
                .difficulty(Question.Difficulty.상)
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .questionText("[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"복잡한 수식 계산 문제\"}]}]")
                .answerText("12")
                .build();
        
        question4 = Question.builder()
                .unit(unit2)
                .difficulty(Question.Difficulty.하)
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .questionText("[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"삼각형의 내각의 합은?\"}]}]")
                .answerText("180도")
                .build();
        
        question5 = Question.builder()
                .unit(unit3)
                .difficulty(Question.Difficulty.중)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .questionText("[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"분수의 곱셈 방법\"}]}]")
                .answerText("분자끼리 곱하고 분모끼리 곱한다")
                .build();
        
        entityManager.persistAndFlush(question1);
        entityManager.persistAndFlush(question2);
        entityManager.persistAndFlush(question3);
        entityManager.persistAndFlush(question4);
        entityManager.persistAndFlush(question5);
        
        entityManager.clear();
    }

    @Nested
    @DisplayName("단원별 조회 테스트")
    class FindByUnitTest {

        @Test
        @DisplayName("단원별 문제 목록 조회")
        void findByUnitId_Success() {
            // When
            List<Question> result = questionRepository.findByUnitId(unit1.getId());

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(q -> q.getUnit().getId().equals(unit1.getId()));
        }

        @Test
        @DisplayName("다중 단원별 문제 목록 조회")
        void findByUnitIdIn_Success() {
            // When
            List<Question> result = questionRepository.findByUnitIdIn(
                    Arrays.asList(unit1.getId(), unit2.getId()));

            // Then
            assertThat(result).hasSize(4);
            assertThat(result).allMatch(q -> 
                q.getUnit().getId().equals(unit1.getId()) || 
                q.getUnit().getId().equals(unit2.getId()));
        }

        @Test
        @DisplayName("단원별 문제 수 카운트")
        void countByUnitId_Success() {
            // When
            long count = questionRepository.countByUnitId(unit1.getId());

            // Then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("다중 단원별 문제 수 카운트")
        void countByUnitIdIn_Success() {
            // When
            long count = questionRepository.countByUnitIdIn(
                    Arrays.asList(unit1.getId(), unit2.getId()));

            // Then
            assertThat(count).isEqualTo(4);
        }

        @Test
        @DisplayName("존재하지 않는 단원 - 빈 결과")
        void findByUnitId_NonExistent_EmptyResult() {
            // When
            // Given - 존재하지 않는 UUID 생성
            UUID nonExistentUnitId = UUIDv7Generator.generate();
            
            // When
            List<Question> result = questionRepository.findByUnitId(nonExistentUnitId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("난이도별 조회 테스트")
    class FindByDifficultyTest {

        @Test
        @DisplayName("단원별, 난이도별 문제 목록 조회")
        void findByUnitIdAndDifficulty_Success() {
            // When
            List<Question> result = questionRepository.findByUnitIdAndDifficulty(
                    unit1.getId(), Question.Difficulty.하);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(question1);
            assertThat(result.get(0).getDifficulty()).isEqualTo(Question.Difficulty.하);
        }

        @Test
        @DisplayName("단원별, 난이도별 문제 수 카운트")
        void countByUnitIdAndDifficulty_Success() {
            // When
            long count = questionRepository.countByUnitIdAndDifficulty(
                    unit1.getId(), Question.Difficulty.중);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("단원별, 난이도별 문제 목록 조회 - 문자열 기반")
        void findByUnitIdAndDifficultyName_Success() {
            // When
            List<Question> result = questionRepository.findByUnitIdAndDifficultyName(
                    unit1.getId(), "상");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(question3);
            assertThat(result.get(0).getDifficulty()).isEqualTo(Question.Difficulty.상);
        }
    }

    @Nested
    @DisplayName("문제 유형별 조회 테스트")
    class FindByQuestionTypeTest {

        @Test
        @DisplayName("단원별, 문제 유형별 문제 목록 조회")
        void findByUnitIdAndQuestionType_Success() {
            // When
            List<Question> result = questionRepository.findByUnitIdAndQuestionType(
                    unit1.getId(), Question.QuestionType.MULTIPLE_CHOICE);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(q -> q.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE);
            assertThat(result).containsExactlyInAnyOrder(question1, question3);
        }

        @Test
        @DisplayName("단원별, 난이도별, 문제 유형별 문제 목록 조회")
        void findByUnitIdAndDifficultyNameAndQuestionType_Success() {
            // When
            List<Question> result = questionRepository.findByUnitIdAndDifficultyNameAndQuestionType(
                    unit1.getId(), "중", Question.QuestionType.SUBJECTIVE);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(question2);
            assertThat(result.get(0).getDifficulty()).isEqualTo(Question.Difficulty.중);
            assertThat(result.get(0).getQuestionType()).isEqualTo(Question.QuestionType.SUBJECTIVE);
        }
    }

    @Nested
    @DisplayName("학년별 조회 테스트")
    class FindByGradeTest {

        @Test
        @DisplayName("학년별 문제 목록 조회")
        void findByUnit_Grade_Success() {
            // When
            List<Question> result = questionRepository.findByUnit_Grade(1);

            // Then
            assertThat(result).hasSize(4);
            assertThat(result).allMatch(q -> q.getUnit().getGrade() == 1);
        }

        @Test
        @DisplayName("학년별, 난이도별 문제 목록 조회")
        void findByUnit_GradeAndDifficultyName_Success() {
            // When
            List<Question> result = questionRepository.findByUnit_GradeAndDifficultyName(1, "하");

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(q -> q.getUnit().getGrade() == 1);
            assertThat(result).allMatch(q -> q.getDifficulty() == Question.Difficulty.하);
            assertThat(result).containsExactlyInAnyOrder(question1, question4);
        }

        @Test
        @DisplayName("학년별, 문제 유형별 문제 목록 조회")
        void findByUnit_GradeAndQuestionType_Success() {
            // When
            List<Question> result = questionRepository.findByUnit_GradeAndQuestionType(
                    1, Question.QuestionType.SUBJECTIVE);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(question2);
            assertThat(result.get(0).getUnit().getGrade()).isEqualTo(1);
            assertThat(result.get(0).getQuestionType()).isEqualTo(Question.QuestionType.SUBJECTIVE);
        }

        @Test
        @DisplayName("학년별, 난이도별, 문제 유형별 문제 목록 조회")
        void findByUnit_GradeAndDifficultyNameAndQuestionType_Success() {
            // When
            List<Question> result = questionRepository.findByUnit_GradeAndDifficultyNameAndQuestionType(
                    1, "상", Question.QuestionType.MULTIPLE_CHOICE);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(question3);
            assertThat(result.get(0).getUnit().getGrade()).isEqualTo(1);
            assertThat(result.get(0).getDifficulty()).isEqualTo(Question.Difficulty.상);
            assertThat(result.get(0).getQuestionType()).isEqualTo(Question.QuestionType.MULTIPLE_CHOICE);
        }
    }

    @Nested
    @DisplayName("검색 기능 테스트")
    class SearchTest {

        @Test
        @DisplayName("문제 내용 검색 - 키워드 포함")
        void findByQuestionTextContaining_Success() {
            // When
            List<Question> result = questionRepository.findByQuestionTextContaining("정수");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(question2);
        }

        @Test
        @DisplayName("문제 내용 검색 - 키워드 없음")
        void findByQuestionTextContaining_NoMatch_EmptyResult() {
            // When
            List<Question> result = questionRepository.findByQuestionTextContaining("존재하지않는키워드");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("페이징 조회 테스트")
    class PagingTest {

        @Test
        @DisplayName("단원별 문제 목록 페이징 조회")
        void findByUnitId_WithPaging_Success() {
            // Given
            PageRequest pageable = PageRequest.of(0, 2);

            // When
            Page<Question> result = questionRepository.findByUnitId(unit1.getId(), pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.isFirst()).isTrue();
        }

        @Test
        @DisplayName("단원별 문제 목록 페이징 조회 - 두 번째 페이지")
        void findByUnitId_WithPaging_SecondPage_Success() {
            // Given
            PageRequest pageable = PageRequest.of(1, 2);

            // When
            Page<Question> result = questionRepository.findByUnitId(unit1.getId(), pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("존재하지 않는 학년으로 조회 - 빈 결과")
        void findByUnit_Grade_NonExistentGrade_EmptyResult() {
            // When
            List<Question> result = questionRepository.findByUnit_Grade(5);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 단원 ID 목록으로 조회 - 빈 결과")
        void findByUnitIdIn_EmptyList_EmptyResult() {
            // When
            List<Question> result = questionRepository.findByUnitIdIn(List.of());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 단원들로 조회 - 빈 결과")
        void findByUnitIdIn_NonExistentUnits_EmptyResult() {
            // When
            // Given - 존재하지 않는 UUID들 생성
            UUID nonExistentUnitId1 = UUIDv7Generator.generate();
            UUID nonExistentUnitId2 = UUIDv7Generator.generate();
            
            // When
            List<Question> result = questionRepository.findByUnitIdIn(Arrays.asList(nonExistentUnitId1, nonExistentUnitId2));

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("데이터 무결성 테스트")
    class DataIntegrityTest {

        @Test
        @DisplayName("모든 문제가 유효한 단원을 참조하는지 확인")
        void findAll_ValidUnitReference_Success() {
            // When
            List<Question> allQuestions = questionRepository.findAll();

            // Then
            assertThat(allQuestions).hasSize(5);
            assertThat(allQuestions).allMatch(q -> q.getUnit() != null);
            assertThat(allQuestions).allMatch(q -> q.getUnit().getId() != null);
        }

        @Test
        @DisplayName("난이도 enum 값이 올바르게 저장되는지 확인")
        void findAll_ValidDifficulty_Success() {
            // When
            List<Question> allQuestions = questionRepository.findAll();

            // Then
            assertThat(allQuestions).allMatch(q -> q.getDifficulty() != null);
            assertThat(allQuestions)
                    .extracting(Question::getDifficulty)
                    .containsOnly(Question.Difficulty.하, Question.Difficulty.중, Question.Difficulty.상);
        }

        @Test
        @DisplayName("문제 유형 enum 값이 올바르게 저장되는지 확인")
        void findAll_ValidQuestionType_Success() {
            // When
            List<Question> allQuestions = questionRepository.findAll();

            // Then
            assertThat(allQuestions).allMatch(q -> q.getQuestionType() != null);
            assertThat(allQuestions)
                    .extracting(Question::getQuestionType)
                    .containsOnly(Question.QuestionType.MULTIPLE_CHOICE, Question.QuestionType.SUBJECTIVE);
        }
    }
}