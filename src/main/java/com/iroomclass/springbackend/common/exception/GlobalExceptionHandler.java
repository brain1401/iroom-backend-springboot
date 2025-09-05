package com.iroomclass.springbackend.common.exception;

import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.common.ResultStatus;
import com.iroomclass.springbackend.domain.teacher.exception.TeacherNotFoundException;
import com.iroomclass.springbackend.domain.exam.exception.QuestionNotFoundException;
import com.iroomclass.springbackend.domain.exam.exception.GradeMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 * 
 * <p>모든 컨트롤러에서 발생하는 예외를 ApiResponse로 통일하여 처리합니다.
 * 모든 예외를 캐치하여 일관된 에러 응답 형식을 제공합니다.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation 실패 예외 처리 (@RequestBody @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        Map<String, String> errorDetails = fieldErrors.stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing
                ));
        
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Validation 실패: {}", errorMessage);
        
        ApiResponse<Map<String, String>> errorResponse = ApiResponse.error(
            "입력 데이터 검증에 실패했습니다",
            errorDetails
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 바인딩 실패 예외 처리 (@ModelAttribute @Valid)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(
            BindException ex, HttpServletRequest request) {
        
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("바인딩 실패: {}", errorMessage);
        return createErrorResponse(
                "요청 파라미터 검증에 실패했습니다: " + errorMessage, 
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * 필수 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        log.warn("필수 파라미터 누락: {}", ex.getParameterName());
        return createErrorResponse(
                "필수 파라미터가 누락되었습니다: " + ex.getParameterName(),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * HTTP 메서드 미지원 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        log.warn("지원하지 않는 HTTP 메서드: {}", ex.getMethod());
        return createErrorResponse(
                "지원하지 않는 HTTP 메서드입니다: " + ex.getMethod(),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    /**
     * JSON 파싱 실패 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        log.warn("JSON 파싱 실패: {}", ex.getMessage());
        return createErrorResponse(
                "JSON 형식이 올바르지 않습니다",
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * 파라미터 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        log.warn("파라미터 타입 불일치: {} = {}", ex.getName(), ex.getValue());
        return createErrorResponse(
                String.format("파라미터 '%s'의 값 '%s'가 올바르지 않습니다", ex.getName(), ex.getValue()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * 제약 조건 위반 예외 처리 (@Validated)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("제약 조건 위반: {}", errorMessage);
        return createErrorResponse(
                "입력 데이터 검증에 실패했습니다: " + errorMessage,
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * 데이터 무결성 위반 예외 처리
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        log.warn("데이터 무결성 위반: {}", ex.getMessage());
        
        String message = "데이터 처리 중 오류가 발생했습니다";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry")) {
                message = "이미 존재하는 데이터입니다";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "참조 무결성 제약 조건에 위배됩니다";
            }
        }
        
        return createErrorResponse(message, HttpStatus.CONFLICT);
    }

    /**
     * NoResourceFoundException 처리 (404 에러)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        
        log.warn("존재하지 않는 리소스: {}", ex.getResourcePath());
        return createErrorResponse(
                "존재하지 않는 엔드포인트입니다: " + ex.getResourcePath(),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * 비즈니스 로직 예외 처리 (커스텀 예외들)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        log.warn("비즈니스 로직 예외: {}", ex.getMessage());
        return createErrorResponse(ex.getMessage(), ex.getHttpStatus());
    }

    /**
     * 엔티티 없음 예외 처리
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        
        log.warn("엔티티 없음: {}", ex.getMessage());
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * 잘못된 요청 예외 처리
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(
            InvalidRequestException ex, HttpServletRequest request) {
        
        log.warn("잘못된 요청: {}", ex.getMessage());
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * 선생님 인증 실패 예외 처리
     */
    @ExceptionHandler(TeacherNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTeacherNotFound(
            TeacherNotFoundException ex, HttpServletRequest request) {
        
        log.warn("선생님 인증 실패: {}", ex.getMessage());
        return createErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * 문제 찾기 실패 예외 처리
     */
    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleQuestionNotFound(
            QuestionNotFoundException ex, HttpServletRequest request) {
        
        log.warn("문제 찾기 실패: {}", ex.getMessage());
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * 학년 불일치 예외 처리
     */
    @ExceptionHandler(GradeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleGradeMismatch(
            GradeMismatchException ex, HttpServletRequest request) {
        
        log.warn("학년 불일치: {}", ex.getMessage());
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        log.warn("잘못된 인수: {}", ex.getMessage());
        return createErrorResponse(
                "잘못된 요청 파라미터입니다: " + ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * IllegalStateException 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        
        log.warn("잘못된 상태: {}", ex.getMessage());
        return createErrorResponse(
                "현재 상태에서 처리할 수 없는 요청입니다: " + ex.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    /**
     * 모든 예외의 최종 처리자
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("예상치 못한 오류가 발생했습니다", ex);
        
        // 개발 환경에서만 상세한 에러 정보 노출
        String message = "서버 내부 오류가 발생했습니다";
        if (isDevelopmentMode()) {
            message += ": " + ex.getMessage();
        }
        
        return createErrorResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 표준화된 에러 응답 생성
     */
    private ResponseEntity<ApiResponse<Void>> createErrorResponse(String message, HttpStatus status) {
        ApiResponse<Void> errorResponse = ApiResponse.error(message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * 개발 모드 확인
     */
    private boolean isDevelopmentMode() {
        // 프로파일이나 설정을 통해 개발 모드 확인
        String activeProfiles = System.getProperty("spring.profiles.active", "");
        return activeProfiles.contains("dev") || activeProfiles.contains("local");
    }
}