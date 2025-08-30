package com.iroomclass.springbackend.domain.admin.exam.service;

import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetCreateRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetCreateResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetListResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetPreviewResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamSheetQuestionManageResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.QuestionReplaceRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.QuestionSelectionRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.SelectableQuestionsResponse;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheetQuestion;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheetSelectedUnit;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetQuestionRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetSelectedUnitRepository;
import com.iroomclass.springbackend.domain.admin.question.entity.Question;
import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;
import com.iroomclass.springbackend.domain.admin.question.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.admin.unit.repository.UnitRepository;
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
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

/**
 * ExamSheetService 단위 테스트
 * 
 * 시험지 서비스의 핵심 비즈니스 로직을 테스트합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExamSheetService 단위 테스트")
class ExamSheetServiceTest {

    @Mock
    private ExamSheetRepository examSheetRepository;

    @Mock
    private ExamSheetQuestionRepository examSheetQuestionRepository;

    @Mock
    private ExamSheetSelectedUnitRepository examSheetSelectedUnitRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private ExamSheetService examSheetService;

    // 테스트 데이터 setup
    private ExamSheet testExamSheet;
    private Unit testUnit;
    private Question testQuestion1;
    private Question testQuestion2;
    private ExamSheetQuestion testExamSheetQuestion;

    @BeforeEach
    void setUp() {
        // 테스트용 Unit 생성
        testUnit = Unit.builder()
                .id(1L)
                .unitName("1. 수와 연산")
                .grade(1)
                .build();

        // 테스트용 Question 생성
        testQuestion1 = Question.builder()
                .id(1L)
                .unit(testUnit)
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .difficulty(Question.Difficulty.하)
                .questionText(
                        "[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"2 + 2는?\"}]}]")
                .answerKey("4")
                .build();

        testQuestion2 = Question.builder()
                .id(2L)
                .unit(testUnit)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .difficulty(Question.Difficulty.중)
                .questionText(
                        "[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"다음 계산 과정을 설명하시오\"}]}]")
                .answerKey("계산 과정 설명")
                .build();

        // 테스트용 ExamSheet 생성
        testExamSheet = ExamSheet.builder()
                .id(1L)
                .examName("1학년 중간고사")
                .grade(1)
                .totalQuestions(20)
                .multipleChoiceCount(15)
                .subjectiveCount(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 테스트용 ExamSheetQuestion 생성
        testExamSheetQuestion = ExamSheetQuestion.builder()
                .id(1L)
                .examSheet(testExamSheet)
                .question(testQuestion1)
                .seqNo(1)
                .points(5)
                .questionOrder(1)
                .selectionMethod(ExamSheetQuestion.SelectionMethod.MANUAL)
                .build();
    }

    @Nested
    @DisplayName("시험지 생성 테스트")
    class CreateExamSheetTest {

        @Test
        @DisplayName("정상적인 시험지 생성 - 성공")
        void createExamSheet_WithValidRequest_Success() {
            // Given
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    "1학년 중간고사",
                    1,
                    20,
                    15,
                    5,
                    List.of(1L));

            given(unitRepository.findById(1L)).willReturn(Optional.of(testUnit));
            given(questionRepository.countByUnitIdIn(anyList()))
                    .willReturn(50L); // 충분한 문제 수
            given(examSheetRepository.save(any(ExamSheet.class)))
                    .willReturn(testExamSheet);
            given(examSheetSelectedUnitRepository.saveAll(anyList()))
                    .willReturn(List.of());
            // Create sufficient questions for test
            List<Question> availableQuestions = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                Question question = Question.builder()
                        .id((long) (i + 1))
                        .unit(testUnit)
                        .difficulty(Question.Difficulty.중)
                        .questionType(
                                i % 2 == 0 ? Question.QuestionType.MULTIPLE_CHOICE : Question.QuestionType.SUBJECTIVE)
                        .questionText(
                                "[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"테스트 문제 "
                                        + (i + 1) + "\"}]}]")
                        .answerKey("테스트 답안 " + (i + 1))
                        .build();
                availableQuestions.add(question);
            }
            given(questionRepository.findByUnitIdIn(anyList()))
                    .willReturn(availableQuestions);
            given(examSheetQuestionRepository.saveAll(anyList()))
                    .willReturn(List.of());

            // When
            ExamSheetCreateResponse response = examSheetService.createExamSheet(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.examSheetId()).isEqualTo(1L);
            assertThat(response.examName()).isEqualTo("1학년 중간고사");
            assertThat(response.grade()).isEqualTo(1);
            assertThat(response.totalQuestions()).isEqualTo(20);

            verify(examSheetRepository).save(any(ExamSheet.class));
            verify(examSheetSelectedUnitRepository).saveAll(anyList());
            verify(examSheetQuestionRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("잘못된 학년으로 시험지 생성 - 실패")
        void createExamSheet_WithInvalidGrade_ThrowsException() {
            // Given
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    "잘못된 학년 시험지",
                    4, // 잘못된 학년 (1-3만 허용)
                    10,
                    5,
                    5,
                    List.of(1L));

            // When & Then
            assertThatThrownBy(() -> examSheetService.createExamSheet(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("학년은 1, 2, 3만 가능합니다");

            verify(examSheetRepository, never()).save(any(ExamSheet.class));
        }

        @Test
        @DisplayName("문제 개수 초과로 시험지 생성 - 실패")
        void createExamSheet_WithTooManyQuestions_ThrowsException() {
            // Given
            ExamSheetCreateRequest request = new ExamSheetCreateRequest(
                    "문제 개수 초과 시험지",
                    1,
                    25, // 총 25개 문제 요청 (유효 범위)
                    15,
                    10,
                    List.of(1L));

            given(questionRepository.countByUnitIdIn(anyList()))
                    .willReturn(20L); // 20개 문제만 있음 (요청한 25개보다 적음)

            // When & Then
            assertThatThrownBy(() -> examSheetService.createExamSheet(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("문제만 있는데")
                    .hasMessageContaining("문제를 요청했습니다");

            verify(examSheetRepository, never()).save(any(ExamSheet.class));
        }
    }

    @Nested
    @DisplayName("필터링된 시험지 목록 조회 테스트")
    class GetFilteredExamSheetsTest {

        @Test
        @DisplayName("모든 필터 조건으로 시험지 목록 조회 - 성공")
        void getFilteredExamSheets_WithAllFilters_Success() {
            // Given
            Integer grade = 1;
            Integer minQuestions = 10;
            Integer maxQuestions = 30;
            String startDate = "2024-01-01";
            String endDate = "2024-12-31";
            String keyword = "중간고사";
            String sortBy = "createdAt";
            String sortDirection = "desc";

            List<ExamSheet> mockSheets = List.of(testExamSheet);
            Page<ExamSheet> mockPage = new PageImpl<>(mockSheets);

            given(examSheetRepository.findByGradeWithFilters(
                    eq(grade), eq(keyword), eq(null), any(), any(), any()))
                    .willReturn(mockPage);
            given(examSheetSelectedUnitRepository.countByExamSheetId(1L))
                    .willReturn(3L);

            // When
            ExamSheetListResponse response = examSheetService.getFilteredExamSheets(
                    grade, minQuestions, maxQuestions, startDate, endDate,
                    keyword, sortBy, sortDirection);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.grade()).isEqualTo(grade);
            assertThat(response.totalCount()).isEqualTo(1);
            assertThat(response.examSheets()).hasSize(1);

            ExamSheetListResponse.ExamSheetInfo examInfo = response.examSheets().get(0);
            assertThat(examInfo.examSheetId()).isEqualTo(1L);
            assertThat(examInfo.examName()).isEqualTo("1학년 중간고사");
            assertThat(examInfo.grade()).isEqualTo(1);
            assertThat(examInfo.totalQuestions()).isEqualTo(20);
            assertThat(examInfo.selectedUnitCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("문제 개수 범위 필터링 - 범위 밖 제외")
        void getFilteredExamSheets_WithQuestionRangeFilter_FiltersCorrectly() {
            // Given
            ExamSheet sheet1 = ExamSheet.builder()
                    .id(1L)
                    .examName("적은 문제 시험지")
                    .grade(1)
                    .totalQuestions(5) // 범위 밖
                    .build();

            ExamSheet sheet2 = ExamSheet.builder()
                    .id(2L)
                    .examName("적절한 문제 시험지")
                    .grade(1)
                    .totalQuestions(15) // 범위 내
                    .build();

            List<ExamSheet> mockSheets = List.of(sheet1, sheet2);
            Page<ExamSheet> mockPage = new PageImpl<>(mockSheets);

            given(examSheetRepository.findWithFilters(
                    eq(null), eq(null), eq(null), eq(null), any()))
                    .willReturn(mockPage);
            given(examSheetSelectedUnitRepository.countByExamSheetId(anyLong()))
                    .willReturn(2L);

            // When
            ExamSheetListResponse response = examSheetService.getFilteredExamSheets(
                    null, 10, 20, null, null, null, null, null);

            // Then
            assertThat(response.totalCount()).isEqualTo(1); // sheet1은 필터링됨
            assertThat(response.examSheets().get(0).examSheetId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("시험지 문제 추가 테스트")
    class AddQuestionToExamSheetTest {

        @Test
        @DisplayName("시험지에 문제 추가 - 성공")
        void addQuestionToExamSheet_WithValidRequest_Success() {
            // Given
            Long examSheetId = 1L;
            QuestionSelectionRequest request = new QuestionSelectionRequest(
                    2L, // 새로운 문제 ID
                    5, // 배점
                    2 // 문제 순서
            );

            given(examSheetRepository.findById(examSheetId))
                    .willReturn(Optional.of(testExamSheet));
            given(questionRepository.findById(2L))
                    .willReturn(Optional.of(testQuestion2));
            given(examSheetQuestionRepository.findByExamSheetIdAndQuestionId(
                    examSheetId, 2L))
                    .willReturn(Optional.empty()); // 중복 없음
            given(examSheetQuestionRepository.findByExamSheetIdAndQuestionOrderGreaterThanEqual(
                    examSheetId, 2))
                    .willReturn(List.of()); // 재정렬할 기존 문제 없음
            given(examSheetQuestionRepository.save(any(ExamSheetQuestion.class)))
                    .willReturn(testExamSheetQuestion);
            given(examSheetQuestionRepository.findByExamSheetIdOrderByQuestionOrder(examSheetId))
                    .willReturn(List.of(testExamSheetQuestion));

            // When
            ExamSheetQuestionManageResponse response = examSheetService.addQuestionToExamSheet(examSheetId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.examSheetId()).isEqualTo(examSheetId);

            verify(examSheetQuestionRepository).save(any(ExamSheetQuestion.class));
        }

        @Test
        @DisplayName("존재하지 않는 시험지에 문제 추가 - 실패")
        void addQuestionToExamSheet_WithNonExistentExamSheet_ThrowsException() {
            // Given
            Long examSheetId = 999L;
            QuestionSelectionRequest request = new QuestionSelectionRequest(2L, 5, 1);

            given(examSheetRepository.findById(examSheetId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> examSheetService.addQuestionToExamSheet(examSheetId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 시험지입니다");

            verify(examSheetQuestionRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 포함된 문제 추가 - 실패")
        void addQuestionToExamSheet_WithDuplicateQuestion_ThrowsException() {
            // Given
            Long examSheetId = 1L;
            QuestionSelectionRequest request = new QuestionSelectionRequest(1L, 5, 1);

            given(examSheetRepository.findById(examSheetId))
                    .willReturn(Optional.of(testExamSheet));
            given(questionRepository.findById(1L))
                    .willReturn(Optional.of(testQuestion1));
            given(examSheetQuestionRepository.findByExamSheetIdAndQuestionId(
                    examSheetId, 1L))
                    .willReturn(Optional.of(testExamSheetQuestion)); // 중복 있음

            // When & Then
            assertThatThrownBy(() -> examSheetService.addQuestionToExamSheet(examSheetId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 시험지에 포함된 문제입니다");

            verify(examSheetQuestionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("시험지 문제 제거 테스트")
    class RemoveQuestionFromExamSheetTest {

        @Test
        @DisplayName("시험지에서 문제 제거 - 성공")
        void removeQuestionFromExamSheet_WithValidRequest_Success() {
            // Given
            Long examSheetId = 1L;
            Long questionId = 1L;

            given(examSheetRepository.findById(examSheetId))
                    .willReturn(Optional.of(testExamSheet));
            given(examSheetQuestionRepository.findByExamSheetIdAndQuestionId(
                    examSheetId, questionId))
                    .willReturn(Optional.of(testExamSheetQuestion));
            given(examSheetQuestionRepository.findByExamSheetIdAndQuestionOrderGreaterThan(
                    examSheetId, 1))
                    .willReturn(List.of()); // 재정렬할 문제 없음
            given(examSheetQuestionRepository.findByExamSheetIdOrderByQuestionOrder(examSheetId))
                    .willReturn(List.of());

            // When
            ExamSheetQuestionManageResponse response = examSheetService.removeQuestionFromExamSheet(examSheetId,
                    questionId);

            // Then
            assertThat(response).isNotNull();

            verify(examSheetQuestionRepository).delete(testExamSheetQuestion);
        }

        @Test
        @DisplayName("시험지에 포함되지 않은 문제 제거 - 실패")
        void removeQuestionFromExamSheet_WithNonExistentQuestion_ThrowsException() {
            // Given
            Long examSheetId = 1L;
            Long questionId = 999L;

            given(examSheetRepository.findById(examSheetId))
                    .willReturn(Optional.of(testExamSheet));
            given(examSheetQuestionRepository.findByExamSheetIdAndQuestionId(
                    examSheetId, questionId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> examSheetService.removeQuestionFromExamSheet(examSheetId, questionId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시험지에 포함되지 않은 문제입니다");

            verify(examSheetQuestionRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("시험지 문제 교체 테스트")
    class ReplaceQuestionInExamSheetTest {

        @Test
        @DisplayName("시험지 문제 교체 - 성공")
        void replaceQuestionInExamSheet_WithValidRequest_Success() {
            // Given
            Long examSheetId = 1L;
            QuestionReplaceRequest request = new QuestionReplaceRequest(
                    1L, // 기존 문제 ID
                    2L, // 새 문제 ID
                    10, // 새 배점
                    "더 적절한 난이도로 교체");

            given(examSheetRepository.findById(examSheetId))
                    .willReturn(Optional.of(testExamSheet));
            given(examSheetQuestionRepository.findByExamSheetIdOrderByQuestionOrder(examSheetId))
                    .willReturn(List.of(testExamSheetQuestion));
            given(questionRepository.findById(2L))
                    .willReturn(Optional.of(testQuestion2));
            given(examSheetQuestionRepository.save(any(ExamSheetQuestion.class)))
                    .willReturn(testExamSheetQuestion);

            // When
            ExamSheetQuestionManageResponse response = examSheetService.replaceQuestionInExamSheet(examSheetId,
                    request);

            // Then
            assertThat(response).isNotNull();

            verify(examSheetQuestionRepository).save(any(ExamSheetQuestion.class));
        }

        @Test
        @DisplayName("같은 문제로 교체 시도 - 실패 (DTO 레벨에서 검증)")
        void replaceQuestionInExamSheet_WithSameQuestion_ThrowsException() {
            // Given - DTO compact constructor에서 검증되므로 생성자 호출 시 예외 발생

            // When & Then
            assertThatThrownBy(() -> new QuestionReplaceRequest(
                    1L, // 기존 문제 ID
                    1L, // 같은 문제 ID
                    10,
                    "교체 사유")).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("기존 문제와 새로운 문제가 동일할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("시험지 미리보기 테스트")
    class GetExamSheetPreviewTest {

        @Test
        @DisplayName("시험지 미리보기 조회 - 성공")
        void getExamSheetPreview_WithValidExamSheet_Success() {
            // Given
            Long examSheetId = 1L;

            given(examSheetRepository.findById(examSheetId))
                    .willReturn(Optional.of(testExamSheet));
            given(examSheetQuestionRepository.findByExamSheetIdOrderByQuestionOrder(examSheetId))
                    .willReturn(List.of(testExamSheetQuestion));

            // When
            ExamSheetPreviewResponse response = examSheetService.getExamSheetPreview(examSheetId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.examSheetId()).isEqualTo(examSheetId);
            assertThat(response.examSheetName()).isEqualTo("1학년 중간고사");
            assertThat(response.grade()).isEqualTo(1);
            assertThat(response.totalQuestions()).isEqualTo(20);
            assertThat(response.multipleChoiceCount()).isEqualTo(15);
            assertThat(response.subjectiveCount()).isEqualTo(5);
            assertThat(response.questions()).hasSize(1);
            assertThat(response.statistics()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 시험지 미리보기 - 실패")
        void getExamSheetPreview_WithNonExistentExamSheet_ThrowsException() {
            // Given
            Long examSheetId = 999L;

            given(examSheetRepository.findById(examSheetId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> examSheetService.getExamSheetPreview(examSheetId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 시험지입니다");
        }
    }

    @Nested
    @DisplayName("선택 가능한 문제 조회 테스트")
    class GetSelectableQuestionsTest {

        @Test
        @DisplayName("단원별 선택 가능한 문제 조회 - 성공")
        void getSelectableQuestions_WithUnitFilter_Success() {
            // Given
            Long examSheetId = 1L;
            Long unitId = 1L;

            given(examSheetRepository.findById(examSheetId))
                    .willReturn(Optional.of(testExamSheet));
            given(examSheetQuestionRepository.findByExamSheetIdOrderBySeqNo(examSheetId))
                    .willReturn(List.of(testExamSheetQuestion));
            given(questionRepository.findByUnitId(unitId))
                    .willReturn(List.of(testQuestion1, testQuestion2));
            given(unitRepository.findById(unitId))
                    .willReturn(Optional.of(testUnit));

            // When
            SelectableQuestionsResponse response = examSheetService.getSelectableQuestions(examSheetId, unitId, null,
                    null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.unitId()).isEqualTo(unitId);
            assertThat(response.unitName()).isEqualTo("1. 수와 연산");
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.questions()).hasSize(2);
        }
    }
}