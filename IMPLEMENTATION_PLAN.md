# 🎯 iRoom 백엔드 신기능 구현 계획서

## 📊 분석 요약

현재 프로젝트를 새로운 기능 요구사항과 비교분석한 결과:
- **현재 구현률**: 약 40% ✅
- **신규 구현 필요**: 7개 주요 영역, 27개 변경사항
- **예상 개발 기간**: 3-4주 (16개발일)
- **우선순위**: 학생 기본 정보 → 관리 기능 → 고도화

---

## 🚀 Phase 1: 기반 시스템 강화 (1주차)

### 1.1 User 엔티티 확장 [3일]

#### 🎯 목표
- 학생 정보에 학년, 생년월일 필드 추가
- 안전한 DB 마이그레이션 수행
- 하위 호환성 보장

#### 📋 구현 단계

**1단계: DB 스키마 확장**
```sql
-- src/main/resources/db/migration/V2_1__add_user_fields.sql
ALTER TABLE user 
ADD COLUMN grade INTEGER NULL,
ADD COLUMN birth_date DATE NULL;

-- 성능 최적화 인덱스
CREATE INDEX idx_user_grade ON user(grade);
CREATE INDEX idx_user_birth_date ON user(birth_date);
```

**2단계: 엔티티 클래스 수정**
```java
// src/main/java/com/iroomclass/springbackend/domain/user/info/entity/User.java
@Entity
@Table(name = "user")
public class User {
    // ... 기존 필드들
    
    /**
     * 학년 (1, 2, 3학년)
     */
    @Column(nullable = false)
    private Integer grade;
    
    /**
     * 생년월일
     */
    @Column(nullable = false, name = "birth_date")
    private LocalDate birthDate;
    
    // 생성자, getter 메서드 추가
}
```

**3단계: DTO 확장**
```java
// UserLoginRequest.java
public record UserLoginRequest(
    @NotBlank String name,
    @NotBlank String phone,
    @NotNull @Schema(description = "생년월일", example = "2008-03-15")
    LocalDate birthDate
) {}

// UserLoginResponse.java  
public record UserLoginResponse(
    Long id,
    String name, 
    String phone,
    Integer grade,
    LocalDate birthDate,
    String message
) {}
```

**4단계: Repository 확장**
```java
// UserRepository.java
public interface UserRepository extends JpaRepository<User, Long> {
    // 기존 메서드 유지 (하위 호환성)
    Optional<User> findByNameAndPhone(String name, String phone);
    
    // 새 3-factor 인증 메서드
    Optional<User> findByNameAndPhoneAndBirthDate(String name, String phone, LocalDate birthDate);
}
```

#### ✅ 검증 기준
- [ ] 마이그레이션 성공률 100%
- [ ] 기존 API 호환성 유지
- [ ] 새 필드 NULL 값 0개

---

### 1.2 로그인 로직 개선 [1일]

#### 🎯 목표
- 3-factor 인증 구현 (name + phone + birthDate)
- 점진적 전환으로 서비스 중단 방지

#### 📋 구현 방법

**점진적 인증 전환**
```java
// UserService.java
@Service
public class UserService {
    
    public UserLoginResponse login(UserLoginRequest request) {
        log.info("3-factor 로그인 시도: 이름={}, 전화번호={}, 생년월일={}", 
                request.name(), request.phone(), request.birthDate());
        
        // 새로운 3-factor 인증
        User user = userRepository.findByNameAndPhoneAndBirthDate(
            request.name(), request.phone(), request.birthDate()
        ).orElseThrow(() -> new IllegalArgumentException(
            "이름, 전화번호, 생년월일이 일치하지 않습니다."
        ));
        
        return new UserLoginResponse(
            user.getId(),
            user.getName(),
            user.getPhone(), 
            user.getGrade(),
            user.getBirthDate(),
            "로그인에 성공했습니다."
        );
    }
}
```

---

### 1.3 마이페이지 API 구현 [1일]

#### 🎯 목표
- 학생 기본정보 조회 페이지 API 개발

#### 📋 구현 방법

