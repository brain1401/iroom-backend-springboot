package com.iroomclass.springbackend.common;

/**
 * API 응답 관련 상수 클래스
 * 
 * <p>프로젝트 전반에서 사용되는 API 응답 메시지와 Swagger 예시를 중앙집중식으로 관리합니다.
 * 코드 중복을 제거하고 일관된 응답 형식을 제공합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
public final class ApiResponseConstants {

    private ApiResponseConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // === 공통 응답 메시지 ===
    public static final String SUCCESS_MESSAGE = "작업이 성공적으로 완료되었습니다";
    public static final String VALIDATION_FAILED = "입력 데이터 검증에 실패했습니다";
    public static final String SERVER_ERROR = "서버 내부 오류가 발생했습니다";
    public static final String NOT_FOUND = "%s을(를) 찾을 수 없습니다";
    public static final String ALREADY_EXISTS = "이미 존재하는 %s입니다";
    public static final String OPERATION_NOT_ALLOWED = "허용되지 않은 작업입니다";

    // === 도메인별 응답 메시지 ===
    
    // 시험 관련
    public static final String EXAM_CREATE_SUCCESS = "시험 등록 성공";
    public static final String EXAM_LIST_SUCCESS = "전체 시험 목록 조회 성공";
    public static final String EXAM_LIST_BY_GRADE_SUCCESS = "학년별 시험 목록 조회 성공";
    public static final String EXAM_DETAIL_SUCCESS = "시험 상세 조회 성공";
    public static final String EXAM_UPDATE_SUCCESS = "시험 수정 성공";
    public static final String EXAM_DELETE_SUCCESS = "시험 삭제 성공";
    public static final String EXAM_NOT_FOUND = "시험을 찾을 수 없습니다";
    public static final String EXAM_SHEET_NOT_FOUND = "시험지 초안을 찾을 수 없습니다";
    
    // 시험 제출 관련
    public static final String SUBMISSION_CREATE_SUCCESS = "시험 제출 생성 성공";
    public static final String SUBMISSION_FINAL_SUCCESS = "시험 최종 제출 성공";
    public static final String SUBMISSION_NOT_FOUND = "시험 제출을 찾을 수 없습니다";
    public static final String SUBMISSION_ALREADY_EXISTS = "이미 제출된 시험입니다";
    public static final String SUBMISSION_NOT_COMPLETED = "답안이 완료되지 않았습니다";
    
    // 시험 결과 관련
    public static final String GRADING_START_SUCCESS = "AI 자동 채점이 시작되었습니다";
    public static final String REGRADING_START_SUCCESS = "AI 재채점이 시작되었습니다";
    public static final String GRADING_COMPLETE_SUCCESS = "채점이 완료되었습니다";
    public static final String RESULT_GET_SUCCESS = "시험 결과 조회 성공";
    public static final String LATEST_RESULT_SUCCESS = "최신 채점 결과 조회 성공";
    public static final String RESULT_DELETE_SUCCESS = "시험 결과가 삭제되었습니다";
    public static final String RESULT_NOT_FOUND = "시험 결과를 찾을 수 없습니다";
    public static final String GRADING_NOT_COMPLETED = "채점이 아직 완료되지 않았습니다";
    public static final String ORIGINAL_RESULT_NOT_FOUND = "기존 채점 결과를 찾을 수 없습니다";
    public static final String REFERENCE_INTEGRITY_VIOLATION = "참조 무결성 제약 조건에 위배됩니다";

    // === HTTP 상태 코드별 응답 예시 ===
    
    // 400 Bad Request - 입력 검증 실패
    public static final String BAD_REQUEST_EXAMPLE = """
            {
              "result": "ERROR",
              "message": "입력 데이터 검증에 실패했습니다",
              "data": null
            }
            """;
    
    // 404 Not Found - 리소스 없음
    public static final String NOT_FOUND_EXAMPLE_TEMPLATE = """
            {
              "result": "ERROR",
              "message": "%s",
              "data": null
            }
            """;
    
    // 409 Conflict - 중복 또는 충돌
    public static final String CONFLICT_EXAMPLE_TEMPLATE = """
            {
              "result": "ERROR",
              "message": "%s",
              "data": null
            }
            """;
    
    // 500 Internal Server Error - 서버 오류
    public static final String SERVER_ERROR_EXAMPLE = """
            {
              "result": "ERROR",
              "message": "서버 내부 오류가 발생했습니다",
              "data": null
            }
            """;

    // === 도메인별 성공 응답 예시 ===
    
    // 시험 등록 성공 예시
    public static final String EXAM_CREATE_SUCCESS_EXAMPLE = """
            {
              "result": "SUCCESS",
              "message": "시험 등록 성공",
              "data": {
                "examId": "123e4567-e89b-12d3-a456-426614174000",
                "examName": "수학 중간고사",
                "grade": 1,
                "studentCount": 30
              }
            }
            """;
    
    // 시험 목록 조회 성공 예시
    public static final String EXAM_LIST_SUCCESS_EXAMPLE = """
            {
              "result": "SUCCESS",
              "message": "전체 시험 목록 조회 성공",
              "data": {
                "exams": [
                  {
                    "examId": "123e4567-e89b-12d3-a456-426614174000",
                    "examName": "수학 중간고사",
                    "grade": 1,
                    "createdAt": "2024-08-30T10:00:00"
                  }
                ],
                "totalCount": 10
              }
            }
            """;
    
    // 시험 상세 조회 성공 예시
    public static final String EXAM_DETAIL_SUCCESS_EXAMPLE = """
            {
              "result": "SUCCESS",
              "message": "시험 상세 조회 성공",
              "data": {
                "examId": "123e4567-e89b-12d3-a456-426614174000",
                "examName": "수학 중간고사",
                "grade": 1,
                "examDate": "2024-09-15",
                "duration": 60,
                "totalQuestions": 20
              }
            }
            """;

    // === 특정 에러 응답 예시 (annotation에서 사용 가능한 상수) ===
    
    // 404 Not Found 예시들
    public static final String EXAM_NOT_FOUND_EXAMPLE = """
            {
              "result": "ERROR",
              "message": "시험을 찾을 수 없습니다",
              "data": null
            }
            """;
    
    public static final String EXAM_SHEET_NOT_FOUND_EXAMPLE = """
            {
              "result": "ERROR",
              "message": "시험지 초안을 찾을 수 없습니다",
              "data": null
            }
            """;
    
    public static final String SUBMISSION_NOT_FOUND_EXAMPLE = """
            {
              "result": "ERROR",
              "message": "시험 제출을 찾을 수 없습니다",
              "data": null
            }
            """;
    
    public static final String RESULT_NOT_FOUND_EXAMPLE = """
            {
              "result": "ERROR",
              "message": "시험 결과를 찾을 수 없습니다",
              "data": null
            }
            """;
    
    public static final String ORIGINAL_RESULT_NOT_FOUND_EXAMPLE = """
            {
              "result": "ERROR",
              "message": "기존 채점 결과를 찾을 수 없습니다",
              "data": null
            }
            """;
    
    // 409 Conflict 예시들
    public static final String SUBMISSION_ALREADY_EXISTS_EXAMPLE = """
            {
              "result": "ERROR",
              "message": "이미 제출된 시험입니다",
              "data": null
            }
            """;
    
    public static final String SUBMISSION_NOT_COMPLETED_EXAMPLE = """
            {
              "result": "ERROR",
              "message": "답안이 완료되지 않았습니다",
              "data": null
            }
            """;
    
    public static final String GRADING_NOT_COMPLETED_EXAMPLE = """
            {
              "result": "ERROR",
              "message": "채점이 아직 완료되지 않았습니다",
              "data": null
            }
            """;

    // === 헬퍼 메서드 ===
    
    /**
     * 엔티티별 Not Found 응답 예시 생성
     */
    public static String getNotFoundExample(String entityName) {
        return String.format(NOT_FOUND_EXAMPLE_TEMPLATE, entityName + "을(를) 찾을 수 없습니다");
    }
    
    /**
     * 엔티티별 Conflict 응답 예시 생성
     */
    public static String getConflictExample(String message) {
        return String.format(CONFLICT_EXAMPLE_TEMPLATE, message);
    }
    
    /**
     * Not Found 메시지 생성
     */
    public static String getNotFoundMessage(String entityName) {
        return String.format(NOT_FOUND, entityName);
    }
    
    /**
     * Already Exists 메시지 생성
     */
    public static String getAlreadyExistsMessage(String entityName) {
        return String.format(ALREADY_EXISTS, entityName);
    }
}