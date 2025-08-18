# 🐳 Docker 환경 구축 가이드

> **Spring Boot 3.5.4 프로젝트를 위한 완전한 Docker 컨테이너화 솔루션**

## 📋 목차

1. [개요](#-개요)
2. [사전 준비사항](#-사전-준비사항)
3. [빠른 시작](#-빠른-시작)
4. [상세 사용법](#-상세-사용법)
5. [Docker 이미지 종류](#-docker-이미지-종류)
6. [환경 설정](#-환경-설정)
7. [트러블슈팅](#-트러블슈팅)
8. [성능 최적화](#-성능-최적화)

---

## 🎯 개요

이 가이드는 Spring Boot 3.5.4 기반 iRoom 백엔드 애플리케이션을 Docker로 컨테이너화하는 방법을 제공합니다.

### 포함된 구성 요소

```
🏗️ 애플리케이션 스택
├── 🚀 Spring Boot 3.5.4 (Java 21)
├── 🗄️ MySQL 8.0 (데이터베이스)
├── 🔄 Redis 7 (캐시)
├── 🔧 Adminer (DB 관리도구)
└── 🌐 Nginx (프록시, 선택사항)
```

### 주요 특징

- **다중 단계 빌드**: 최적화된 이미지 크기
- **레이어 캐싱**: 빠른 빌드 시간
- **CDS 지원**: 향상된 시작 성능
- **보안 강화**: Non-root 사용자
- **헬스체크**: 자동 상태 모니터링
- **볼륨 마운트**: 데이터 영속성

---

## 🔧 사전 준비사항

### 필수 요구사항

```bash
# Docker 및 Docker Compose 설치 확인
docker --version
# Docker version 24.0.0 이상

docker-compose --version
# Docker Compose version 2.0.0 이상

# Java 및 Gradle 확인
java --version
# Java 21 이상

./gradlew --version
# Gradle 8.0 이상
```

### 시스템 요구사항

| 구성 요소 | 최소 사양 | 권장 사양 |
|-----------|-----------|-----------|
| **CPU** | 2 cores | 4 cores |
| **메모리** | 4GB | 8GB |
| **디스크** | 10GB | 20GB |
| **네트워크** | 브로드밴드 | 브로드밴드 |

---

## 🚀 빠른 시작

### 1. 프로젝트 클론 및 이동
```bash
git clone <repository-url>
cd iroom-backend-springboot
```

### 2. 애플리케이션 빌드 및 실행
```bash
# 편리한 빌드 스크립트 사용
./docker-build.sh build

# Docker Compose로 전체 스택 실행
./docker-build.sh run
```

### 3. 서비스 접근
```bash
# 애플리케이션 확인
curl http://localhost:3055/api/system/health

# 주요 URL
echo "🚀 애플리케이션: http://localhost:3055"
echo "📖 Swagger UI: http://localhost:3055/api/swagger-ui.html"
echo "🗄️ Adminer: http://localhost:8080"
```

---

## 📚 상세 사용법

### docker-build.sh 스크립트 활용

#### 기본 명령어
```bash
# 표준 이미지 빌드
./docker-build.sh build

# CDS 지원 이미지 빌드 (빠른 시작 시간)
./docker-build.sh build-cds

# Docker Compose로 실행
./docker-build.sh run

# 로그 확인
./docker-build.sh logs

# 서비스 중지
./docker-build.sh stop

# 정리 (이미지 및 빌드 아티팩트 삭제)
./docker-build.sh clean
```

#### 고급 사용법
```bash
# 레지스트리에 이미지 푸시
REGISTRY=your-registry.com ./docker-build.sh push

# 특정 환경에서 실행
docker-compose --env-file .env.production up -d

# 개별 서비스 재시작
docker-compose restart app
```

### 수동 Docker 명령어

#### 이미지 빌드
```bash
# 표준 이미지
docker build -t iroom/spring-backend:latest .

# CDS 지원 이미지
docker build -f Dockerfile.cds -t iroom/spring-backend:latest-cds .
```

#### 컨테이너 실행
```bash
# 단독 실행
docker run -d \
  --name iroom-backend \
  -p 3055:3055 \
  -e SPRING_PROFILES_ACTIVE=docker \
  iroom/spring-backend:latest

# 네트워크와 함께 실행
docker network create iroom-network
docker run -d \
  --name iroom-backend \
  --network iroom-network \
  -p 3055:3055 \
  iroom/spring-backend:latest
```

---

## 🔧 Docker 이미지 종류

### 1. 표준 이미지 (Dockerfile)

```dockerfile
# 특징
- 다중 단계 빌드
- 레이어 최적화
- 보안 강화 (non-root)
- 헬스체크 포함

# 사용 시나리오
- 일반적인 프로덕션 환경
- 개발 및 테스트 환경
- CI/CD 파이프라인
```

### 2. CDS 지원 이미지 (Dockerfile.cds)

```dockerfile
# 특징
- Class Data Sharing 지원
- 빠른 애플리케이션 시작 시간
- 메모리 사용량 최적화
- 향상된 성능

# 사용 시나리오
- 고성능이 요구되는 프로덕션
- 마이크로서비스 환경
- 자주 재시작하는 환경
```

### 성능 비교

| 메트릭 | 표준 이미지 | CDS 이미지 |
|--------|-------------|------------|
| **시작 시간** | ~15초 | ~8초 |
| **메모리 사용량** | 512MB | 480MB |
| **이미지 크기** | 280MB | 295MB |
| **빌드 시간** | 2분 | 3분 |

---

## ⚙️ 환경 설정

### 환경 변수 설정

#### .env 파일 생성
```bash
# .env 파일
SPRING_PROFILES_ACTIVE=docker

# Database
DB_HOST=mysql
DB_PORT=3306
DB_NAME=iroom_backend_db
DB_USERNAME=iroom_user
DB_PASSWORD=your_secure_password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# Application
JWT_SECRET=your_jwt_secret_key
```

#### Docker Compose 오버라이드
```yaml
# docker-compose.override.yml
version: '3.8'
services:
  app:
    environment:
      - SPRING_PROFILES_ACTIVE=local-docker
      - DEBUG=true
    volumes:
      - ./logs:/application/logs
    ports:
      - "3055:3055"
      - "5005:5005"  # 디버그 포트
```

### 프로파일별 설정

#### 개발 환경
```bash
# docker-compose.dev.yml 사용
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

#### 프로덕션 환경
```bash
# docker-compose.prod.yml 사용
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## 🔍 모니터링 및 디버깅

### 로그 확인
```bash
# 애플리케이션 로그
docker-compose logs -f app

# 전체 서비스 로그
docker-compose logs -f

# 특정 시간대 로그
docker-compose logs --since="2024-08-17T10:00:00" app
```

### 컨테이너 상태 확인
```bash
# 서비스 상태
docker-compose ps

# 리소스 사용량
docker stats

# 헬스체크 상태
docker inspect iroom-backend | grep -A 5 "Health"
```

### 디버깅
```bash
# 컨테이너 접속
docker exec -it iroom-backend bash

# 설정 확인
docker exec iroom-backend java -XX:+PrintFlagsFinal -version | grep -i gc

# 메모리 덤프
docker exec iroom-backend jcmd 1 GC.run_finalization
```

---

## 🛠️ 트러블슈팅

### 일반적인 문제 해결

#### 1. 포트 충돌
```bash
# 포트 사용 중인 프로세스 확인
netstat -ano | findstr :3055  # Windows
lsof -i :3055                # macOS/Linux

# 다른 포트 사용
docker-compose up -d --scale app=1 -p 3056:3055
```

#### 2. 메모리 부족
```bash
# Docker 메모리 할당 확인
docker system info

# 메모리 정리
docker system prune -a
docker volume prune
```

#### 3. 네트워크 문제
```bash
# 네트워크 상태 확인
docker network ls
docker network inspect iroom-backend-network

# 네트워크 재생성
docker network rm iroom-backend-network
docker network create iroom-backend-network
```

#### 4. 데이터베이스 연결 실패
```bash
# MySQL 컨테이너 로그 확인
docker-compose logs mysql

# 연결 테스트
docker exec iroom-mysql mysql -u iroom_user -p -e "SELECT 1"

# 권한 재설정
docker exec iroom-mysql mysql -u root -p -e "
  GRANT ALL PRIVILEGES ON iroom_backend_db.* TO 'iroom_user'@'%';
  FLUSH PRIVILEGES;"
```

---

## 🚀 성능 최적화

### 빌드 최적화

#### 1. Docker 빌드 캐시 활용
```bash
# BuildKit 활성화
export DOCKER_BUILDKIT=1

# 캐시 마운트 사용
docker build --cache-from iroom/spring-backend:latest .
```

#### 2. 멀티 플랫폼 이미지
```bash
# ARM64 및 AMD64 지원
docker buildx create --use
docker buildx build --platform linux/amd64,linux/arm64 \
  -t iroom/spring-backend:latest --push .
```

### 런타임 최적화

#### 1. JVM 튜닝
```yaml
# docker-compose.yml
services:
  app:
    environment:
      - JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

#### 2. 연결 풀 최적화
```yaml
# application-docker.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  redis:
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
```

---

## 📊 모범 사례

### 보안 모범 사례

1. **비밀번호 관리**
   - Docker Secrets 사용
   - 환경 변수 암호화
   - 정기적인 비밀번호 변경

2. **이미지 보안**
   - 최소 권한 원칙
   - 취약점 스캔
   - 베이스 이미지 업데이트

3. **네트워크 보안**
   - 내부 네트워크 사용
   - 필요한 포트만 노출
   - TLS 암호화

### 운영 모범 사례

1. **모니터링**
   - 헬스체크 구현
   - 로그 중앙집중화
   - 메트릭 수집

2. **백업**
   - 정기적인 데이터 백업
   - 볼륨 스냅샷
   - 설정 파일 버전 관리

3. **업데이트**
   - 롤링 업데이트
   - 블루-그린 배포
   - 롤백 전략

---

## 🔗 참고 자료

### 공식 문서
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

### 관련 도구
- [Portainer](https://www.portainer.io/) - Docker 관리 UI
- [Watchtower](https://containrrr.dev/watchtower/) - 자동 컨테이너 업데이트
- [Traefik](https://traefik.io/) - 리버스 프록시

---

**💡 문제가 발생하면 이 가이드를 참조하고, 해결되지 않는 경우 팀에 문의하세요!**