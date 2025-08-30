package com.iroomclass.springbackend.domain.admin.exam.repository;

import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheetSelectedUnit;
import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;
import com.iroomclass.springbackend.domain.admin.unit.entity.UnitCategory;
import com.iroomclass.springbackend.domain.admin.unit.entity.UnitSubcategory;
import com.iroomclass.springbackend.domain.admin.unit.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExamSheetRepository 테스트
 * 
 * 시험지 Repository의 커스텀 쿼리 메서드들을 테스트합니다.
 * 특히 복잡한 필터링 쿼리의 정확성을 검증합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ExamSheetRepository 테스트")
class ExamSheetRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExamSheetRepository examSheetRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private ExamSheetSelectedUnitRepository examSheetSelectedUnitRepository;

    // 테스트 데이터
    private Unit unit1;
    private Unit unit2;
    private Unit unit3;
    private ExamSheet examSheet1;
    private ExamSheet examSheet2;
    private ExamSheet examSheet3;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0);

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

        // ExamSheet 테스트 데이터 생성
        examSheet1 = ExamSheet.builder()
                .examName("1학년 중간고사")
                .grade(1)
                .totalQuestions(20)
                .multipleChoiceCount(15)
                .subjectiveCount(5)
                .createdAt(baseTime)
                .updatedAt(baseTime)
                .build();

        examSheet2 = ExamSheet.builder()
                .examName("1학년 기말고사")
                .grade(1)
                .totalQuestions(25)
                .multipleChoiceCount(20)
                .subjectiveCount(5)
                .createdAt(baseTime.plusDays(10))
                .updatedAt(baseTime.plusDays(10))
                .build();

        examSheet3 = ExamSheet.builder()
                .examName("2학년 중간고사")
                .grade(2)
                .totalQuestions(30)
                .multipleChoiceCount(25)
                .subjectiveCount(5)
                .createdAt(baseTime.plusDays(20))
                .updatedAt(baseTime.plusDays(20))
                .build();

        entityManager.persistAndFlush(examSheet1);
        entityManager.persistAndFlush(examSheet2);
        entityManager.persistAndFlush(examSheet3);

        // ExamSheetSelectedUnit 연결 데이터 생성
        ExamSheetSelectedUnit selectedUnit1_1 = ExamSheetSelectedUnit.builder()
                .examSheet(examSheet1)
                .unit(unit1)
                .build();

        ExamSheetSelectedUnit selectedUnit1_2 = ExamSheetSelectedUnit.builder()
                .examSheet(examSheet1)
                .unit(unit2)
                .build();

        ExamSheetSelectedUnit selectedUnit2_1 = ExamSheetSelectedUnit.builder()
                .examSheet(examSheet2)
                .unit(unit1)
                .build();

        ExamSheetSelectedUnit selectedUnit3_1 = ExamSheetSelectedUnit.builder()
                .examSheet(examSheet3)
                .unit(unit3)
                .build();

        entityManager.persistAndFlush(selectedUnit1_1);
        entityManager.persistAndFlush(selectedUnit1_2);
        entityManager.persistAndFlush(selectedUnit2_1);
        entityManager.persistAndFlush(selectedUnit3_1);

        entityManager.clear();
    }

    @Nested
    @DisplayName("기본 조회 메서드 테스트")
    class BasicQueryTest {

        @Test
        @DisplayName("학년별 시험지 조회 - 최신순 정렬")
        void findByGradeOrderByIdDesc_Success() {
            // When
            List<ExamSheet> result = examSheetRepository.findByGradeOrderByIdDesc(1);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getExamName()).isEqualTo("1학년 기말고사"); // 최신순
            assertThat(result.get(1).getExamName()).isEqualTo("1학년 중간고사");
            assertThat(result).allMatch(examSheet -> examSheet.getGrade() == 1);
        }

        @Test
        @DisplayName("전체 시험지 조회 - 최신순 정렬")
        void findAllByOrderByIdDesc_Success() {
            // When
            List<ExamSheet> result = examSheetRepository.findAllByOrderByIdDesc();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getExamName()).isEqualTo("2학년 중간고사"); // 가장 최신
            assertThat(result.get(1).getExamName()).isEqualTo("1학년 기말고사");
            assertThat(result.get(2).getExamName()).isEqualTo("1학년 중간고사");
        }

        @Test
        @DisplayName("존재하지 않는 학년 조회 - 빈 결과")
        void findByGradeOrderByIdDesc_EmptyResult() {
            // When
            List<ExamSheet> result = examSheetRepository.findByGradeOrderByIdDesc(3);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("필터링 쿼리 테스트")
    class FilteringQueryTest {

        @Test
        @DisplayName("시험지명 필터링 - 부분 일치 검색")
        void findWithFilters_ExamNameFilter_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    "중간고사", null, null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(ExamSheet::getExamName)
                    .allMatch(name -> name.contains("중간고사"));
        }

        @Test
        @DisplayName("단원명 필터링 - 부분 일치 검색")
        void findWithFilters_UnitNameFilter_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    null, "수와 연산", null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            // "1. 수와 연산" 단원이 포함된 시험지들이 조회되어야 함
            assertThat(result.getContent())
                    .extracting(ExamSheet::getExamName)
                    .containsExactly("1학년 기말고사", "1학년 중간고사"); // 최신순
        }

        @Test
        @DisplayName("생성일시 범위 필터링 - From 조건")
        void findWithFilters_CreatedAtFromFilter_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            LocalDateTime fromDate = baseTime.plusDays(5);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    null, null, fromDate, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(ExamSheet::getExamName)
                    .containsExactly("2학년 중간고사", "1학년 기말고사"); // 최신순
        }

        @Test
        @DisplayName("생성일시 범위 필터링 - To 조건")
        void findWithFilters_CreatedAtToFilter_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            LocalDateTime toDate = baseTime.plusDays(15);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    null, null, null, toDate, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(ExamSheet::getExamName)
                    .containsExactly("1학년 기말고사", "1학년 중간고사"); // 최신순
        }

        @Test
        @DisplayName("생성일시 범위 필터링 - From과 To 조건")
        void findWithFilters_CreatedAtRangeFilter_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            LocalDateTime fromDate = baseTime.plusDays(5);
            LocalDateTime toDate = baseTime.plusDays(15);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    null, null, fromDate, toDate, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getExamName()).isEqualTo("1학년 기말고사");
        }

        @Test
        @DisplayName("복합 필터링 - 시험지명 + 단원명")
        void findWithFilters_MultipleFilters_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    "1학년", "수와", null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(examSheet -> examSheet.getExamName().contains("1학년"));
        }

        @Test
        @DisplayName("모든 필터 조건 NULL - 전체 조회")
        void findWithFilters_AllFiltersNull_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    null, null, null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("필터 조건에 매치되는 결과 없음")
        void findWithFilters_NoMatch_EmptyResult() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    "존재하지않는시험지", null, null, null, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("학년별 필터링 쿼리 테스트")
    class GradeFilteringQueryTest {

        @Test
        @DisplayName("학년별 시험지명 필터링")
        void findByGradeWithFilters_ExamNameFilter_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findByGradeWithFilters(
                    1, "기말고사", null, null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getExamName()).isEqualTo("1학년 기말고사");
            assertThat(result.getContent().get(0).getGrade()).isEqualTo(1);
        }

        @Test
        @DisplayName("학년별 단원명 필터링")
        void findByGradeWithFilters_UnitNameFilter_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findByGradeWithFilters(
                    1, null, "도형", null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getExamName()).isEqualTo("1학년 중간고사");
            assertThat(result.getContent().get(0).getGrade()).isEqualTo(1);
        }

        @Test
        @DisplayName("학년별 생성일시 범위 필터링")
        void findByGradeWithFilters_DateRangeFilter_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            LocalDateTime fromDate = baseTime;
            LocalDateTime toDate = baseTime.plusDays(15);

            // When
            Page<ExamSheet> result = examSheetRepository.findByGradeWithFilters(
                    1, null, null, fromDate, toDate, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(examSheet -> examSheet.getGrade() == 1);
            assertThat(result.getContent())
                    .extracting(ExamSheet::getExamName)
                    .containsExactly("1학년 기말고사", "1학년 중간고사"); // 최신순
        }

        @Test
        @DisplayName("학년별 복합 필터링")
        void findByGradeWithFilters_MultipleFilters_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findByGradeWithFilters(
                    1, "중간고사", "수와", null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getExamName()).isEqualTo("1학년 중간고사");
        }

        @Test
        @DisplayName("다른 학년의 시험지는 필터링에서 제외")
        void findByGradeWithFilters_OtherGradeExcluded_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findByGradeWithFilters(
                    2, "중간고사", null, null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getExamName()).isEqualTo("2학년 중간고사");
            assertThat(result.getContent().get(0).getGrade()).isEqualTo(2);
        }

        @Test
        @DisplayName("존재하지 않는 학년으로 필터링 - 빈 결과")
        void findByGradeWithFilters_NonExistentGrade_EmptyResult() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findByGradeWithFilters(
                    3, null, null, null, null, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("페이징 테스트")
    class PagingTest {

        @Test
        @DisplayName("페이징 - 첫 번째 페이지")
        void findWithFilters_FirstPage_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 2);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    null, null, null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isFalse();
        }

        @Test
        @DisplayName("페이징 - 두 번째 페이지")
        void findWithFilters_SecondPage_Success() {
            // Given
            Pageable pageable = PageRequest.of(1, 2);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    null, null, null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("정렬 테스트")
    class SortingTest {

        @Test
        @DisplayName("생성일시 내림차순 정렬 확인")
        void findWithFilters_SortByCreatedAtDesc_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    null, null, null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);

            // 생성일시 내림차순 확인
            LocalDateTime previousCreatedAt = null;
            for (ExamSheet examSheet : result.getContent()) {
                if (previousCreatedAt != null) {
                    assertThat(examSheet.getCreatedAt()).isBefore(previousCreatedAt);
                }
                previousCreatedAt = examSheet.getCreatedAt();
            }
        }
    }

    @Nested
    @DisplayName("DISTINCT 검증 테스트")
    class DistinctTest {

        @Test
        @DisplayName("JOIN으로 인한 중복 제거 확인")
        void findWithFilters_DistinctResults_Success() {
            // Given
            // examSheet1은 unit1, unit2 두 개의 단원과 연결되어 있음
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<ExamSheet> result = examSheetRepository.findWithFilters(
                    "1학년 중간고사", null, null, null, pageable);

            // Then
            // JOIN으로 인해 여러 행이 반환될 수 있지만 DISTINCT로 중복 제거되어야 함
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getExamName()).isEqualTo("1학년 중간고사");
        }
    }
}