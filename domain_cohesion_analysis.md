# 도메인 응집도 분석 보고서

## 현재 도메인 구조 문제점

### 1. 과도한 도메인 분리
- **admin 도메인**: exam, question, unit, print, statistics, dashboard, submission, info (8개)
- **user 도메인**: exam (answer, result), info, student (4개)
- 총 138개 파일이 12개 세분화된 도메인에 분산

### 2. 높은 도메인 간 결합도 
#### StatisticsService 의존성:
- admin.exam → ExamRepository
- user.exam → ExamSubmissionRepository  
- user.exam.answer → StudentAnswerSheetRepository
- admin.question → Question
- admin.unit → Unit

#### DashboardService 의존성:
- admin.exam → ExamRepository
- user.exam → ExamSubmissionRepository

#### ExamSubmissionService 의존성:
- admin.exam → ExamRepository  
- user.exam.answer → StudentAnswerSheetRepository
- user.info → UserRepository

### 3. 낮은 도메인 응집도
- 시험 관련 기능이 admin.exam, user.exam, admin.question에 분산
- 사용자 관련 기능이 admin.info, user.info에 분산
- 통계/대시보드가 별도 도메인으로 분리되어 있음

## 개선된 도메인 구조 설계

### 1. 새로운 도메인 구조
```
com.iroomclass.springbackend.domain/
├── exam/                           # 시험 관리 통합 도메인
│   ├── controller/
│   │   ├── ExamController.java           # 시험 관리 (기존 admin.exam)
│   │   ├── ExamSheetController.java      # 시험지 관리
│   │   ├── QuestionController.java       # 문제 관리 (기존 admin.question)
│   │   ├── ExamSubmissionController.java # 시험 제출 (기존 user.exam)
│   │   └── ExamResultController.java     # 채점 결과 (기존 user.exam.result)
│   ├── service/
│   │   ├── ExamService.java
│   │   ├── ExamSheetService.java
│   │   ├── QuestionService.java
│   │   ├── ExamSubmissionService.java
│   │   ├── ExamResultService.java
│   │   ├── QuestionResultService.java
│   │   └── StudentAnswerSheetService.java
│   ├── repository/
│   │   ├── ExamRepository.java
│   │   ├── ExamSheetRepository.java
│   │   ├── QuestionRepository.java
│   │   ├── ExamSubmissionRepository.java
│   │   ├── ExamResultRepository.java
│   │   ├── QuestionResultRepository.java
│   │   └── StudentAnswerSheetRepository.java
│   ├── entity/
│   │   ├── Exam.java
│   │   ├── ExamSheet.java
│   │   ├── ExamDocument.java
│   │   ├── ExamSheetQuestion.java
│   │   ├── Question.java
│   │   ├── ExamSubmission.java
│   │   ├── ExamResult.java
│   │   ├── QuestionResult.java
│   │   └── StudentAnswerSheet.java
│   └── dto/
│       ├── exam/
│       ├── question/
│       ├── submission/
│       └── result/
├── curriculum/                     # 교육과정 도메인
│   ├── controller/
│   │   └── UnitController.java           # 단원 관리 (기존 admin.unit)
│   ├── service/
│   │   └── UnitService.java
│   ├── repository/
│   │   ├── UnitRepository.java
│   │   ├── UnitCategoryRepository.java
│   │   └── UnitSubcategoryRepository.java
│   ├── entity/
│   │   ├── Unit.java
│   │   ├── UnitCategory.java
│   │   └── UnitSubcategory.java
│   └── dto/
├── user/                           # 사용자 관리 통합 도메인
│   ├── controller/
│   │   ├── UserController.java           # 학생 관리 (기존 user.info)
│   │   └── AdminController.java          # 관리자 관리 (기존 admin.info)
│   ├── service/
│   │   ├── UserService.java
│   │   └── AdminService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── AdminRepository.java
│   ├── entity/
│   │   ├── User.java
│   │   └── Admin.java
│   └── dto/
├── analysis/                       # 분석 및 통계 도메인
│   ├── controller/
│   │   ├── StatisticsController.java     # 통계 (기존 admin.statistics)
│   │   └── DashboardController.java      # 대시보드 (기존 admin.dashboard)
│   ├── service/
│   │   ├── StatisticsService.java
│   │   └── DashboardService.java
│   ├── repository/
│   └── dto/
├── print/                          # 출력 도메인 (단독 유지)
│   ├── controller/
│   ├── service/
│   ├── util/
│   └── dto/
└── system/                         # 시스템 유틸리티 (단독 유지)
    ├── controller/
    ├── service/
    └── dto/
```

### 2. 도메인별 책임 재정의

#### exam 도메인 (시험 관리 통합)
- **핵심 책임**: 시험 생성, 문제 관리, 제출 처리, 채점 관리
- **주요 엔티티**: Exam, ExamSheet, Question, ExamSubmission, ExamResult
- **비즈니스 기능**: 시험 생성/수정, 문제 출제, 학생 제출, AI 채점
- **내부 응집도**: 시험 라이프사이클 전체 관리

#### curriculum 도메인 (교육과정)  
- **핵심 책임**: 교육과정 단원 체계 관리
- **주요 엔티티**: Unit, UnitCategory, UnitSubcategory
- **비즈니스 기능**: 단원 분류, 교육과정 구조 관리
- **내부 응집도**: 교육과정 표준화

#### user 도메인 (사용자 관리)
- **핵심 책임**: 학생 및 관리자 정보 관리  
- **주요 엔티티**: User, Admin
- **비즈니스 기능**: 사용자 인증, 프로필 관리
- **내부 응집도**: 사용자 라이프사이클 관리

#### analysis 도메인 (분석 및 통계)
- **핵심 책임**: 성적 분석 및 통계 생성
- **주요 기능**: 학년별 통계, 대시보드, 성적 분석
- **비즈니스 기능**: 데이터 분석, 리포팅
- **내부 응집도**: 분석 및 보고서 생성

### 3. 의존성 해결 전략

#### Before (현재 문제)
```
StatisticsService → admin.exam + user.exam + user.exam.answer + admin.question + admin.unit
DashboardService → admin.exam + user.exam  
ExamSubmissionService → admin.exam + user.exam.answer + user.info
```

#### After (개선 후)
```
analysis.StatisticsService → exam.* (단일 도메인)
analysis.DashboardService → exam.* (단일 도메인)
exam.ExamSubmissionService → user.* (필요 시만)
```

### 4. 마이그레이션 장점
- **응집도 향상**: 관련 기능들이 하나의 도메인에 집중
- **결합도 감소**: 도메인 간 의존성 최소화  
- **유지보수성 향상**: 변경 영향 범위 축소
- **이해도 향상**: 비즈니스 도메인과 코드 구조 일치