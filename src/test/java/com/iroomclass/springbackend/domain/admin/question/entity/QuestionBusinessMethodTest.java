package com.iroomclass.springbackend.domain.admin.question.entity;

import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Question 엔티티 비즈니스 메서드 테스트
 * 
 * Question 엔티티의 비즈니스 로직 메서드들에 대한 포괄적인 테스트를 수행합니다.
 * JSON 파싱, HTML 변환, 문제 유형 확인, 정답 검증 등의 로직을 검증합니다.
 */
@DisplayName("Question 엔티티 비즈니스 메서드 테스트")
class QuestionBusinessMethodTest {
    
    /**
     * 테스트용 Unit 엔티티 생성
     */
    private Unit createTestUnit() {
        return Unit.builder()
            .id(1L)
            .unitName("1. 수와 연산")
            .grade(1)
            .build();
    }
    
    @Nested
    @DisplayName("getQuestionTextAsHtml 메서드 테스트")
    class GetQuestionTextAsHtmlTest {
        
        @Test
        @DisplayName("정상적인 JSON 텍스트를 HTML로 변환")
        void convertValidJsonToHtml() {
            // Given
            String jsonText = "[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"2 + 2는?\"}]}]";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText(jsonText)
                .answerKey("4")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            String html = question.getQuestionTextAsHtml();
            
            // Then
            assertThat(html).isEqualTo("<p>2 + 2는?</p>");
        }
        
        @Test
        @DisplayName("LaTeX 포함 JSON을 HTML로 변환")
        void convertJsonWithLatexToHtml() {
            // Given
            String jsonText = "[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"다음 식을 계산하시오: \"}, {\"type\": \"latex\", \"value\": \"x^2 + 2x + 1\"}]}]";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.중)
                .questionText(jsonText)
                .answerKey("(x+1)^2")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            String html = question.getQuestionTextAsHtml();
            
            // Then
            assertThat(html).isEqualTo("<p>다음 식을 계산하시오: $x^2 + 2x + 1$</p>");
        }
        
        @Test
        @DisplayName("여러 paragraph 포함 JSON을 HTML로 변환")
        void convertMultipleParagraphsToHtml() {
            // Given
            String jsonText = "[" +
                "{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"첫 번째 문단\"}]}," +
                "{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"두 번째 문단\"}]}" +
                "]";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.상)
                .questionText(jsonText)
                .answerKey("답안")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            String html = question.getQuestionTextAsHtml();
            
