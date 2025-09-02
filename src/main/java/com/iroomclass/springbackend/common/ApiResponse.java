package com.iroomclass.springbackend.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 표준 API 응답 래퍼
 */
@Schema(
    name = "ApiResponse",
    description = "표준 API 응답 래퍼",
    example = """
    {
      "result": "SUCCESS",
      "message": "요청 처리 완료",
      "data": null
    }
    """
)
public record ApiResponse<T>(
        @Schema(
            description = "응답 결과",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "SUCCESS",
            allowableValues = {"SUCCESS", "ERROR"}
        )
        ResultStatus result,
        
        @Schema(
            description = "응답 메시지",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "요청이 성공적으로 처리되었습니다"
        )
        String message,
        
        @Schema(
            description = "응답 데이터 (성공 시에만 포함)",
            nullable = true
        )
        T data) {
    public ApiResponse {
        // result, message null 불가 검증 수행
        if (result == null)
            throw new IllegalArgumentException("result는 필수입니다");
        if (message == null)
            throw new IllegalArgumentException("message는 필수입니다");
    }

    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResultStatus.SUCCESS, message, data);
    }

    /**
     * 성공 응답 생성 (메시지 기본값)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultStatus.SUCCESS, "", data);
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(ResultStatus.SUCCESS, message, null);
    }

    /**
     * 오류 응답 생성
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(ResultStatus.ERROR, message, null);
    }

    /**
     * 제네릭 오류 응답 생성 (타입 안전성 보장)
     */
    public static <T> ApiResponse<T> errorWithType(String message) {
        return new ApiResponse<>(ResultStatus.ERROR, message, null);
    }

    /**
     * 오류 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(ResultStatus.ERROR, message, data);
    }
    
    /**
     * 성공 응답용 스키마 클래스 (Swagger 문서화용)
     */
    @Schema(
        name = "SuccessResponse",
        description = "성공 응답 스키마",
        example = """
        {
          "result": "SUCCESS",
          "message": "요청이 성공적으로 처리되었습니다",
          "data": { ... }
        }
        """
    )
    public static class SuccessResponse<T> {
        @Schema(description = "응답 결과", example = "SUCCESS")
        public String result = "SUCCESS";
        
        @Schema(description = "성공 메시지", example = "요청이 성공적으로 처리되었습니다")
        public String message;
        
        @Schema(description = "응답 데이터")
        public T data;
    }
    
    /**
     * 에러 응답용 스키마 클래스 (Swagger 문서화용)
     */
    @Schema(
        name = "ErrorResponse", 
        description = "에러 응답 스키마",
        example = """
        {
          "result": "ERROR",
          "message": "요청 처리 중 오류가 발생했습니다",
          "data": null
        }
        """
    )
    public static class ErrorResponse {
        @Schema(description = "응답 결과", example = "ERROR")
        public String result = "ERROR";
        
        @Schema(description = "오류 메시지", example = "요청 처리 중 오류가 발생했습니다")
        public String message;
        
        @Schema(description = "데이터 (항상 null)", nullable = true)
        public Object data = null;
    }
}