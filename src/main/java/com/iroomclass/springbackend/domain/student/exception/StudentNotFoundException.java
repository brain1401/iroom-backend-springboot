package com.iroomclass.springbackend.domain.student.exception;

import com.iroomclass.springbackend.common.exception.EntityNotFoundException;

/**
 * 학생을 찾을 수 없을 때 발생하는 예외
 * 
 * <p>학생 인증 정보(이름, 생년월일, 전화번호)로 학생을 찾을 수 없을 때 발생합니다.
 * EntityNotFoundException을 상속받아 HTTP 404 상태 코드로 자동 처리됩니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
public class StudentNotFoundException extends EntityNotFoundException {
    
    /**
     * 메시지와 함께 예외 생성
     * 
     * @param message 예외 메시지
     */
    public StudentNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인 예외와 함께 예외 생성
     * 
     * @param message 예외 메시지
     * @param cause   원인 예외
     */
    public StudentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 표준화된 학생 인증 실패 메시지로 예외 생성
     * 
     * @param name 학생 이름
     * @param phone 전화번호 (마스킹 처리됨)
     */
    public StudentNotFoundException(String name, String phone) {
        super(String.format("학생 정보를 찾을 수 없습니다 (이름: %s, 전화번호: %s)", name, maskPhone(phone)));
    }
    
    /**
     * 전화번호 마스킹 처리
     * 
     * @param phone 원본 전화번호
     * @return 마스킹된 전화번호 (010-****-1234)
     */
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return "****-****";
        }
        return phone.substring(0, 3) + "-****-" + phone.substring(phone.length() - 4);
    }
}