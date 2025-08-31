package com.iroomclass.springbackend.domain.user.exam.result.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroomclass.springbackend.domain.admin.question.entity.Question;
import com.iroomclass.springbackend.domain.user.exam.answer.entity.StudentAnswerSheet;
import com.iroomclass.springbackend.domain.user.exam.answer.repository.StudentAnswerSheetRepository;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult;
import com.iroomclass.springbackend.domain.user.exam.result.entity.QuestionResult;
import com.iroomclass.springbackend.domain.user.exam.result.entity.QuestionResult.GradingMethod;
import com.iroomclass.springbackend.domain.user.exam.result.repository.QuestionResultRepository;

/**
 * QuestionResultService 단위 테스트
 * 
 * Mock을 사용하여 문제별 채점 결과 서비스의 핵심 비즈니스 로직을 테스트합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class QuestionResultServiceTest {
    
    @Mock
    private QuestionResultRepository questionResultRepository;
    
    @Mock
    private StudentAnswerSheetRepository studentAnswerSheetRepository;
    
    @InjectMocks
    private QuestionResultService questionResultService;
    
    private ExamResult testExamResult;
    private ExamSubmission testSubmission;
    private List<StudentAnswerSheet> testAnswers;
    private QuestionResult testQuestionResult;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        testSubmission = ExamSubmission.builder()
            .id(UUID.randomUUID())
            .submittedAt(LocalDateTime.now())
            .build();
            
        testExamResult = ExamResult.builder()
            .id(UUID.randomUUID())
            .examSubmission(testSubmission)
            .build();
            
        Question testQuestion = Question.builder()
            .id(UUID.randomUUID())
            .questionText("[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"value\":\"테스트 문제\"}]}]")
            .answerText("정답")
            .questionType(Question.QuestionType.SUBJECTIVE)
            .difficulty(Question.Difficulty.중)
            .points(10)
            .build();
            
        StudentAnswerSheet testAnswer = StudentAnswerSheet.builder()
            .id(UUID.randomUUID())
            .examSubmission(testSubmission)
            .question(testQuestion)
            .answerText("학생 답안")
            .build();
            
        testAnswers = List.of(testAnswer);
        
        testQuestionResult = QuestionResult.builder()
            .id(UUID.randomUUID())
            .examResult(testExamResult)
            .studentAnswerSheet(testAnswer)
            .question(testQuestion)
            .gradingMethod(GradingMethod.MANUAL)
            .isCorrect(false)
            .score(0)
            .maxScore(10)
            .build();
    }
    
    @Test
    @DisplayName("시험 결과 ID로 문제별 결과 목록 조회 - 성공")
    void findByExamResultId_List_Success() {
        // Given
        UUID examResultId = testExamResult.getId();
        when(questionResultRepository.findByExamResultIdOrderByQuestionOrder(examResultId))
            .thenReturn(List.of(testQuestionResult));
        
        // When
        List<QuestionResult> results = questionResultService.findByExamResultId(examResultId);
        
        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(testQuestionResult);
        verify(questionResultRepository).findByExamResultIdOrderByQuestionOrder(examResultId);
    }
    
    @Test
    @DisplayName("문제별 결과 조회 - 성공")
    void findByExamResultId_Success() {
        // Given
        UUID examResultId = testExamResult.getId();
        when(questionResultRepository.findByExamResultIdOrderByQuestionOrder(examResultId))
            .thenReturn(List.of(testQuestionResult));
        
        // When
        List<QuestionResult> results = questionResultService.findByExamResultId(examResultId);
        
        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(testQuestionResult);
        verify(questionResultRepository).findByExamResultIdOrderByQuestionOrder(examResultId);
    }
    
    @Test
    @DisplayName("문제별 결과 ID로 조회 - 성공")
    void findById_Success() {
        // Given
        UUID questionResultId = testQuestionResult.getId();
        when(questionResultRepository.findById(questionResultId))
            .thenReturn(Optional.of(testQuestionResult));
        
        // When
        QuestionResult result = questionResultService.findById(questionResultId);
        
        // Then
        assertThat(result).isEqualTo(testQuestionResult);
        verify(questionResultRepository).findById(questionResultId);
    }
    
    @Test
    @DisplayName("문제별 결과 ID로 조회 - 결과 없음")
    void findById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(questionResultRepository.findById(nonExistentId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> questionResultService.findById(nonExistentId))
            .isInstanceOf(RuntimeException.class);
            
        verify(questionResultRepository).findById(nonExistentId);
    }
    
    @Test
    @DisplayName("문제별 결과 저장 - 성공")
    void save_Success() {
        // Given
        when(questionResultRepository.findById(testQuestionResult.getId()))
            .thenReturn(Optional.of(testQuestionResult));
        when(questionResultRepository.save(any(QuestionResult.class)))
            .thenReturn(testQuestionResult);
        
        // When
        questionResultService.processManualGrading(testQuestionResult.getId(), 8, true, "잘 풀었습니다");
        
        // Then
        verify(questionResultRepository).findById(testQuestionResult.getId());
        verify(questionResultRepository).save(testQuestionResult);
    }
}