package com.iroomclass.springbackend.common.exception;

/**
 * 엔티티 없음 예외
 * 
 * <p>요청한 엔티티를 찾을 수 없을 때 발생하는 예외입니다.
 * HTTP 404 Not Found 상태로 처리됩니다.</p>
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * 기본 생성자
     * 
     * @param message 예외 메시지
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * 원인 예외와 함께 생성하는 생성자
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 엔티티 타입과 ID로 표준 메시지 생성
     * 
     * @param entityType 엔티티 타입명
     * @param id 엔티티 ID
     */
    public EntityNotFoundException(String entityType, Object id) {
        super(String.format("%s를 찾을 수 없습니다 (ID: %s)", entityType, id));
    }

    /**
     * 엔티티 클래스와 ID로 표준 메시지 생성
     * 
     * @param entityClass 엔티티 클래스
     * @param id 엔티티 ID
     */
    public EntityNotFoundException(Class<?> entityClass, Object id) {
        super(String.format("%s를 찾을 수 없습니다 (ID: %s)", entityClass.getSimpleName(), id));
    }
}