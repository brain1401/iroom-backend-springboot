package com.iroomclass.springbackend.domain.exam.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroomclass.springbackend.common.UUIDv7Generator;
import com.iroomclass.springbackend.domain.unit.entity.Unit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 문제 정보 Entity
 * 
 * 각 단원별로 생성된 문제들을 관리합니다.
 * 주관식과 객관식 문제를 모두 지원하며, JSON 형태로 저장된 문제 내용을 HTML로 변환합니다.
 * 
 * <p>
 * 문제 유형별 특징:
 * </p>
 * <ul>
 * <li>주관식: questionText와 answerText 사용</li>
 * <li>객관식: questionText, choices, correctChoice 사용</li>
 * </ul>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Slf4j
public class Question {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 문제 고유 ID
     * UUIDv7로 생성되는 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * 단원과의 관계
     * ManyToOne: 여러 문제가 하나의 단원에 속함
     * FetchType.LAZY: 필요할 때만 단원 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    /**
     * 문제 난이도
     * 하: 쉬움, 중: 보통, 상: 어려움
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    /**
     * 문제 내용 (JSON 형태)
     * 주관식 문제 내용을 JSON 형태로 저장
     * 예시: [{"type": "paragraph", "content": [{"type": "text", "value": "연립부등식 "},
     * {"type": "latex", "value": "\\begin{cases}..."}]}]
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    /**
     * 문제 정답
     * 문제의 정답을 저장
     */
    @Column(columnDefinition = "LONGTEXT")
    private String answerText;

    /**
     * 채점 기준
     * 문제의 채점 기준이나 평가 루브릭을 저장
     * 주관식 문제의 경우 상세한 채점 기준을 제공하여 일관된 평가가 가능하도록 함
     */
    @Column(columnDefinition = "TEXT")
    private String scoringRubric;

    /**
     * 문제 이미지 (JSON 형태)
     * 문제와 관련된 이미지들을 JSON 형태로 저장
     * 예시: {"images": ["image1.jpg", "image2.png"]}
     */
    @Column(columnDefinition = "JSON")
    private String image;

    /**
     * 문제 유형
     * 주관식(SUBJECTIVE) 또는 객관식(MULTIPLE_CHOICE)
     * 기본값: 주관식 (하위 호환성 보장)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'SUBJECTIVE'")
    @Builder.Default
    private QuestionType questionType = QuestionType.SUBJECTIVE;

    /**
     * 객관식 선택지 (JSON 형태)
     * 객관식 문제일 때만 사용
     * 예시: {"1": "선택지1", "2": "선택지2", "3": "선택지3", "4": "선택지4", "5": "선택지5"}
     */
    @Column(columnDefinition = "JSON")
    private String choices;

    /**
     * 객관식 정답 번호
     * 객관식 문제일 때만 사용 (1, 2, 3, 4, 5)
     */
    @Column
    private Integer correctChoice;

    /**
     * 문제 배점
     * 해당 문제의 점수
     */
    @Column
    @Builder.Default
    private Integer points = 10;