**Controller 추가**
```java
// StudentController.java에 추가
@GetMapping("/profile")
@Operation(summary = "학생 마이페이지 조회", description = "학생의 기본 정보를 조회합니다.")
public ApiResponse<StudentProfileResponse> getProfile(
    @Parameter(description = "학생 이름") @RequestParam String name,
    @Parameter(description = "학생 전화번호") @RequestParam String phone,
    @Parameter(description = "학생 생년월일") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate
) {
    StudentProfileResponse profile = studentService.getProfile(name, phone, birthDate);
    return ApiResponse.success("프로필 조회 성공", profile);
}
```

**DTO 신규 생성**
```java
// StudentProfileResponse.java
public record StudentProfileResponse(
    @Schema(description = "학생 이름", example = "김철수")
    String name,
    
    @Schema(description = "전화번호", example = "010-1234-5678") 
    String phone,
    
    @Schema(description = "생년월일", example = "2008-03-15")
    LocalDate birthDate,
    
    @Schema(description = "학년", example = "1")
    Integer grade
) {}
```

---

## 🔧 Phase 2: 관리 기능 강화 (2주차)

### 2.1 대시보드 전체/학년별 선택 [2일]

#### 🎯 목표
- 전체 학년 통합 통계 기능
- 기존 학년별 + 새로운 전체 선택 옵션

#### 📋 구현 방법

**전체 통계 API**
```java
// DashboardController.java에 추가
@GetMapping("/overall-statistics")
@Operation(summary = "전체 학년 통합 통계", description = "모든 학년의 통합 통계를 조회합니다.")
public ApiResponse<OverallStatisticsResponse> getOverallStatistics() {
    OverallStatisticsResponse statistics = dashboardService.getOverallStatistics();
    return ApiResponse.success("전체 통계 조회 성공", statistics);
}
```

**전체 학년 통합 DTO**
```java
// OverallStatisticsResponse.java
public record OverallStatisticsResponse(
    @Schema(description = "전체 학생 수")
    int totalStudentCount,
    
    @Schema(description = "전체 평균 성적")
    double overallAverageScore,
    
    @Schema(description = "상/중/하위권 분포")
    OverallRankDistribution rankDistribution,
    
    @Schema(description = "학년별 세부 통계")
    List<GradeStatistics> gradeStatistics
) {}
```

---

### 2.2 시험지 목록 필터링 [1일]

#### 🎯 목표
- 생성일시, 단원정보, 시험지명으로 필터링

#### 📋 구현 방법

**Repository 쿼리 메서드**
```java
// ExamSheetRepository.java에 추가
@Query("""
    SELECT es FROM ExamSheet es 
    LEFT JOIN es.selectedUnits su 
    LEFT JOIN su.unit u
    WHERE (:examName IS NULL OR es.examName LIKE %:examName%)
    AND (:unitName IS NULL OR u.unitName LIKE %:unitName%)
    AND (:createdAtFrom IS NULL OR es.createdAt >= :createdAtFrom)
    AND (:createdAtTo IS NULL OR es.createdAt <= :createdAtTo)
    """)
Page<ExamSheet> findWithFilters(
    @Param("examName") String examName,
    @Param("unitName") String unitName, 
    @Param("createdAtFrom") LocalDateTime createdAtFrom,
    @Param("createdAtTo") LocalDateTime createdAtTo,
    Pageable pageable
);
```

---

## 🚀 Phase 3: 고도화 기능 (3-4주차)

### 3.1 문제 직접 선택 시스템 [3일]

#### 🎯 목표
- 랜덤 선택 → 직접 선택 방식 전환
- 문제 미리보기 기능

#### 📋 신규 API 4개

**1. 단원 트리 구조 API**
```java
@GetMapping("/admin/units/tree")
public ApiResponse<List<UnitTreeResponse>> getUnitTree() {
    List<UnitTreeResponse> tree = unitService.getUnitTree();
    return ApiResponse.success("단원 트리 조회 성공", tree);
}
```

