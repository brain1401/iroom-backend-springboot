package com.iroomclass.springbackend.domain.question.entity;

import com.iroomclass.springbackend.domain.unit.entity.Unit;

import jakarta.persistence.*;
import lombok.*;

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
     * React Editor에서 작성된 HTML 형태의 문제 내용
     * 이미지 포함 가능
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String stem;

    /**
     * 문제 정답
     * 문제의 정답을 저장
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String answerKey;

    /**
     * 문제 난이도 열거형
     */
    public enum Difficulty {
        하, 중, 상
    }

}