    /**
     * JSON 형태의 questionText를 HTML로 변환
     * 
     * @return HTML 형태의 문제 내용
     */
    public String getQuestionTextAsHtml() {
        try {
            List<Map<String, Object>> questionData = objectMapper.readValue(questionText,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            StringBuilder html = new StringBuilder();

            for (Map<String, Object> block : questionData) {
                String type = (String) block.get("type");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> content = (List<Map<String, Object>>) block.get("content");

                if ("paragraph".equals(type)) {
                    html.append("<p>");
                    for (Map<String, Object> item : content) {
                        String itemType = (String) item.get("type");
                        String value = (String) item.get("value");

                        if ("text".equals(itemType)) {
                            html.append(value);
                        } else if ("latex".equals(itemType)) {
                            html.append("$").append(value).append("$");
                        }
                    }
                    html.append("</p>");
                }
            }

            // 이미지가 있으면 문제 내용 아래에 추가
            List<String> imageUrls = getImageUrls();
            if (!imageUrls.isEmpty()) {
                html.append("<div style='margin-top: 15px;'>");
                for (String imageUrl : imageUrls) {
                    html.append("<img src='").append(imageUrl)
                            .append("' alt='문제 이미지' style='max-width: 100%; height: auto; margin: 10px 0;'/>");
                }
                html.append("</div>");
            }

            return html.toString();

        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
            return questionText; // JSON 파싱 실패 시 원본 반환
        }
    }

    /**
     * 이미지 JSON을 파싱하여 이미지 URL 목록 반환
     * 
     * @return 이미지 URL 목록
     */
    public List<String> getImageUrls() {
        try {
            if (image == null || image.trim().isEmpty()) {
                return List.of();
            }

            Map<String, Object> imageData = objectMapper.readValue(image, new TypeReference<Map<String, Object>>() {
            });
            @SuppressWarnings("unchecked")
            List<String> images = (List<String>) imageData.get("images");

            return images != null ? images : List.of();

        } catch (JsonProcessingException e) {
            log.error("이미지 JSON 파싱 오류: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 객관식 문제 여부 확인
     * 
     * @return 객관식 문제이면 true, 아니면 false
     */
    public boolean isMultipleChoice() {
        return QuestionType.MULTIPLE_CHOICE.equals(this.questionType);
    }

    /**
     * 주관식 문제 여부 확인
     * 
     * @return 주관식 문제이면 true, 아니면 false
     */
    public boolean isSubjective() {
        return QuestionType.SUBJECTIVE.equals(this.questionType);
    }

    /**
     * 객관식 선택지를 Map 형태로 반환
     * 
     * @return 선택지 Map (번호 -> 내용), 주관식이면 빈 Map
     */
    public Map<String, String> getChoicesAsMap() {
        try {
            if (!isMultipleChoice() || choices == null || choices.trim().isEmpty()) {
                return Map.of();
            }

            return objectMapper.readValue(choices, new TypeReference<Map<String, String>>() {
            });

        } catch (JsonProcessingException e) {
            log.error("선택지 JSON 파싱 오류: {}", e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * 객관식 선택지를 HTML 형태로 변환
     * 
     * @return HTML 형태의 선택지 목록
     */
    public String getChoicesAsHtml() {
        if (!isMultipleChoice()) {
            return "";
        }

        Map<String, String> choicesMap = getChoicesAsMap();
        if (choicesMap.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<div class='multiple-choice-options'>");

        // 선택지 번호 순서대로 정렬 (1, 2, 3, 4, 5)
        choicesMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String number = entry.getKey();
                    String content = entry.getValue();
                    html.append("<div class='choice-option'>")
                            .append("<span class='choice-number'>").append(number).append("</span>")
                            .append("<span class='choice-content'>").append(content).append("</span>")
                            .append("</div>");
                });

        html.append("</div>");
        return html.toString();
    }

    /**
     * 정답 검증 메서드 (객관식용)
     * 
     * @param selectedChoice 선택된 답안 번호
     * @return 정답 여부
     */
    public boolean isCorrectChoice(Integer selectedChoice) {
        if (!isMultipleChoice() || selectedChoice == null || correctChoice == null) {
            return false;
        }
        return correctChoice.equals(selectedChoice);
    }

    /**
     * 문제 유형에 따른 완전한 HTML 표시
     * 
     * @return 문제 내용 + 선택지(객관식인 경우) HTML
     */
    public String getCompleteQuestionHtml() {
        StringBuilder html = new StringBuilder();

        // 기본 문제 내용
        html.append(getQuestionTextAsHtml());

        // 객관식인 경우 선택지 추가
        if (isMultipleChoice()) {
            html.append(getChoicesAsHtml());
        }

        return html.toString();
    }

    /**
     * 엔티티 저장 전 UUID 자동 생성
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUIDv7Generator.generate();
        }
    }

    /**
     * 문제 배점 반환
     * 
     * @return 문제 배점
     */
    public Integer getPoints() {
        return points;
    }

    /**
     * 문제 난이도 열거형
     */
    public enum Difficulty {
        하, 중, 상
    }

    /**
     * 문제 유형 열거형
     */
    public enum QuestionType {
        /**
         * 주관식 문제
         * 학생이 직접 답안을 작성하는 형태
         */
        SUBJECTIVE,

        /**
         * 객관식 문제 (5지 선다)
         * 주어진 선택지 중 정답을 선택하는 형태
         */
        MULTIPLE_CHOICE
    }
}
