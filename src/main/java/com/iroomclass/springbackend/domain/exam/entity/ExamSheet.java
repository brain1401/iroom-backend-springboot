package com.iroomclass.springbackend.domain.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import com.iroomclass.springbackend.common.UUIDv7Generator;
import com.iroomclass.springbackend.common.ApplicationContextProvider;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetQuestionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 시험지 Entity
 * 
 * 시험지 생성을 위한 기본 정보를 관리합니다.
 * 총점은 100점으로 고정되며, 문제 개수는 exam_sheet_question 테이블에서 조회합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_sheet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamSheet {

    /**
     * 시험지 고유 ID
     * UUIDv7 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    /**
     * 시험지 이름
     * 예시: "1학년 중간고사", "2학년 기말고사"
     * 최대 100자, 필수 입력
     */
    @Column(nullable = false, length = 100)
    private String examName;

    /**
     * 학년
     * 해당 시험지가 몇 학년용인지 (1, 2, 3학년)
     * 필수 입력
     */
    @Column(nullable = false)
    private Integer grade;

    /**
     * 생성일시
     * 시험지가 생성된 날짜와 시간
     * 자동으로 현재 시간이 설정됩니다.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     * 시험지가 마지막으로 수정된 날짜와 시간
     * 자동으로 현재 시간이 설정됩니다.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 시험지 문제 목록
     */
    @OneToMany(mappedBy = "examSheet", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamSheetQuestion> questions;

    /**
     * Entity 저장 전 실행되는 메서드
     * UUID, 생성일시와 수정일시를 자동으로 설정합니다.
     * 이미 설정된 값이 있으면 유지합니다.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUIDv7Generator.generate();
        }
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    /**
     * Entity 업데이트 전 실행되는 메서드
     * 수정일시를 자동으로 설정합니다.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 총 문제 수 계산
     * Repository를 통해 COUNT 쿼리로 조회합니다.
     * 
     * @return 총 문제 수
     */
    public Integer getTotalQuestions() {
        try {
            ExamSheetQuestionRepository repository = ApplicationContextProvider.getBean(ExamSheetQuestionRepository.class);
            return Math.toIntExact(repository.countByExamSheetId(this.id));
        } catch (Exception e) {
            // Repository 조회 실패 시 기본 메모리 기반 계산으로 fallback
            if (questions == null) return 0;
            return questions.size();
        }
    }

    /**
     * 객관식 문제 수 계산
     * Repository를 통해 COUNT 쿼리로 조회합니다.
     * 
     * @return 객관식 문제 수
     */
    public Integer getMultipleChoiceCount() {
        try {
            ExamSheetQuestionRepository repository = ApplicationContextProvider.getBean(ExamSheetQuestionRepository.class);
            return Math.toIntExact(repository.countMultipleChoiceByExamSheetId(this.id));
        } catch (Exception e) {
            // Repository 조회 실패 시 기본 메모리 기반 계산으로 fallback
            if (questions == null) return 0;
            return Math.toIntExact(questions.stream()
                    .filter(q -> q.getQuestion() != null && q.getQuestion().getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE)
                    .count());
        }
    }

    /**
     * 주관식 문제 수 계산
     * Repository를 통해 COUNT 쿼리로 조회합니다.
     * 
     * @return 주관식 문제 수
     */
    public Integer getSubjectiveCount() {
        try {
            ExamSheetQuestionRepository repository = ApplicationContextProvider.getBean(ExamSheetQuestionRepository.class);
            return Math.toIntExact(repository.countSubjectiveByExamSheetId(this.id));
        } catch (Exception e) {
            // Repository 조회 실패 시 기본 메모리 기반 계산으로 fallback
            if (questions == null) return 0;
            return Math.toIntExact(questions.stream()
                    .filter(q -> q.getQuestion() != null && q.getQuestion().getQuestionType() == Question.QuestionType.SUBJECTIVE)
                    .count());
        }
    }

    /**
     * 총 배점 계산
     * Repository를 통해 SUM 쿼리로 조회합니다.
     * 
     * @return 총 배점
     */
    public Integer getTotalPoints() {
        try {
            ExamSheetQuestionRepository repository = ApplicationContextProvider.getBean(ExamSheetQuestionRepository.class);
            return repository.sumPointsByExamSheetId(this.id);
        } catch (Exception e) {
            // Repository 조회 실패 시 기본 메모리 기반 계산으로 fallback
            if (questions == null) return 0;
            return questions.stream()
                    .mapToInt(esq -> esq.getPoints())
                    .sum();
        }
    }

    /**
     * 문제 개수 업데이트 (임시 메서드)
     * 
     * @param totalQuestions 총 문제 수
     * @param multipleChoiceCount 객관식 문제 수
     * @param subjectiveCount 주관식 문제 수
     */
    public void updateQuestionCounts(Integer totalQuestions, long multipleChoiceCount, long subjectiveCount) {
        // 현재는 실제 업데이트 로직이 없음 - questions 리스트가 실제 데이터를 반영
        // 필요시 추후 구현
    }

}