package com.iroomclass.springbackend.domain.user.exam.result.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult.ResultStatus;
import com.iroomclass.springbackend.domain.user.exam.result.repository.ExamResultRepository;

/**
 * ExamResultService 단위 테스트
 * 
 * AI 기반 자동 채점 서비스의 비즈니스 로직을 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
class ExamResultServiceTest {

    @Mock
    private ExamResultRepository examResultRepository;
    
    @Mock
    private ExamSubmissionRepository examSubmissionRepository;
    
    @Mock
    private QuestionResultService questionResultService;
    
    @InjectMocks
    private ExamResultService examResultService;
    
    private UUID testSubmissionId;
    private ExamSubmission testSubmission;
    
    @BeforeEach
    void setUp() {
        testSubmissionId = UUID.randomUUID();
        testSubmission = ExamSubmission.builder()
            .id(testSubmissionId)
            .submittedAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @DisplayName("AI 자동 채점 시작 - 성공")
    void startAutoGrading_Success() {
        // Given
        ExamResult mockResult = ExamResult.builder()
            .examSubmission(testSubmission)
            .status(ResultStatus.IN_PROGRESS)
            .version(1)
            .gradedAt(LocalDateTime.now())
            .build();
        
        when(examSubmissionRepository.findById(testSubmissionId)).thenReturn(Optional.of(testSubmission));
        when(examResultRepository.save(any(ExamResult.class))).thenReturn(mockResult);
        // QuestionResultService는 void 메서드이므로 별도 Mock 설정 불필요
        
        // When
        ExamResult result = examResultService.startAutoGrading(testSubmissionId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExamSubmission()).isEqualTo(testSubmission);
        assertThat(result.isAutoGrading()).isTrue(); // AI 자동 채점
        assertThat(result.getStatus()).isEqualTo(ResultStatus.IN_PROGRESS);
        
        verify(examSubmissionRepository).findById(testSubmissionId);
        verify(examResultRepository).save(any(ExamResult.class));
    }
    
    @Test
    @DisplayName("채점 완료 처리 - 성공")
    void completeGrading_Success() {
        // Given
        UUID resultId = UUID.randomUUID();
        ExamResult mockResult = ExamResult.builder()
            .id(resultId)
            .examSubmission(testSubmission)
            .status(ResultStatus.IN_PROGRESS)
            .version(1)
            .gradedAt(LocalDateTime.now())
            .build();
        
        when(examResultRepository.findById(resultId)).thenReturn(Optional.of(mockResult));
        when(examResultRepository.save(any(ExamResult.class))).thenReturn(mockResult);
        // QuestionResultService Mock 설정 - 모든 문제 채점 완료 상태
        when(questionResultService.isAllQuestionsGraded(resultId)).thenReturn(true);
        
        // When
        examResultService.completeGrading(resultId, "AI 자동 채점 완료");
        
        // Then
        verify(examResultRepository).findById(resultId);
        verify(examResultRepository).save(any(ExamResult.class));
    }
    
    @Test
    @DisplayName("AI 재채점 시작 - 성공")
    void startRegrading_Success() {
        // Given
        UUID originalResultId = UUID.randomUUID();
        ExamResult originalResult = ExamResult.builder()
            .id(originalResultId)
            .examSubmission(testSubmission)
            .status(ResultStatus.COMPLETED)
            .version(1)
            .gradedAt(LocalDateTime.now())
            .build();
        
        ExamResult newResult = ExamResult.builder()
            .examSubmission(testSubmission)
            .status(ResultStatus.IN_PROGRESS)
            .version(2)
            .gradedAt(LocalDateTime.now())
            .build();
        
        when(examResultRepository.findById(originalResultId)).thenReturn(Optional.of(originalResult));
        when(examResultRepository.save(any(ExamResult.class))).thenReturn(newResult);
        // QuestionResultService.prepareRegrading은 void 메서드이므로 별도 Mock 설정 불필요
        
        // When
        ExamResult result = examResultService.startRegrading(originalResultId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo(2);
        assertThat(result.isAutoGrading()).isTrue();
        assertThat(result.getStatus()).isEqualTo(ResultStatus.IN_PROGRESS);
        
        verify(examResultRepository).findById(originalResultId);
        verify(examResultRepository, times(2)).save(any(ExamResult.class)); // 기존 결과 업데이트 + 새 결과 저장
    }
    
    @Test
    @DisplayName("존재하지 않는 제출물로 채점 시작 - 실패")
    void startAutoGrading_SubmissionNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(examSubmissionRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> examResultService.startAutoGrading(nonExistentId))
            .isInstanceOf(RuntimeException.class);
        
        verify(examSubmissionRepository).findById(nonExistentId);
        verify(examResultRepository, times(0)).save(any(ExamResult.class));
    }
    
    @Test
    @DisplayName("결과 ID로 조회 - 성공")
    void findById_Success() {
        // Given
        UUID resultId = UUID.randomUUID();
        ExamResult mockResult = ExamResult.builder()
            .id(resultId)
            .examSubmission(testSubmission)
            .status(ResultStatus.COMPLETED)
            .version(1)
            .gradedAt(LocalDateTime.now())
            .build();
        
        when(examResultRepository.findById(resultId)).thenReturn(Optional.of(mockResult));
        
        // When
        ExamResult result = examResultService.findById(resultId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(resultId);
        assertThat(result.isAutoGrading()).isTrue();
        
        verify(examResultRepository).findById(resultId);
    }
}