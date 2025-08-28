package com.iroomclass.springbackend.domain.admin.question.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 문제 정보 Entity
 * 
 * 각 단원별로 생성된 문제들을 관리합니다.
 * 주관식 문제만 지원하며, JSON 형태로 저장된 문제 내용을 HTML로 변환합니다.
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
@Slf4j
public class Question {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 문제 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
     * 예시: [{"type": "paragraph", "content": [{"type": "text", "value": "연립부등식 "}, {"type": "latex", "value": "\\begin{cases}..."}]}]
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    /**
     * 문제 정답
     * 문제의 정답을 저장
     */
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String answerKey;

    /**
     * 문제 이미지 (JSON 형태)
     * 문제와 관련된 이미지들을 JSON 형태로 저장
     * 예시: {"images": ["image1.jpg", "image2.png"]}
     */
    @Column(columnDefinition = "JSON")
    private String image;

    /**
     * JSON 형태의 questionText를 HTML로 변환
     * 
     * @return HTML 형태의 문제 내용
     */
    public String getQuestionTextAsHtml() {
        try {
            List<Map<String, Object>> questionData = objectMapper.readValue(questionText, new TypeReference<List<Map<String, Object>>>() {});
            StringBuilder html = new StringBuilder();
            
            for (Map<String, Object> block : questionData) {
                String type = (String) block.get("type");
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
                    html.append("<img src='").append(imageUrl).append("' alt='문제 이미지' style='max-width: 100%; height: auto; margin: 10px 0;'/>");
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
            
            Map<String, Object> imageData = objectMapper.readValue(image, new TypeReference<Map<String, Object>>() {});
            @SuppressWarnings("unchecked")
            List<String> images = (List<String>) imageData.get("images");
            
            return images != null ? images : List.of();
            
        } catch (JsonProcessingException e) {
            log.error("이미지 JSON 파싱 오류: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 문제 난이도 열거형
     */
    public enum Difficulty {
        하, 중, 상
    }
}
