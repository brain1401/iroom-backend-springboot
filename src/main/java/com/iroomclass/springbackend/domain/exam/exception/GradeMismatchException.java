package com.iroomclass.springbackend.domain.exam.exception;

import com.iroomclass.springbackend.common.exception.InvalidRequestException;

import java.util.List;
import java.util.UUID;

/**
 * 학년 불일치 예외
 * 
 * <p>요청한 학년과 문제의 학년이 일치하지 않을 때 발생하는 예외입니다.
 * HTTP 400 Bad Request 상태로 처리됩니다.</p>
 */
public class GradeMismatchException extends InvalidRequestException {

    /**
     * 기본 생성자
     * 
     * @param message 예외 메시지
     */
    public GradeMismatchException(String message) {
        super(message);
    }

    /**
     * 원인 예외와 함께 생성하는 생성자
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public GradeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 학년 불일치 문제들에 대한 표준 메시지 생성
     * 
     * @param requestedGrade 요청된 학년
     * @param invalidQuestionIds 학년이 일치하지 않는 문제 ID들
     * @return 학년 불일치 예외
     */
    public static GradeMismatchException forQuestions(Integer requestedGrade, List<UUID> invalidQuestionIds) {
        if (invalidQuestionIds.size() == 1) {
            return new GradeMismatchException(
                String.format("문제 %s는 %d학년용이 아닙니다", 
                    invalidQuestionIds.get(0), requestedGrade)
            );
        }
        
        String idsString = invalidQuestionIds.stream()
            .map(UUID::toString)
            .collect(java.util.stream.Collectors.joining(", "));
        
        return new GradeMismatchException(
            String.format("다음 문제들은 %d학년용이 아닙니다: [%s]", requestedGrade, idsString)
        );
    }

    /**
     * 단일 문제 학년 불일치
     * 
     * @param requestedGrade 요청된 학년
     * @param questionId 문제 ID
     * @param actualGrade 실제 문제의 학년
     * @return 학년 불일치 예외
     */
    public static GradeMismatchException forQuestion(Integer requestedGrade, UUID questionId, Integer actualGrade) {
        return new GradeMismatchException(
            String.format("문제 %s는 %d학년용이지만 %d학년 시험지에 포함될 수 없습니다", 
                questionId, actualGrade, requestedGrade)
        );
    }
}