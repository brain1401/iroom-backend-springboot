# 📚 Spring Backend 프로젝트 문서

> **Spring Boot 3.5.4 + Java 21 기반 백엔드 프로젝트의 종합 문서 모음**

## 🗂️ 문서 구조

### 📋 핵심 가이드
| 문서 | 용도 | 대상 |
|------|------|------|
| [**팀 협업 가이드**](TEAM_COLLABORATION_GUIDE.md) | 전체 협업 프로세스 및 워크플로우 | 모든 팀원 |
| [**코딩 표준**](CODING_STANDARDS.md) | 코딩 컨벤션 및 스타일 가이드 | 개발자 |
| [**API 가이드라인**](API_GUIDELINES.md) | REST API 설계 및 구현 규칙 | 백엔드 개발자 |

### 🛠️ 설정 및 환경
| 문서 | 용도 | 대상 |
|------|------|------|
| [**환경 설정 가이드**](SETUP_GUIDE.md) | 개발 환경 설정 및 프로젝트 실행 | 신규 개발자 |
| [**설정 관리 가이드**](CONFIGURATION.md) | 애플리케이션 설정, DB/Redis 연결 | 개발자, DevOps |
| [**아키텍처 문서**](ARCHITECTURE.md) | 시스템 아키텍처 및 설계 원칙 | 시니어 개발자, 아키텍트 |
| [**문제 해결 가이드**](TROUBLESHOOTING.md) | 자주 발생하는 문제와 해결 방법 | 모든 개발자 |

## 🚀 빠른 시작

### 신규 개발자라면
1. **[환경 설정 가이드](SETUP_GUIDE.md)** - 개발 환경 구축
2. **[설정 관리 가이드](CONFIGURATION.md)** - 애플리케이션 설정
3. **[아키텍처 문서](ARCHITECTURE.md)** - 프로젝트 이해
4. **[코딩 표준](CODING_STANDARDS.md)** - 코딩 규칙 숙지
5. **[팀 협업 가이드](TEAM_COLLABORATION_GUIDE.md)** - 협업 프로세스 학습

### 기능 개발 시
1. **[API 가이드라인](API_GUIDELINES.md)** - API 설계 규칙
2. **[코딩 표준](CODING_STANDARDS.md)** - 구현 시 참조
3. **[팀 협업 가이드](TEAM_COLLABORATION_GUIDE.md)** - 개발 워크플로우

### 문제 발생 시
1. **[문제 해결 가이드](TROUBLESHOOTING.md)** - FAQ 및 해결 방법
2. **팀 Slack 채널** - 실시간 도움 요청

## 🎯 프로젝트 개요

### 기술 스택
- **Framework**: Spring Boot 3.5.4
- **Language**: Java 21
- **Build**: Gradle 8.14.3
- **Database**: MySQL + JPA/Hibernate
- **Cache**: Redis
- **Security**: Spring Security 6.x
- **Documentation**: SpringDoc OpenAPI

### 주요 특징
- **Domain-Driven Design**: 도메인 중심 패키지 구조
- **Modern Java**: Java 21 Record, Pattern Matching 활용
- **API-First**: OpenAPI 기반 API 문서화
- **Security by Design**: 보안을 고려한 설계

## 📊 문서 활용 통계

| 역할 | 주요 참조 문서 | 활용 빈도 |
|------|----------------|-----------|
| **신규 개발자** | 환경설정 → 아키텍처 → 코딩표준 | 온보딩 시 |
| **백엔드 개발자** | API 가이드 → 코딩표준 → 협업가이드 | 일일 업무 |
| **시니어 개발자** | 아키텍처 → 협업가이드 → 문제해결 | 리뷰 및 멘토링 |
| **팀 리드** | 협업가이드 → 아키텍처 → 전체 문서 | 프로세스 관리 |

## 🔄 문서 업데이트 정책

### 업데이트 주기
- **정기 업데이트**: 월 1회 문서 전체 검토
- **버전 변경 시**: 기술 스택 업그레이드 시 즉시 반영
- **프로세스 변경**: 협업 방식 변경 시 24시간 내 반영

### 기여 방법
1. **문서 오류 발견**: Issue 생성 또는 Slack 알림
2. **개선 제안**: Pull Request 생성
3. **새로운 가이드**: 팀 논의 후 추가

## 📞 문의 및 지원

| 구분 | 연락처 | 응답 시간 |
|------|--------|-----------|
| **긴급 문제** | 팀 Slack #dev-support | 30분 내 |
| **일반 문의** | 팀 리드 DM | 4시간 내 |
| **문서 개선** | GitHub Issue | 1일 내 |

## 📈 추가 자료

### 외부 참고 문서
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Java 21 새로운 기능](https://openjdk.org/projects/jdk/21/)
- [REST API 설계 가이드](https://restfulapi.net/)
- [Clean Code 가이드](https://clean-code-developer.com/)

### 내부 리소스
- **API 문서**: http://localhost:3055/api/swagger-ui.html
- **팀 위키**: 내부 Confluence 페이지
- **코드 리뷰 체크리스트**: 별도 문서 관리

---

**📝 이 문서는 실제 프로젝트 구조와 팀의 개발 프로세스를 분석하여 작성되었습니다.**  
**문서 개선 제안이나 오류 발견 시 언제든 팀에 알려주세요. 🙏**

---

*마지막 업데이트: 2024년 8월*  
*다음 정기 업데이트: 2024년 9월*