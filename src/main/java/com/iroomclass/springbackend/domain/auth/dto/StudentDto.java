package com.iroomclass.springbackend.domain.auth.dto;

import com.iroomclass.springbackend.domain.auth.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학생 정보 응답 DTO
 * 
 * <p>학생의 전체 정보를 담는 DTO입니다.
 * 학생 ID를 포함하여 완전한 학생 정보를 제공합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(
    description = "학생 정보 응답 DTO",
    example = """
        {
          "id": 1,
          "name": "김철수",
          "phone": "010-1234-5678",
          "birthDate": "2008-03-15",
          "createdAt": "2025-01-15T10:30:00",
          "updatedAt": "2025-01-15T10:30:00"
        }
        """
)
public record StudentDto(
    
    @Schema(description = "학생 고유 식별자", example = "1")
    Long id,
    
    @Schema(description = "학생 이름", example = "김철수")
    String name,
    
    @Schema(description = "학생 전화번호", example = "010-1234-5678")
    String phone,
    
    @Schema(description = "학생 생년월일", example = "2008-03-15")
    LocalDate birthDate,
    
    @Schema(description = "생성 시간", example = "2025-01-15T10:30:00")
    LocalDateTime createdAt,
    
    @Schema(description = "수정 시간", example = "2025-01-15T10:30:00")
    LocalDateTime updatedAt
    
) {
    
    /**
     * Entity에서 DTO로 변환하는 정적 팩토리 메서드
     * 
     * @param student 학생 엔티티
     * @return StudentDto 인스턴스
     */
    public static StudentDto from(Student student) {
        return new StudentDto(
            student.getId(),
            student.getName(),
            student.getPhone(),
            student.getBirthDate(),
            student.getCreatedAt(),
            student.getUpdatedAt()
        );
    }
    
    /**
     * 기본 정보만으로 생성하는 정적 팩토리 메서드
     * 
     * @param name      학생 이름
     * @param phone     전화번호
     * @param birthDate 생년월일
     * @return StudentDto 인스턴스 (ID와 시간 정보는 null)
     */
    public static StudentDto of(String name, String phone, LocalDate birthDate) {
        return new StudentDto(null, name, phone, birthDate, null, null);
    }
    
    /**
     * 학생이 신규 등록된 학생인지 확인
     * 
     * @return ID가 없으면 신규 등록된 학생으로 간주
     */
    public boolean isNewStudent() {
        return id == null;
    }
}