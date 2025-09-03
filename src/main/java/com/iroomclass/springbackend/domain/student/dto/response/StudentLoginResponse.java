package com.iroomclass.springbackend.domain.student.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 학생 로그인 응답 DTO
 * 
 * <p>학생 로그인(존재 여부 확인) 결과를 담는 DTO입니다.
 * JWT 토큰 없이 단순한 존재 여부만 확인합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학생 로그인 응답", example = """
    {
      "success": true,
      "message": "로그인 성공"
    }
    """)
public record StudentLoginResponse(
    
    @Schema(description = "로그인 성공 여부", example = "true")
    boolean isSuccessful,
    
    @Schema(description = "응답 메시지", example = "로그인 성공")
    String message
    
) {
    /**
     * 로그인 성공 응답 생성
     * 
     * @return 성공 응답
     */
    public static StudentLoginResponse success() {
        return new StudentLoginResponse(true, "로그인 성공");
    }
    
    /**
     * 로그인 실패 응답 생성
     * 
     * @return 실패 응답
     */
    public static StudentLoginResponse failure() {
        return new StudentLoginResponse(false, "학생 정보를 찾을 수 없습니다");
    }
    
    /**
     * 커스텀 메시지와 함께 성공 응답 생성
     * 
     * @param message 사용자 정의 메시지
     * @return 성공 응답
     */
    public static StudentLoginResponse success(String message) {
        return new StudentLoginResponse(true, message);
    }
    
    /**
     * 커스텀 메시지와 함께 실패 응답 생성
     * 
     * @param message 사용자 정의 메시지
     * @return 실패 응답
     */
    public static StudentLoginResponse failure(String message) {
        return new StudentLoginResponse(false, message);
    }
    
    /**
     * Student 엔터티로부터 성공 응답 생성
     * 
     * @param student 학생 엔터티
     * @return 성공 응답
     */
    public static StudentLoginResponse from(com.iroomclass.springbackend.domain.auth.entity.Student student) {
        return new StudentLoginResponse(true, String.format("학생 '%s' 로그인 성공", student.getName()));
    }
}