            // Then
            assertThat(html).isEqualTo("<p>첫 번째 문단</p><p>두 번째 문단</p>");
        }
        
        @Test
        @DisplayName("이미지 포함 시 HTML에 이미지 태그 추가")
        void includeImagesInHtml() {
            // Given
            String jsonText = "[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"문제 내용\"}]}]";
            String imageJson = "{\"images\": [\"image1.jpg\", \"image2.png\"]}";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText(jsonText)
                .answerKey("답안")
                .image(imageJson)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            String html = question.getQuestionTextAsHtml();
            
            // Then
            assertThat(html).contains("<p>문제 내용</p>");
            assertThat(html).contains("<div style='margin-top: 15px;'>");
            assertThat(html).contains("<img src='image1.jpg' alt='문제 이미지'");
            assertThat(html).contains("<img src='image2.png' alt='문제 이미지'");
            assertThat(html).contains("</div>");
        }
        
        @Test
        @DisplayName("잘못된 JSON 형식일 때 원본 텍스트 반환")
        void returnOriginalTextForInvalidJson() {
            // Given
            String invalidJson = "invalid json format";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText(invalidJson)
                .answerKey("답안")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            String html = question.getQuestionTextAsHtml();
            
            // Then
            assertThat(html).isEqualTo(invalidJson);
        }
        
        @Test
        @DisplayName("빈 content 배열일 때 빈 paragraph 생성")
        void handleEmptyContent() {
            // Given
            String jsonText = "[{\"type\": \"paragraph\", \"content\": []}]";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText(jsonText)
                .answerKey("답안")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            String html = question.getQuestionTextAsHtml();
            
            // Then
            assertThat(html).isEqualTo("<p></p>");
        }
    }
    
    @Nested
    @DisplayName("getImageUrls 메서드 테스트")
    class GetImageUrlsTest {
        
        @Test
        @DisplayName("정상적인 이미지 JSON 파싱")
        void parseValidImageJson() {
            // Given
            String imageJson = "{\"images\": [\"image1.jpg\", \"image2.png\", \"image3.gif\"]}";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .image(imageJson)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            List<String> urls = question.getImageUrls();
            
            // Then
            assertThat(urls).containsExactly("image1.jpg", "image2.png", "image3.gif");
        }
        
        @Test
        @DisplayName("이미지가 null인 경우 빈 리스트 반환")
        void returnEmptyListForNullImage() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .image(null)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            List<String> urls = question.getImageUrls();
            
            // Then
            assertThat(urls).isEmpty();
        }
        
        @Test
        @DisplayName("이미지가 빈 문자열인 경우 빈 리스트 반환")
        void returnEmptyListForEmptyImage() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .image("   ")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            List<String> urls = question.getImageUrls();
            
            // Then
            assertThat(urls).isEmpty();
        }
        
        @Test
        @DisplayName("images 키가 없는 JSON인 경우 빈 리스트 반환")
        void returnEmptyListForMissingImagesKey() {
            // Given
            String imageJson = "{\"other\": \"data\"}";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .image(imageJson)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            List<String> urls = question.getImageUrls();
            
            // Then
            assertThat(urls).isEmpty();
        }
        
        @Test
        @DisplayName("images 값이 null인 경우 빈 리스트 반환")
        void returnEmptyListForNullImagesValue() {
            // Given
            String imageJson = "{\"images\": null}";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .image(imageJson)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            List<String> urls = question.getImageUrls();
            
            // Then
            assertThat(urls).isEmpty();
        }
        
        @Test
        @DisplayName("잘못된 이미지 JSON 형식일 때 빈 리스트 반환")
        void returnEmptyListForInvalidImageJson() {
            // Given
            String invalidImageJson = "invalid json";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .image(invalidImageJson)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            List<String> urls = question.getImageUrls();
            
            // Then
            assertThat(urls).isEmpty();
        }
        
        @Test
        @DisplayName("빈 이미지 배열인 경우 빈 리스트 반환")
        void returnEmptyListForEmptyImageArray() {
            // Given
            String imageJson = "{\"images\": []}";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .image(imageJson)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            List<String> urls = question.getImageUrls();
            
            // Then
            assertThat(urls).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("문제 유형 확인 메서드 테스트")
    class QuestionTypeCheckTest {
        
        @Test
        @DisplayName("주관식 문제 유형 확인")
        void checkSubjectiveQuestionType() {
            // Given
            Question subjectiveQuestion = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When & Then
            assertThat(subjectiveQuestion.isSubjective()).isTrue();
            assertThat(subjectiveQuestion.isMultipleChoice()).isFalse();
        }
        
        @Test
        @DisplayName("객관식 문제 유형 확인")
        void checkMultipleChoiceQuestionType() {
            // Given
            Question multipleChoiceQuestion = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("1번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices("{\"1\": \"선택지1\", \"2\": \"선택지2\"}")
                .correctChoice(1)
                .build();
            
            // When & Then
            assertThat(multipleChoiceQuestion.isMultipleChoice()).isTrue();
            assertThat(multipleChoiceQuestion.isSubjective()).isFalse();
        }
        
        @Test
        @DisplayName("기본값은 주관식")
        void defaultQuestionTypeIsSubjective() {
            // Given - questionType을 명시하지 않음
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .build();
            
            // When & Then
            assertThat(question.isSubjective()).isTrue();
            assertThat(question.isMultipleChoice()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("getChoicesAsMap 메서드 테스트")
    class GetChoicesAsMapTest {
        
        @Test
        @DisplayName("객관식 문제의 선택지 JSON을 Map으로 변환")
        void convertValidChoicesJsonToMap() {
            // Given
            String choicesJson = "{\"1\": \"첫 번째 선택지\", \"2\": \"두 번째 선택지\", \"3\": \"세 번째 선택지\"}";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("1번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices(choicesJson)
                .correctChoice(1)
                .build();
            
            // When
            Map<String, String> choicesMap = question.getChoicesAsMap();
            
            // Then
            assertThat(choicesMap).hasSize(3);
            assertThat(choicesMap.get("1")).isEqualTo("첫 번째 선택지");
            assertThat(choicesMap.get("2")).isEqualTo("두 번째 선택지");
            assertThat(choicesMap.get("3")).isEqualTo("세 번째 선택지");
        }
        
        @Test
        @DisplayName("주관식 문제는 빈 Map 반환")
        void returnEmptyMapForSubjectiveQuestion() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .choices("{\"1\": \"선택지1\"}")  // 주관식이므로 무시됨
                .build();
            
            // When
            Map<String, String> choicesMap = question.getChoicesAsMap();
            
            // Then
            assertThat(choicesMap).isEmpty();
        }
        
        @Test
        @DisplayName("choices가 null인 객관식 문제는 빈 Map 반환")
        void returnEmptyMapForNullChoices() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("1번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices(null)
                .correctChoice(1)
                .build();
            
            // When
            Map<String, String> choicesMap = question.getChoicesAsMap();
            
            // Then
            assertThat(choicesMap).isEmpty();
        }
        
        @Test
        @DisplayName("choices가 빈 문자열인 객관식 문제는 빈 Map 반환")
        void returnEmptyMapForEmptyChoices() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("1번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices("   ")
                .correctChoice(1)
                .build();
            
            // When
            Map<String, String> choicesMap = question.getChoicesAsMap();
            
            // Then
            assertThat(choicesMap).isEmpty();
        }
        
        @Test
        @DisplayName("잘못된 choices JSON 형식일 때 빈 Map 반환")
        void returnEmptyMapForInvalidChoicesJson() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("1번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices("invalid json")
                .correctChoice(1)
                .build();
            
            // When
            Map<String, String> choicesMap = question.getChoicesAsMap();
            
            // Then
            assertThat(choicesMap).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("getChoicesAsHtml 메서드 테스트")
    class GetChoicesAsHtmlTest {
        
        @Test
        @DisplayName("객관식 문제의 선택지를 HTML로 변환")
        void convertMultipleChoicesToHtml() {
            // Given
            String choicesJson = "{\"1\": \"첫 번째 선택지\", \"2\": \"두 번째 선택지\", \"3\": \"세 번째 선택지\"}";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("1번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices(choicesJson)
                .correctChoice(1)
                .build();
            
            // When
            String html = question.getChoicesAsHtml();
            
            // Then
            assertThat(html).contains("<div class='multiple-choice-options'>");
            assertThat(html).contains("<div class='choice-option'>");
            assertThat(html).contains("<span class='choice-number'>1</span>");
            assertThat(html).contains("<span class='choice-content'>첫 번째 선택지</span>");
            assertThat(html).contains("<span class='choice-number'>2</span>");
            assertThat(html).contains("<span class='choice-content'>두 번째 선택지</span>");
            assertThat(html).contains("<span class='choice-number'>3</span>");
            assertThat(html).contains("<span class='choice-content'>세 번째 선택지</span>");
            assertThat(html).endsWith("</div>");
        }
        
        @Test
        @DisplayName("선택지 번호 순서대로 정렬되어 HTML 생성")
        void sortChoicesByNumberInHtml() {
            // Given - 의도적으로 순서를 바꿔서 입력
            String choicesJson = "{\"3\": \"세 번째\", \"1\": \"첫 번째\", \"2\": \"두 번째\"}";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("1번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices(choicesJson)
                .correctChoice(1)
                .build();
            
            // When
            String html = question.getChoicesAsHtml();
            
            // Then
            // 1, 2, 3 순서로 나타나는지 확인
            int index1 = html.indexOf("<span class='choice-number'>1</span>");
            int index2 = html.indexOf("<span class='choice-number'>2</span>");
            int index3 = html.indexOf("<span class='choice-number'>3</span>");
            
            assertThat(index1).isLessThan(index2);
            assertThat(index2).isLessThan(index3);
        }
        
        @Test
        @DisplayName("주관식 문제는 빈 문자열 반환")
        void returnEmptyStringForSubjectiveQuestion() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            String html = question.getChoicesAsHtml();
            
            // Then
            assertThat(html).isEmpty();
        }
        
        @Test
        @DisplayName("선택지가 없는 객관식 문제는 빈 문자열 반환")
        void returnEmptyStringForNoChoices() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("1번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices(null)
                .correctChoice(1)
                .build();
            
            // When
            String html = question.getChoicesAsHtml();
            
            // Then
            assertThat(html).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("isCorrectChoice 메서드 테스트")
    class IsCorrectChoiceTest {
        
        @Test
        @DisplayName("객관식 문제에서 정답 번호 일치 시 true 반환")
        void returnTrueForCorrectChoice() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("2번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices("{\"1\": \"선택지1\", \"2\": \"선택지2\", \"3\": \"선택지3\"}")
                .correctChoice(2)
                .build();
            
            // When & Then
            assertThat(question.isCorrectChoice(2)).isTrue();
        }
        
        @Test
        @DisplayName("객관식 문제에서 오답 번호 입력 시 false 반환")
        void returnFalseForWrongChoice() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("2번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices("{\"1\": \"선택지1\", \"2\": \"선택지2\", \"3\": \"선택지3\"}")
                .correctChoice(2)
                .build();
            
            // When & Then
            assertThat(question.isCorrectChoice(1)).isFalse();
            assertThat(question.isCorrectChoice(3)).isFalse();
        }
        
        @Test
        @DisplayName("주관식 문제는 항상 false 반환")
        void returnFalseForSubjectiveQuestion() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When & Then
            assertThat(question.isCorrectChoice(1)).isFalse();
            assertThat(question.isCorrectChoice(2)).isFalse();
        }
        
        @Test
        @DisplayName("선택된 답안이 null인 경우 false 반환")
        void returnFalseForNullSelectedChoice() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("2번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices("{\"1\": \"선택지1\", \"2\": \"선택지2\"}")
                .correctChoice(2)
                .build();
            
            // When & Then
            assertThat(question.isCorrectChoice(null)).isFalse();
        }
        
        @Test
        @DisplayName("정답 번호가 null인 경우 false 반환")
        void returnFalseForNullCorrectChoice() {
            // Given
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText("[{\"type\": \"paragraph\", \"content\": []}]")
                .answerKey("답안")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices("{\"1\": \"선택지1\", \"2\": \"선택지2\"}")
                .correctChoice(null)
                .build();
            
            // When & Then
            assertThat(question.isCorrectChoice(1)).isFalse();
            assertThat(question.isCorrectChoice(2)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("getCompleteQuestionHtml 메서드 테스트")
    class GetCompleteQuestionHtmlTest {
        
        @Test
        @DisplayName("주관식 문제는 문제 내용만 포함한 HTML 반환")
        void returnQuestionTextOnlyForSubjective() {
            // Given
            String jsonText = "[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"주관식 문제 내용\"}]}]";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText(jsonText)
                .answerKey("답안")
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When
            String html = question.getCompleteQuestionHtml();
            
            // Then
            assertThat(html).isEqualTo("<p>주관식 문제 내용</p>");
            assertThat(html).doesNotContain("multiple-choice-options");
        }
        
        @Test
        @DisplayName("객관식 문제는 문제 내용과 선택지 포함한 HTML 반환")
        void returnQuestionTextAndChoicesForMultipleChoice() {
            // Given
            String jsonText = "[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"객관식 문제 내용\"}]}]";
            String choicesJson = "{\"1\": \"선택지1\", \"2\": \"선택지2\"}";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.하)
                .questionText(jsonText)
                .answerKey("1번")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices(choicesJson)
                .correctChoice(1)
                .build();
            
            // When
            String html = question.getCompleteQuestionHtml();
            
            // Then
            assertThat(html).contains("<p>객관식 문제 내용</p>");
            assertThat(html).contains("<div class='multiple-choice-options'>");
            assertThat(html).contains("<span class='choice-number'>1</span>");
            assertThat(html).contains("<span class='choice-content'>선택지1</span>");
            assertThat(html).contains("<span class='choice-number'>2</span>");
            assertThat(html).contains("<span class='choice-content'>선택지2</span>");
        }
        
        @Test
        @DisplayName("이미지가 있는 문제는 이미지도 포함한 HTML 반환")
        void includeImagesInCompleteHtml() {
            // Given
            String jsonText = "[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"이미지 포함 문제\"}]}]";
            String imageJson = "{\"images\": [\"test_image.jpg\"]}";
            String choicesJson = "{\"1\": \"선택지1\", \"2\": \"선택지2\"}";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.중)
                .questionText(jsonText)
                .answerKey("1번")
                .image(imageJson)
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices(choicesJson)
                .correctChoice(1)
                .build();
            
            // When
            String html = question.getCompleteQuestionHtml();
            
            // Then
            assertThat(html).contains("<p>이미지 포함 문제</p>");
            assertThat(html).contains("<img src='test_image.jpg' alt='문제 이미지'");
            assertThat(html).contains("<div class='multiple-choice-options'>");
            assertThat(html).contains("선택지1");
            assertThat(html).contains("선택지2");
        }
        
        @Test
        @DisplayName("선택지가 없는 객관식 문제는 문제 내용만 포함")
        void returnQuestionTextOnlyForMultipleChoiceWithoutChoices() {
            // Given
            String jsonText = "[{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"value\": \"선택지 없는 객관식\"}]}]";
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.상)
                .questionText(jsonText)
                .answerKey("답안")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices(null)
                .correctChoice(null)
                .build();
            
            // When
            String html = question.getCompleteQuestionHtml();
            
            // Then
            assertThat(html).isEqualTo("<p>선택지 없는 객관식</p>");
            assertThat(html).doesNotContain("multiple-choice-options");
        }
    }
    
    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {
        
        @Test
        @DisplayName("완전한 객관식 문제 시나리오 테스트")
        void completeMultipleChoiceQuestionScenario() {
            // Given - 모든 요소를 포함한 완전한 객관식 문제
            String jsonText = "[" +
                "{\"type\": \"paragraph\", \"content\": [" +
                "{\"type\": \"text\", \"value\": \"다음 중 올바른 계산 결과는? \"}, " +
                "{\"type\": \"latex\", \"value\": \"2^3 + 1\"}" +
                "]}" +
                "]";
            String imageJson = "{\"images\": [\"math_formula.png\"]}";
            String choicesJson = "{\"1\": \"7\", \"2\": \"9\", \"3\": \"8\", \"4\": \"10\"}";
            
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.중)
                .questionText(jsonText)
                .answerKey("3번")
                .image(imageJson)
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .choices(choicesJson)
                .correctChoice(2)
                .build();
            
            // When & Then - 각 비즈니스 메서드 동작 확인
            
            // 1. 문제 유형 확인
            assertThat(question.isMultipleChoice()).isTrue();
            assertThat(question.isSubjective()).isFalse();
            
            // 2. 이미지 URL 추출
            List<String> imageUrls = question.getImageUrls();
            assertThat(imageUrls).containsExactly("math_formula.png");
            
            // 3. 선택지 Map 변환
            Map<String, String> choicesMap = question.getChoicesAsMap();
            assertThat(choicesMap).hasSize(4);
            assertThat(choicesMap.get("2")).isEqualTo("9");
            
            // 4. 정답 검증
            assertThat(question.isCorrectChoice(2)).isTrue();
            assertThat(question.isCorrectChoice(1)).isFalse();
            assertThat(question.isCorrectChoice(3)).isFalse();
            
            // 5. HTML 변환
            String questionHtml = question.getQuestionTextAsHtml();
            assertThat(questionHtml).contains("<p>다음 중 올바른 계산 결과는? $2^3 + 1$</p>");
            assertThat(questionHtml).contains("<img src='math_formula.png'");
            
            String choicesHtml = question.getChoicesAsHtml();
            assertThat(choicesHtml).contains("choice-number'>1</span>");
            assertThat(choicesHtml).contains("choice-content'>7</span>");
            
            // 6. 완전한 HTML 생성
            String completeHtml = question.getCompleteQuestionHtml();
            assertThat(completeHtml).contains("다음 중 올바른 계산 결과는?");
            assertThat(completeHtml).contains("$2^3 + 1$");
            assertThat(completeHtml).contains("math_formula.png");
            assertThat(completeHtml).contains("multiple-choice-options");
            assertThat(completeHtml).contains("choice-content'>9</span>");
        }
        
        @Test
        @DisplayName("완전한 주관식 문제 시나리오 테스트")
        void completeSubjectiveQuestionScenario() {
            // Given - 이미지가 포함된 주관식 문제
            String jsonText = "[" +
                "{\"type\": \"paragraph\", \"content\": [" +
                "{\"type\": \"text\", \"value\": \"아래 그래프를 보고 함수의 극값을 구하시오.\"}" +
                "]}" +
                "]";
            String imageJson = "{\"images\": [\"function_graph.png\", \"coordinate_system.jpg\"]}";
            
            Question question = Question.builder()
                .unit(createTestUnit())
                .difficulty(Question.Difficulty.상)
                .questionText(jsonText)
                .answerKey("극대값: f(1) = 3, 극소값: f(-1) = -1")
                .image(imageJson)
                .questionType(Question.QuestionType.SUBJECTIVE)
                .build();
            
            // When & Then - 각 비즈니스 메서드 동작 확인
            
            // 1. 문제 유형 확인
            assertThat(question.isSubjective()).isTrue();
            assertThat(question.isMultipleChoice()).isFalse();
            
            // 2. 이미지 URL 추출
            List<String> imageUrls = question.getImageUrls();
            assertThat(imageUrls).containsExactly("function_graph.png", "coordinate_system.jpg");
            
            // 3. 선택지 관련 메서드는 빈 결과 반환
            assertThat(question.getChoicesAsMap()).isEmpty();
            assertThat(question.getChoicesAsHtml()).isEmpty();
            assertThat(question.isCorrectChoice(1)).isFalse();
            
            // 4. HTML 변환
            String questionHtml = question.getQuestionTextAsHtml();
            assertThat(questionHtml).contains("<p>아래 그래프를 보고 함수의 극값을 구하시오.</p>");
            assertThat(questionHtml).contains("<img src='function_graph.png'");
            assertThat(questionHtml).contains("<img src='coordinate_system.jpg'");
            
            // 5. 완전한 HTML 생성 (주관식이므로 선택지 없음)
            String completeHtml = question.getCompleteQuestionHtml();
            assertThat(completeHtml).isEqualTo(questionHtml);
            assertThat(completeHtml).doesNotContain("multiple-choice-options");
        }
    }
}