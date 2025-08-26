package com.iroomclass.springbackend.domain.question.entity;

import com.iroomclass.springbackend.domain.unit.entity.Unit;

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

/**
 * 문제 정보 Entity
 * 
 * 각 단원별로 생성된 문제들을 관리합니다.
 * 주관식 문제만 지원하며, React Editor로 작성된 HTML 내용을 저장합니다.
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
public class Question {
    
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
     * 문제 내용
     * 주관식 문제 내용 (HTML 형태, 텍스트 + 이미지 포함 가능)
     * 예시: "x + 3 = 7일 때, x의 값은?" 또는 "다음 그래프를 보고 답하세요. <img src='graph.png'> f(x) = 2x + 1일 때, f(3)의 값은?"
     */
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String stem;

    /**
     * 문제 정답
     * 문제의 정답을 저장
     */
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String answerKey;

    /**
     * 문제 난이도 열거형
     */
    public enum Difficulty {
        하, 중, 상
    }

}