**2. 단원별 문제 목록 API**
```java
@GetMapping("/admin/units/{unitId}/questions")
public ApiResponse<Page<QuestionSummaryResponse>> getQuestionsByUnit(
    @PathVariable Long unitId,
    Pageable pageable
) {
    Page<QuestionSummaryResponse> questions = questionService.findByUnitId(unitId, pageable);
    return ApiResponse.success("단원별 문제 목록 조회 성공", questions);
}
```

**3. 문제 미리보기 API**
```java
@GetMapping("/admin/questions/{questionId}/preview") 
public ApiResponse<QuestionPreviewResponse> getQuestionPreview(@PathVariable Long questionId) {
    QuestionPreviewResponse preview = questionService.getPreview(questionId);
    return ApiResponse.success("문제 미리보기 조회 성공", preview);
}
```

**4. 문제 교체 API**
```java
@PostMapping("/admin/exam-sheets/{examSheetId}/questions/replace")
public ApiResponse<Void> replaceQuestion(
    @PathVariable Long examSheetId,
    @RequestBody QuestionReplaceRequest request
) {
    examSheetService.replaceQuestion(examSheetId, request.oldQuestionId(), request.newQuestionId());
    return ApiResponse.success("문제 교체 완료");
}
```

---

## 📊 개발 일정 및 마일스톤

### 주차별 계획

| 주차 | 목표 | 주요 산출물 | 완료 기준 |
|------|------|-------------|-----------|
| **1주차** | 기반 시스템 | User 확장, 마이페이지 | 로그인 3-factor 작동 |
| **2주차** | 관리 기능 | 대시보드, 필터링 | 전체 통계 정확성 |
| **3주차** | 문제 관리 | 직접 선택, 미리보기 | UI 워크플로우 완성 |
| **4주차** | 안정화 | 테스트, 최적화 | 성능 목표 달성 |

### 성공 지표

#### 기능적 지표
- [ ] 모든 신규 API 정상 작동
- [ ] 기존 기능 100% 호환성 유지
- [ ] 데이터 무손실 마이그레이션

#### 비기능적 지표
- [ ] API 응답시간 < 500ms (95%ile)
- [ ] 전체 통계 조회 < 2초
- [ ] DB 마이그레이션 시간 < 30분

---

## ⚠️ 위험 요소 및 완화 방안

### 주요 위험 요소

1. **DB 마이그레이션 실패**
   - 완화: 점진적 스키마 변경 + 전체 백업
   - 모니터링: 실시간 데이터 무결성 검증

2. **성능 저하**
   - 완화: 인덱스 최적화 + 쿼리 성능 테스트  
   - 모니터링: APM 도구로 실시간 성능 추적

3. **사용자 로그인 장애**
   - 완화: Dual Authentication 방식으로 점진적 전환
   - 모니터링: 로그인 성공률 실시간 추적

### 롤백 계획

**긴급 롤백 시나리오**
```sql
-- DB 롤백
ALTER TABLE user DROP COLUMN grade, DROP COLUMN birth_date;

-- 애플리케이션 롤백  
git revert [commit-hash]
```

---

## 🧪 테스트 전략

### 단위 테스트
- Service 로직 테스트 커버리지 > 80%
- Repository 쿼리 메서드 검증
- DTO 변환 로직 검증

### 통합 테스트
- API 엔드포인트 전체 테스트
- DB 마이그레이션 시나리오 테스트
- 인증 플로우 엔드투엔드 테스트

### 성능 테스트
- 대시보드 전체 통계 조회 성능
- 대용량 사용자 로그인 동시성 테스트
- DB 쿼리 실행 계획 분석

---

## 🎯 결론

본 구현 계획은 **안전성과 점진적 배포**를 최우선으로 하여:

1. **기존 시스템 안정성** 보장
2. **사용자 경험 연속성** 유지  
3. **성능 최적화** 달성
4. **확장 가능한 아키텍처** 구축

을 목표로 합니다.

**예상 결과**: 3-4주 내에 모든 요구사항을 안전하게 구현하여, 사용자와 관리자 모두에게 향상된 경험을 제공할 수 있을 것입니다.