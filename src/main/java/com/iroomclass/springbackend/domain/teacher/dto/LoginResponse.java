package com.iroomclass.springbackend.domain.teacher.dto;

import com.iroomclass.springbackend.domain.auth.entity.Teacher;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 선생님 로그인 응답 DTO
 */
@Schema(description = "선생님 로그인 응답")
public record LoginResponse(
    @Schema(description = "선생님 ID", example = "1")
    Long id,
    
    @Schema(description = "사용자명", example = "teacher01")
    String username,
    
    @Schema(description = "계정 생성 시간", example = "2024-08-17T10:30:00")
    LocalDateTime createdAt
) {
    /**
     * Teacher 엔티티로부터 LoginResponse 생성
     *
     * @param teacher 선생님 엔티티
     * @return LoginResponse DTO
     */
    public static LoginResponse from(Teacher teacher) {
        return new LoginResponse(
            teacher.getId(),
            teacher.getUsername(),
            teacher.getCreatedAt()
        );
    }
}