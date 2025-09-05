package com.iroomclass.springbackend.domain.exam.exception;

import com.iroomclass.springbackend.common.exception.EntityNotFoundException;

import java.util.UUID;

/**
 * 문제를 찾을 수 없을 때 발생하는 예외
 * 
 * <p>요청한 문제 ID에 해당하는 문제가 존재하지 않을 때 발생합니다.
 * HTTP 404 Not Found 상태로 처리됩니다.</p>
 */
public class QuestionNotFoundException extends EntityNotFoundException {

    /**
     * 기본 생성자
     * 
     * @param message 예외 메시지
     */
    public QuestionNotFoundException(String message) {
        super(message);
    }

    /**
     * 원인 예외와 함께 생성하는 생성자
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public QuestionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 문제 ID로 표준 메시지 생성
     * 
     * @param questionId 문제 고유 식별자
     */
    public QuestionNotFoundException(UUID questionId) {
        super("문제를 찾을 수 없습니다", questionId);
    }

    /**
     * 여러 문제 ID에 대한 메시지 생성
     * 
     * @param questionIds 존재하지 않는 문제 ID 목록
     * @return 표준화된 예외 메시지
     */
    public static QuestionNotFoundException forMultipleIds(java.util.List<UUID> questionIds) {
        if (questionIds.size() == 1) {
            return new QuestionNotFoundException(questionIds.get(0));
        }
        
        String idsString = questionIds.stream()
            .map(UUID::toString)
            .collect(java.util.stream.Collectors.joining(", "));
        
        return new QuestionNotFoundException(
            String.format("다음 문제들을 찾을 수 없습니다: [%s]", idsString)
        );
    }

    /**
     * 단원에서 문제를 찾을 수 없을 때
     * 
     * @param unitId 단원 ID
     * @param grade 학년
     * @return 단원 관련 문제 없음 예외
     */
    public static QuestionNotFoundException forUnitAndGrade(UUID unitId, Integer grade) {
        return new QuestionNotFoundException(
            String.format("해당 단원(ID: %s)에서 %d학년 문제를 찾을 수 없습니다", unitId, grade)
        );
    }
}