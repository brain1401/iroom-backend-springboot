package com.iroomclass.springbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import com.iroomclass.springbackend.common.ValidationConstants;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 통합 로그인 요청 DTO
 * 
 * <p>학생과 관리자 로그인을 모두 지원하는 통합 인증 요청 DTO입니다.
 * userType에 따라 다른 인증 방식을 사용합니다:</p>
 * 
 * <ul>
 *   <li><strong>STUDENT</strong>: 3-factor 인증 (이름 + 전화번호 + 생년월일)</li>
 *   <li><strong>TEACHER</strong>: 기본 인증 (사용자명 + 비밀번호)</li>
 * </ul>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(
    description = """
        통합 로그인 요청 DTO
        
        userType에 따라 필수 필드가 달라집니다:
        
        **STUDENT (학생 3-factor 인증)**
        - userType: "STUDENT" (필수)
        - name: 학생 이름 (필수)
        - phone: 전화번호 "010-1234-5678" 형식 (필수)
        - birthDate: 생년월일 "2008-03-15" 형식 (필수)
        - username, password: null 또는 생략
        
        **TEACHER (관리자 기본 인증)**  
        - userType: "TEACHER" (필수)
        - username: 관리자 사용자명 (필수)
        - password: 관리자 비밀번호 (필수)
        - name, phone, birthDate: null 또는 생략
        """,
    example = """
        {
          "userType": "STUDENT",
          "name": "김철수", 
          "phone": "010-1234-5678",
          "birthDate": "2008-03-15"
        }
        """
)
public record UnifiedLoginRequest(
    // 관리자 로그인용 필드
    @Schema(description = "관리자 사용자명 (TEACHER 타입 전용)", 
            example = "admin", 
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String username,
    
    @Schema(description = "관리자 비밀번호 (TEACHER 타입 전용)", 
            example = "admin123", 
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String password,
    
    // 학생 로그인용 필드
    @Schema(description = "학생/관리자 이름 (모든 타입)", 
            example = "김철수", 
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String name,
    
    @Pattern(regexp = ValidationConstants.PHONE_NUMBER_PATTERN, 
             message = ValidationConstants.INVALID_PHONE_FORMAT)
    @Schema(description = "학생 전화번호 (STUDENT 타입 전용)", 
            example = "010-1234-5678", 
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String phone,
    
    @Schema(description = "학생 생년월일 (STUDENT 타입 전용)", 
            example = "2008-03-15", 
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    LocalDate birthDate,
    
    // 사용자 타입 컨텍스트
    @NotBlank(message = "사용자 타입은 필수입니다")
    @Pattern(regexp = "^(STUDENT|TEACHER)$", 
             message = "지원되지 않는 사용자 타입입니다")
    @Schema(description = "사용자 타입", 
            example = "STUDENT", 
            allowableValues = {"STUDENT", "TEACHER"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    String userType
) {
    
    /**
     * Compact Constructor - 사용자 타입별 입력 검증
     */
    public UnifiedLoginRequest {
        Objects.requireNonNull(userType, "userType은 필수입니다");
        
        if (isTeacher()) {
            // 관리자 로그인 검증
            Objects.requireNonNull(username, "관리자 타입에서는 username이 필수입니다");
            Objects.requireNonNull(password, "관리자 타입에서는 password가 필수입니다");
            if (username.isBlank()) {
                throw new IllegalArgumentException("username은 빈 값일 수 없습니다");
            }
            if (password.isBlank()) {
                throw new IllegalArgumentException("password는 빈 값일 수 없습니다");
            }
        } else if (isStudent()) {
            // 학생 로그인 검증
            Objects.requireNonNull(name, "학생 타입에서는 name이 필수입니다");
            Objects.requireNonNull(phone, "학생 타입에서는 phone이 필수입니다");
            Objects.requireNonNull(birthDate, "학생 타입에서는 birthDate가 필수입니다");
            if (name.isBlank()) {
                throw new IllegalArgumentException("name은 빈 값일 수 없습니다");
            }
            if (phone.isBlank()) {
                throw new IllegalArgumentException("phone은 빈 값일 수 없습니다");
            }
        }
    }
    
    /**
     * 학생 타입 여부 확인
     * 
     * @return 학생 타입이면 true
     */
    public boolean isStudent() {
        return "STUDENT".equals(userType);
    }
    
    /**
     * 관리자 타입 여부 확인
     * 
     * @return 관리자 타입이면 true  
     */
    public boolean isTeacher() {
        return "TEACHER".equals(userType);
    }
    
    /**
     * 학생 로그인 요청으로 변환
     * 
     * @return StudentLoginRequest 객체
     */
    public StudentLoginRequest toStudentLoginRequest() {
        if (!isStudent()) {
            throw new IllegalStateException("학생 타입이 아닌 경우 변환할 수 없습니다");
        }
        return new StudentLoginRequest(name, phone, birthDate);
    }
    
    /**
     * 관리자 로그인 요청으로 변환
     * 
     * @return LoginRequest 객체
     */
    public LoginRequest toTeacherLoginRequest() {
        if (!isTeacher()) {
            throw new IllegalStateException("관리자 타입이 아닌 경우 변환할 수 없습니다");
        }
        return new LoginRequest(username, password);
    }
}