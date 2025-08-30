package com.iroomclass.springbackend.domain.admin.question.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.iroomclass.springbackend.domain.admin.question.entity.Question.Difficulty;
import com.iroomclass.springbackend.domain.admin.question.entity.Question.QuestionType;
import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;
import com.iroomclass.springbackend.domain.admin.unit.entity.UnitCategory;
import com.iroomclass.springbackend.domain.admin.unit.entity.UnitSubcategory;

/**
 * Question 엔티티의 scoring_rubric 필드 테스트
 * 
 * answer_key → answer_text 변경 및 scoring_rubric 필드 추가 관련 테스트
 * 
 * @author Claude Code Assistant
 * @since 2025
 */
@SpringBootTest
@DisplayName("Question 엔티티 - scoring_rubric 필드 테스트")
class QuestionScoringRubricTest {
    
    /**
     * 테스트용 기본 단원 생성
     */
    private Unit createTestUnit() {
        UnitCategory category = UnitCategory.builder()
            .id(UUID.randomUUID())
            .categoryName("수와 연산")
            .build();
            
        UnitSubcategory subcategory = UnitSubcategory.builder()
            .id(UUID.randomUUID())
            .subcategoryName("자연수")
            .category(category)
            .build();
            
        return Unit.builder()
            .id(UUID.randomUUID())
            .unitName("자연수와 0")
            .description("자연수와 0에 대한 이해")
            .subcategory(subcategory)
            .build();
    }
    
    @Test
    @DisplayName("주관식 문제 - answerText와 scoringRubric 필드 정상 동작")
    void subjectiveQuestionWithScoringRubric() {
        // Given
        Unit unit = createTestUnit();
        String answerText = "x = 3, y = 2";
        String scoringRubric = "연산 과정 50%, 최종 답안 50%로 평가. 단위를 표기하지 않으면 -5점";
        
        // When
        Question question = Question.builder()
            .unit(unit)
            .difficulty(Difficulty.중)
            .questionText("[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"x + y = 5, x - y = 1일 때 x, y를 구하시오\"}]}]")
            .answerText(answerText)
            .scoringRubric(scoringRubric)
            .questionType(QuestionType.SUBJECTIVE)
            .build();
        
        // Then
        assertThat(question.getAnswerText()).isEqualTo(answerText);
        assertThat(question.getScoringRubric()).isEqualTo(scoringRubric);
        assertThat(question.getQuestionType()).isEqualTo(QuestionType.SUBJECTIVE);
        assertThat(question.isSubjective()).isTrue();
        assertThat(question.isMultipleChoice()).isFalse();
    }
    
    @Test
    @DisplayName("객관식 문제 - scoringRubric은 선택적 필드")
    void multipleChoiceQuestionWithOptionalScoringRubric() {
        // Given
        Unit unit = createTestUnit();
        String choices = "{\"1\": \"0\", \"2\": \"-1\", \"3\": \"3\", \"4\": \"1.5\", \"5\": \"모든 것\"}";
        String scoringRubric = "정답 선택 시 만점, 오답 시 0점";
        
        // When
        Question question = Question.builder()
            .unit(unit)
            .difficulty(Difficulty.하)
            .questionText("[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"다음 중 자연수는?\"}]}]")
            .answerText("3번")  // 객관식도 answerText 사용 (정답 설명)
            .scoringRubric(scoringRubric)
            .questionType(QuestionType.MULTIPLE_CHOICE)
            .choices(choices)
            .correctChoice(3)
            .build();
        
        // Then
        assertThat(question.getAnswerText()).isEqualTo("3번");
        assertThat(question.getScoringRubric()).isEqualTo(scoringRubric);
        assertThat(question.getQuestionType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(question.getCorrectChoice()).isEqualTo(3);
        assertThat(question.isSubjective()).isFalse();
        assertThat(question.isMultipleChoice()).isTrue();
    }
    
    @Test
    @DisplayName("scoringRubric null 값 허용 테스트")
    void questionWithNullScoringRubric() {
        // Given
        Unit unit = createTestUnit();
        
        // When
        Question question = Question.builder()
            .unit(unit)
            .difficulty(Difficulty.하)
            .questionText("[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"간단한 문제\"}]}]")
            .answerText("간단한 답안")
            .scoringRubric(null)  // null 값 허용
            .questionType(QuestionType.SUBJECTIVE)
            .build();
        
        // Then
        assertThat(question.getAnswerText()).isEqualTo("간단한 답안");
        assertThat(question.getScoringRubric()).isNull();
        assertThat(question.getQuestionType()).isEqualTo(QuestionType.SUBJECTIVE);
    }
    
    @Test
    @DisplayName("scoringRubric 빈 문자열 테스트")
    void questionWithEmptyScoringRubric() {
        // Given
        Unit unit = createTestUnit();
        
        // When
        Question question = Question.builder()
            .unit(unit)
            .difficulty(Difficulty.중)
            .questionText("[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"또 다른 문제\"}]}]")
            .answerText("또 다른 답안")
            .scoringRubric("")  // 빈 문자열 허용
            .questionType(QuestionType.SUBJECTIVE)
            .build();
        
        // Then
        assertThat(question.getAnswerText()).isEqualTo("또 다른 답안");
        assertThat(question.getScoringRubric()).isEmpty();
        assertThat(question.getQuestionType()).isEqualTo(QuestionType.SUBJECTIVE);
    }
    
    @Test
    @DisplayName("복잡한 채점 기준 텍스트 테스트")
    void questionWithComplexScoringRubric() {
        // Given
        Unit unit = createTestUnit();
        String complexRubric = """
            채점 기준:
            1. 공식 적용 (30점): 올바른 공식을 사용했는지 확인
            2. 계산 과정 (40점): 단계별 계산이 정확한지 확인  
            3. 최종 답안 (20점): 올바른 답을 도출했는지 확인
            4. 단위 표기 (10점): 적절한 단위를 표기했는지 확인
            
            주의사항:
            - 과정 없이 답만 있는 경우: 최대 30점
            - 계산 실수는 부분점수 부여
            - 단위 누락 시 5점 감점
            """;
        
        // When  
        Question question = Question.builder()
            .unit(unit)
            .difficulty(Difficulty.상)
            .questionText("[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"복잡한 물리 문제\"}]}]")
            .answerText("v = 10m/s")
            .scoringRubric(complexRubric)
            .questionType(QuestionType.SUBJECTIVE)
            .build();
        
        // Then
        assertThat(question.getAnswerText()).isEqualTo("v = 10m/s");
        assertThat(question.getScoringRubric()).isEqualTo(complexRubric);
        assertThat(question.getScoringRubric()).contains("채점 기준:");
        assertThat(question.getScoringRubric()).contains("공식 적용");
        assertThat(question.getScoringRubric()).contains("단위 표기");
    }
}