package com.iroomclass.springbackend.common;

/**
 * 검증 관련 상수 클래스
 * 
 * <p>프로젝트 전반에서 사용되는 검증 메시지와 정규식 패턴을 중앙집중식으로 관리합니다.
 * 코드 중복을 제거하고 일관된 메시지를 제공합니다.</p>
 */
public final class ValidationConstants {

    private ValidationConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // === 공통 필수 필드 메시지 ===
    public static final String REQUIRED_FIELD = "%s는 필수입니다";
    public static final String NOT_BLANK = "%s는 필수입니다";
    public static final String NOT_NULL = "%s는 필수입니다";
    public static final String NOT_EMPTY = "%s는 최소 1개 이상이어야 합니다";

    // === 학생 관련 검증 메시지 ===
    public static final String REQUIRED_STUDENT_NAME = "학생 이름은 필수입니다";
    public static final String REQUIRED_STUDENT_PHONE = "학생 전화번호는 필수입니다";
    public static final String REQUIRED_BIRTH_DATE = "생년월일은 필수입니다";

    // === 시험 관련 검증 메시지 ===
    public static final String REQUIRED_EXAM_NAME = "시험지 이름은 필수입니다";
    public static final String REQUIRED_GRADE = "학년은 필수입니다";
    public static final String REQUIRED_TOTAL_QUESTIONS = "총 문제 개수는 필수입니다";
    public static final String REQUIRED_MULTIPLE_CHOICE_COUNT = "객관식 문제 개수는 필수입니다";
    public static final String REQUIRED_SUBJECTIVE_COUNT = "주관식 문제 개수는 필수입니다";
    public static final String REQUIRED_SELECTED_UNITS = "선택된 단원은 최소 1개 이상이어야 합니다";
    
    // 시험 실체 관련
    public static final String REQUIRED_EXAM_SHEET_ID = "시험지 ID는 필수입니다";
    public static final String REQUIRED_STUDENT_COUNT = "학생 수는 필수입니다";
    public static final String INVALID_STUDENT_COUNT = "학생 수는 1명 이상이어야 합니다";
    public static final String REQUIRED_EXAM_ID = "시험 ID는 필수입니다";
    
    // 채점 관련
    public static final String REQUIRED_SUBMISSION_ID = "시험 제출 ID는 필수입니다";
    public static final String REQUIRED_RESULT_ID = "시험 결과 ID는 필수입니다";
    public static final String REQUIRED_GRADER_ID_FOR_MANUAL = "수동 채점시 채점자 ID는 필수입니다";
    public static final String GRADER_ID_NULL_FOR_AUTO = "자동 채점시 채점자 ID는 null이어야 합니다";

    // === 인증 관련 검증 메시지 ===
    public static final String REQUIRED_USERNAME = "사용자명은 필수입니다";
    public static final String REQUIRED_PASSWORD = "비밀번호는 필수입니다";
    public static final String REQUIRED_USER_TYPE = "사용자 타입은 필수입니다";

    // === 범위 검증 메시지 ===
    public static final String INVALID_GRADE_RANGE = "학년은 1 이상 3 이하여야 합니다";
    public static final String INVALID_TOTAL_QUESTIONS_RANGE = "총 문제 개수는 1 이상 30 이하여야 합니다";
    public static final String INVALID_MULTIPLE_CHOICE_RANGE = "객관식 문제 개수는 0 이상 30 이하여야 합니다";
    public static final String INVALID_SUBJECTIVE_RANGE = "주관식 문제 개수는 0 이상 30 이하여야 합니다";

    // === 정규식 패턴 ===
    public static final String PHONE_NUMBER_PATTERN = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$";
    public static final String USER_TYPE_PATTERN = "^(STUDENT|TEACHER)$";

    // === 패턴 관련 검증 메시지 ===
    public static final String INVALID_PHONE_FORMAT = "올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)";
    public static final String INVALID_USER_TYPE = "지원되지 않는 사용자 타입입니다";

    // === 기타 메시지 ===
    public static final String MESSAGE_REQUIRED = "메시지는 필수입니다";
    public static final String ECHO_MESSAGE_REQUIRED = "에코할 메시지는 필수입니다";

    // === 범위 제한값 ===
    public static final int MIN_GRADE = 1;
    public static final int MAX_GRADE = 3;
    public static final int MIN_TOTAL_QUESTIONS = 1;
    public static final int MAX_TOTAL_QUESTIONS = 30;
    public static final int MIN_QUESTION_COUNT = 0;
    public static final int MAX_QUESTION_COUNT = 30;

    // === Objects.requireNonNull 헬퍼 메서드 ===
    public static final String getNullCheckMessage(String fieldName) {
        return fieldName + "는 필수입니다";
    }
}