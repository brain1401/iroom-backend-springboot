package com.iroomclass.springbackend.domain.teacher.exception;

/**
 * 선생님을 찾을 수 없을 때 발생하는 예외
 */
public class TeacherNotFoundException extends RuntimeException {
    
    /**
     * 메시지와 함께 예외 생성
     *
     * @param message 예외 메시지
     */
    public TeacherNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인과 함께 예외 생성
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public TeacherNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}