package com.iroomclass.springbackend.common;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 공통 API 응답 문서화 어노테이션 모음
 * 
 * <p>중복되는 @ApiResponse 어노테이션들을 재사용 가능한 조합으로 제공합니다.
 * 코드 중복을 줄이고 일관된 API 문서화를 보장합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
public final class ApiResponseDocumentation {

    private ApiResponseDocumentation() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * 표준 성공 응답 (200 OK)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "성공", 
            content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))
        )
    })
    public @interface StandardSuccess {
    }

    /**
     * 표준 생성 성공 응답 (201 Created)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "생성 성공", 
            content = @Content(schema = @Schema(implementation = ApiResponse.SuccessResponse.class))
        )
    })
    public @interface StandardCreated {
    }

    /**
     * 표준 삭제 성공 응답 (204 No Content)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204", 
            description = "삭제 성공"
        )
    })
    public @interface StandardDeleted {
    }

    /**
     * 표준 잘못된 요청 응답 (400 Bad Request)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청", 
            content = @Content(
                schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                examples = @ExampleObject(
                    name = "입력 검증 실패", 
                    value = ApiResponseConstants.BAD_REQUEST_EXAMPLE
                )
            )
        )
    })
    public @interface StandardBadRequest {
    }

    /**
     * 표준 리소스 없음 응답 (404 Not Found) - 일반적인 경우
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "리소스 없음", 
            content = @Content(
                schema = @Schema(implementation = ApiResponse.ErrorResponse.class)
            )
        )
    })
    public @interface StandardNotFound {
    }

    /**
     * 표준 시험 없음 응답 (404 Not Found)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "시험 없음", 
            content = @Content(
                schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                examples = @ExampleObject(
                    name = "시험 없음", 
                    value = ApiResponseConstants.EXAM_NOT_FOUND_EXAMPLE
                )
            )
        )
    })
    public @interface ExamNotFound {
    }

    /**
     * 표준 시험 결과 없음 응답 (404 Not Found)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "시험 결과 없음", 
            content = @Content(
                schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                examples = @ExampleObject(
                    name = "시험 결과 없음", 
                    value = ApiResponseConstants.RESULT_NOT_FOUND_EXAMPLE
                )
            )
        )
    })
    public @interface ExamResultNotFound {
    }

    /**
     * 표준 시험 제출물 없음 응답 (404 Not Found)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "시험 제출물 없음", 
            content = @Content(
                schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                examples = @ExampleObject(
                    name = "시험 제출 없음", 
                    value = ApiResponseConstants.SUBMISSION_NOT_FOUND_EXAMPLE
                )
            )
        )
    })
    public @interface ExamSubmissionNotFound {
    }

    /**
     * 표준 서버 내부 오류 응답 (500 Internal Server Error)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류", 
            content = @Content(
                schema = @Schema(implementation = ApiResponse.ErrorResponse.class), 
                examples = @ExampleObject(
                    name = "서버 오류", 
                    value = ApiResponseConstants.SERVER_ERROR_EXAMPLE
                )
            )
        )
    })
    public @interface StandardServerError {
    }

    /**
     * CRUD 조회 API용 표준 응답 조합
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @StandardSuccess
    @StandardNotFound
    @StandardBadRequest
    public @interface StandardRead {
    }

    /**
     * CRUD 생성 API용 표준 응답 조합
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @StandardCreated
    @StandardBadRequest
    @StandardServerError
    public @interface StandardCreate {
    }

    /**
     * CRUD 수정 API용 표준 응답 조합
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @StandardSuccess
    @StandardBadRequest
    @StandardNotFound
    public @interface StandardUpdate {
    }

    /**
     * CRUD 삭제 API용 표준 응답 조합
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @StandardSuccess
    @StandardNotFound
    public @interface StandardDelete {
    }

    /**
     * 시험 관련 조회 API용 표준 응답 조합
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @StandardSuccess
    @ExamNotFound
    @StandardBadRequest
    public @interface ExamRead {
    }

    /**
     * 시험 결과 관련 조회 API용 표준 응답 조합
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @StandardSuccess
    @ExamResultNotFound
    @StandardBadRequest
    public @interface ExamResultRead {
    }

    /**
     * 채점 관련 API용 표준 응답 조합
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @StandardCreated
    @StandardBadRequest
    @ExamSubmissionNotFound
    public @interface GradingOperation {
    }
}