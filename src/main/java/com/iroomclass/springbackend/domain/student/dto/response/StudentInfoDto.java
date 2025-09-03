package com.iroomclass.springbackend.domain.student.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 학생 정보 응답 DTO
 * 
 * <p>학생의 기본 정보를 담는 DTO입니다.
 * 이름, 전화번호, 생년월일, 학년 정보를 제공합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학생 기본 정보", example = """
    {
      "name": "홍길동",
      "phone": "010-1234-5678",
      "birthDate": "2000-01-01",
      "grade": 2
    }
    """)
public record StudentInfoDto(
    
    @Schema(description = "학생 이름", example = "홍길동")
    String name,
    
    @Schema(description = "전화번호", example = "010-1234-5678")
    String phone,
    
    @Schema(description = "생년월일", example = "2000-01-01")
    LocalDate birthDate,
    
    @Schema(description = "학년 (가장 최근 응시한 시험의 학년 기준)", example = "2")
    Integer grade
    
) {
    /**
     * 정적 팩토리 메서드
     * 
     * @param name      학생 이름
     * @param phone     전화번호
     * @param birthDate 생년월일
     * @param grade     학년
     * @return StudentInfoDto 인스턴스
     */
    public static StudentInfoDto of(String name, String phone, LocalDate birthDate, Integer grade) {
        return new StudentInfoDto(name, phone, birthDate, grade);
    }
    
    /**
     * 학년이 없는 경우의 정적 팩토리 메서드
     * 
     * @param name      학생 이름
     * @param phone     전화번호
     * @param birthDate 생년월일
     * @return StudentInfoDto 인스턴스 (학년은 null)
     */
    public static StudentInfoDto withoutGrade(String name, String phone, LocalDate birthDate) {
        return new StudentInfoDto(name, phone, birthDate, null);
    }
    
    /**
     * 학년 정보 존재 여부 확인
     * 
     * @return 학년 정보가 있으면 true
     */
    public boolean hasGrade() {
        return grade != null;
    }
}