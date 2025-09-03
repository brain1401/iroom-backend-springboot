package com.iroomclass.springbackend.domain.exam.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.iroomclass.springbackend.common.UUIDv7Generator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학생 답안지 Entity
 * 
 * 학생이 제출한 시험 답안을 관리합니다. (순수 답안 정보만)
 * 주관식과 객관식 문제의 답안을 모두 지원하며, AI 이미지 인식 결과를 포함합니다.
 * 채점 결과는 별도의 QuestionGrading 엔티티에서 관리합니다.
 * 
 * <p>
 * 답안 유형별 특징:
 * </p>
 * <ul>
 * <li>주관식: answerText 사용 (AI가 이미지에서 인식한 텍스트)</li>
 * <li>객관식: selectedChoice 사용 (선택한 번호 1~5)</li>
 * </ul>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "student_answer_sheet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StudentAnswerSheet {

    /**
     * 답안 고유 ID
     * UUIDv7 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    /**
     * 시험 제출과의 관계
     * ManyToOne: 여러 답안이 하나의 시험 제출에 속함
     * FetchType.LAZY: 필요할 때만 시험 제출 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private ExamSubmission examSubmission;

    /**
     * 학생 이름
     * 시험을 응시한 학생의 이름
     * 최대 100자, 필수 입력
     */
    @Column(nullable = false, length = 100)
    private String studentName;

    /**
     * 문제별 답안 목록
     * OneToMany: 하나의 답안지에 여러 문제별 답안이 속함
     */
    @OneToMany(mappedBy = "studentAnswerSheet", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudentAnswerSheetProblem> problems = new ArrayList<>();

    /**
     * Entity 저장 전 실행되는 메서드
     * UUID를 자동으로 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUIDv7Generator.generate();
        }
    }

    /**
     * 학생 이름 업데이트
     * 
     * @param studentName 새로운 학생 이름
     */
    public void updateStudentName(String studentName) {
        this.studentName = studentName;
    }

    /**
     * 문제별 답안 추가
     * 
     * @param problem 추가할 문제별 답안
     */
    public void addProblem(StudentAnswerSheetProblem problem) {
        problems.add(problem);
    }

    /**
     * 문제별 답안 제거
     * 
     * @param problem 제거할 문제별 답안
     */
    public void removeProblem(StudentAnswerSheetProblem problem) {
        problems.remove(problem);
    }

    /**
     * 총 문제 수 반환
     * 
     * @return 총 문제 수
     */
    public int getTotalProblemCount() {
        return problems.size();
    }

    /**
     * 답안이 제출된 문제 수 반환
     * 
     * @return 답안이 제출된 문제 수
     */
    public int getAnsweredProblemCount() {
        return (int) problems.stream().filter(StudentAnswerSheetProblem::hasAnswer).count();
    }

    /**
     * 특정 문제의 답안 반환
     * 
     * @param questionId 문제 ID
     * @return 해당 문제의 답안, 없으면 null
     */
    public StudentAnswerSheetProblem getProblemByQuestionId(UUID questionId) {
        return problems.stream()
                .filter(problem -> questionId.equals(problem.getQuestionId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 답안 완료 여부 확인
     * 
     * @return 모든 문제에 답안이 있으면 true
     */
    public boolean isCompleted() {
        return !problems.isEmpty() && problems.stream().allMatch(StudentAnswerSheetProblem::hasAnswer);
    }


}