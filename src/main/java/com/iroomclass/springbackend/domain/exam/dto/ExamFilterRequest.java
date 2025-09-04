package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * 시험 목록 조회 필터링 요청 DTO
 * 
 * <p>시험 목록 조회 시 다양한 필터링 조건을 적용하기 위한 요청 객체입니다.</p>
 */
@Schema(
    name = "ExamFilterRequest",
    description = "시험 목록 필터링 요청",
    example = """
        {
          "grade": 1,
          "search": "중간고사",
          "recent": true
        }
        """
)
public record ExamFilterRequest(
    
    @Min(value = 1, message = "학년은 1 이상이어야 합니다")
    @Max(value = 3, message = "학년은 3 이하여야 합니다")
    @Schema(
        description = "학년 필터 (1, 2, 3)", 
        example = "1", 
        minimum = "1", 
        maximum = "3"
    )
    Integer grade,
    
    @Size(max = 100, message = "검색어는 100자 이하여야 합니다")
    @Schema(
        description = "시험명 검색어 (부분 일치)", 
        example = "중간고사", 
        maxLength = 100
    )
    String search,
    
    @Schema(
        description = "최근 생성된 시험만 조회 여부", 
        example = "true",
        defaultValue = "false"
    )
    Boolean recent,
    
    @Schema(
        description = "통계 조회 타입 (by-grade 등)", 
        example = "by-grade"
    )
    String statisticsType
    
) {
    
    /**
     * Compact Constructor - 입력값 검증
     */
    public ExamFilterRequest {
        // null 값 처리 및 trim
        search = search != null ? search.trim() : null;
        statisticsType = statisticsType != null ? statisticsType.trim() : null;
        
        // 빈 문자열을 null로 처리
        if (search != null && search.isEmpty()) {
            search = null;
        }
        if (statisticsType != null && statisticsType.isEmpty()) {
            statisticsType = null;
        }
    }
    
    /**
     * 학년 필터 조건이 있는지 확인
     * 
     * @return 학년 필터 존재 여부
     */
    public boolean hasGradeFilter() {
        return grade != null;
    }
    
    /**
     * 검색어 조건이 있는지 확인
     * 
     * @return 검색어 존재 여부
     */
    public boolean hasSearchFilter() {
        return search != null && !search.isEmpty();
    }
    
    /**
     * 최근 시험 조회 조건인지 확인
     * 
     * @return 최근 시험 조회 여부
     */
    public boolean isRecentFilter() {
        return Boolean.TRUE.equals(recent);
    }
    
    /**
     * 통계 조회 요청인지 확인
     * 
     * @return 통계 조회 요청 여부
     */
    public boolean isStatisticsRequest() {
        return statisticsType != null;
    }
    
    /**
     * 어떤 필터 조건도 없는 기본 조회인지 확인
     * 
     * @return 기본 조회 여부
     */
    public boolean isDefaultQuery() {
        return !hasGradeFilter() && 
               !hasSearchFilter() && 
               !isRecentFilter() && 
               !isStatisticsRequest();
    }
    
    /**
     * 필터링 조건 설명을 생성
     * (로깅용)
     * 
     * @return 필터링 조건 설명
     */
    public String getFilterDescription() {
        if (isDefaultQuery()) {
            return "전체 조회";
        }
        
        StringBuilder desc = new StringBuilder();
        
        if (hasGradeFilter()) {
            desc.append("학년:").append(grade);
        }
        
        if (hasSearchFilter()) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("검색어:").append(search);
        }
        
        if (isRecentFilter()) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("최근시험");
        }
        
        if (isStatisticsRequest()) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("통계:").append(statisticsType);
        }
        
        return desc.toString();
    }
    
    /**
     * 빈 필터 객체 생성 (전체 조회용)
     * 
     * @return 빈 ExamFilterRequest 객체
     */
    public static ExamFilterRequest createEmpty() {
        return new ExamFilterRequest(null, null, null, null);
    }
    
    /**
     * 학년별 필터 객체 생성
     * 
     * @param grade 학년
     * @return 학년별 ExamFilterRequest 객체
     */
    public static ExamFilterRequest createByGrade(Integer grade) {
        return new ExamFilterRequest(grade, null, null, null);
    }
    
    /**
     * 검색어 필터 객체 생성
     * 
     * @param search 검색어
     * @return 검색어 ExamFilterRequest 객체
     */
    public static ExamFilterRequest createBySearch(String search) {
        return new ExamFilterRequest(null, search, null, null);
    }
    
    /**
     * 최근 시험 필터 객체 생성
     * 
     * @return 최근 시험 ExamFilterRequest 객체
     */
    public static ExamFilterRequest createRecent() {
        return new ExamFilterRequest(null, null, true, null);
    }
    
    /**
     * 통계 조회 필터 객체 생성
     * 
     * @param statisticsType 통계 타입
     * @return 통계 ExamFilterRequest 객체
     */
    public static ExamFilterRequest createStatistics(String statisticsType) {
        return new ExamFilterRequest(null, null, null, statisticsType);
    }
}