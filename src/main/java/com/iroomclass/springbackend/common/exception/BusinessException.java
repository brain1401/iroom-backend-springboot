package com.iroomclass.springbackend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 예외
 * 
 * <p>비즈니스 규칙 위반이나 업무적으로 처리할 수 없는 상황에서 발생하는 예외입니다.
 * HTTP 상태 코드를 함께 지정할 수 있어 다양한 비즈니스 상황에 대응할 수 있습니다.</p>
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;

    /**
     * 기본 생성자 (400 Bad Request)
     * 
     * @param message 예외 메시지
     */
    public BusinessException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    /**
     * HTTP 상태 코드를 지정하는 생성자
     * 
     * @param message 예외 메시지
     * @param httpStatus HTTP 상태 코드
     */
    public BusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    /**
     * 원인 예외와 함께 생성하는 생성자
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    /**
     * 원인 예외와 HTTP 상태 코드를 함께 지정하는 생성자
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     * @param httpStatus HTTP 상태 코드
     */
    public BusinessException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    /**
     * HTTP 상태 코드 반환
     * 
     * @return HTTP 상태 코드
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}