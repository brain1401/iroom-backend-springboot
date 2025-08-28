package com.iroomclass.springbackend.common.exception;

/**
 * 잘못된 요청 예외
 * 
 * <p>클라이언트의 잘못된 요청으로 인해 처리할 수 없는 상황에서 발생하는 예외입니다.
 * HTTP 400 Bad Request 상태로 처리됩니다.</p>
 */
public class InvalidRequestException extends RuntimeException {

    /**
     * 기본 생성자
     * 
     * @param message 예외 메시지
     */
    public InvalidRequestException(String message) {
        super(message);
    }

    /**
     * 원인 예외와 함께 생성하는 생성자
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}