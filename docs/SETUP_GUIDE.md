# ⚙️ 개발 환경 설정 가이드

> **Spring Boot 3.5.4 + Java 21 프로젝트를 위한 완전한 개발 환경 구축 가이드**

## 📋 목차

1. [사전 요구사항](#-사전-요구사항)
2. [필수 소프트웨어 설치](#-필수-소프트웨어-설치)
3. [IDE 설정](#-ide-설정)
4. [데이터베이스 설정](#-데이터베이스-설정)
5. [프로젝트 실행](#-프로젝트-실행)
6. [개발 도구 설정](#-개발-도구-설정)
7. [환경별 설정](#-환경별-설정)
8. [검증 및 테스트](#-검증-및-테스트)

---

## 🔧 사전 요구사항

### 시스템 요구사항

| 구분 | 최소 사양 | 권장 사양 |
|------|-----------|-----------|
| **운영체제** | Windows 10/macOS 10.15/Ubuntu 18.04 | Windows 11/macOS 12+/Ubuntu 20.04+ |
| **메모리** | 8GB RAM | 16GB RAM 이상 |
| **저장공간** | 10GB 여유공간 | 20GB 여유공간 이상 |
| **네트워크** | 인터넷 연결 필수 | 안정적인 고속 인터넷 |

### 기술 스택 버전

| 기술 | 버전 | 필수 여부 |
|------|------|-----------|
| **Java** | 21 (LTS) | ✅ 필수 |
| **Spring Boot** | 3.5.4 | ✅ 필수 |
| **Gradle** | 8.14.3+ | ✅ 필수 |
| **MySQL** | 8.0+ | ✅ 필수 |
| **Redis** | 6.0+ | ✅ 필수 |

---

## 💻 필수 소프트웨어 설치

### 1. Java 21 설치

#### Windows
```bash
# Chocolatey 사용
choco install openjdk21

# 또는 직접 다운로드
# https://adoptium.net/temurin/releases/
```

#### macOS
```bash
# Homebrew 사용
brew install openjdk@21

# PATH 설정
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### Ubuntu/Linux
```bash
# APT 사용
sudo apt update
sudo apt install openjdk-21-jdk

# 또는 SDKMAN 사용
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.1-tem
```

#### 설치 확인
```bash
java -version
# 출력 예시: openjdk version "21.0.1" 2023-10-17
```

### 2. Git 설치

#### Windows
```bash
# Chocolatey 사용
choco install git

# 또는 공식 설치 프로그램
# https://git-scm.com/download/win
```

#### macOS
```bash
# Homebrew 사용
brew install git

# 또는 Xcode Command Line Tools
xcode-select --install
```

#### Ubuntu/Linux
```bash
sudo apt update
sudo apt install git
```

#### Git 초기 설정
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
git config --global init.defaultBranch main
```

---

## 🛠️ IDE 설정

### IntelliJ IDEA (권장)

#### 1. 설치
- **Community Edition** (무료) 또는 **Ultimate Edition** 다운로드
- 공식 사이트: https://www.jetbrains.com/idea/

#### 2. 필수 플러그인 설치
```
Settings → Plugins에서 다음 플러그인 설치:
✅ Lombok
✅ Spring Boot
✅ Database Tools and SQL (Ultimate만)
✅ Gradle
✅ Git
```

#### 3. 프로젝트 설정
```
File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
✅ Enable annotation processing 체크
```

#### 4. 코드 스타일 설정
```
Settings → Editor → Code Style → Java
- Indent: 4 spaces
- Continuation indent: 8 spaces
- Tab size: 4
- Use tab character: unchecked
```

### Eclipse (대안)

#### 1. 설치
- **Eclipse IDE for Enterprise Java and Web Developers** 다운로드
- 공식 사이트: https://www.eclipse.org/downloads/

#### 2. 필수 플러그인
```
Help → Eclipse Marketplace에서 설치:
✅ Spring Tools 4 (aka Spring Tool Suite 4)
✅ Lombok
✅ Gradle IDE Pack
```

### Visual Studio Code (경량 대안)

#### 1. 필수 확장
```
Extensions에서 설치:
✅ Extension Pack for Java
✅ Spring Boot Extension Pack
✅ Gradle for Java
✅ Lombok Annotations Support
```

---

## 🗄️ 데이터베이스 설정

### 1. MySQL 8.0+ 설치

#### Windows
```bash
# Chocolatey 사용
choco install mysql

# 또는 MySQL Installer 사용
# https://dev.mysql.com/downloads/installer/
```

#### macOS
```bash
# Homebrew 사용
brew install mysql

# MySQL 시작
brew services start mysql
```

#### Ubuntu/Linux
```bash
sudo apt update
sudo apt install mysql-server

# MySQL 보안 설정
sudo mysql_secure_installation
```

#### 2. 데이터베이스 생성
```sql
-- MySQL 접속
mysql -u root -p

-- 데이터베이스 생성
CREATE DATABASE spring_backend_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER 'spring_user'@'localhost' IDENTIFIED BY 'spring_password';
GRANT ALL PRIVILEGES ON spring_backend_db.* TO 'spring_user'@'localhost';
FLUSH PRIVILEGES;

-- 확인
SHOW DATABASES;
```

### 3. Redis 설치

#### Windows
```bash
# Chocolatey 사용
choco install redis-64

# 또는 WSL2 사용
wsl --install
# WSL2에서 Ubuntu 설치 후 Linux 방법 사용
```

#### macOS
```bash
# Homebrew 사용
brew install redis

# Redis 시작
brew services start redis
```

#### Ubuntu/Linux
```bash
sudo apt update
sudo apt install redis-server

# Redis 시작
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

#### Redis 연결 테스트
```bash
redis-cli ping
# 출력: PONG
```

---

## 🚀 프로젝트 실행

### 1. 프로젝트 클론

```bash
# 프로젝트 클론
git clone [repository-url]
cd spring-backend

# 브랜치 확인
git branch -a
```

### 2. 설정 파일 확인

#### application.yml 설정
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_backend_db
    username: spring_user
    password: spring_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
  
  jpa:
    hibernate:
      ddl-auto: update  # 개발 환경에서만 사용
    show-sql: true
```

### 3. 프로젝트 빌드 및 실행

#### Windows
```cmd
:: 권한 설정 (Git Bash에서)
chmod +x gradlew

:: 빌드
gradlew.bat clean build

:: 실행
gradlew.bat bootRun
```

#### macOS/Linux
```bash
# 권한 설정
chmod +x gradlew

# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

### 4. 실행 확인

#### 애플리케이션 접근
```
✅ 애플리케이션: http://localhost:3055/api
✅ Swagger UI: http://localhost:3055/api/swagger-ui.html
✅ API Docs: http://localhost:3055/api/v3/api-docs
✅ 헬스 체크: http://localhost:3055/api/system/health
```

#### 로그 확인
```bash
# 애플리케이션 로그
tail -f logs/spring-backend.log

# 에러 로그
tail -f logs/spring-backend-error.log
```

---

## 🔧 개발 도구 설정

### 1. API 테스트 도구

#### Postman 설치
```
1. https://www.postman.com/downloads/ 에서 다운로드
2. Collection 생성: "Spring Backend API"
3. Environment 설정:
   - DEV: http://localhost:3055/api
   - PROD: https://api.example.com/api
```

#### 기본 요청 예시
```json
// GET /api/system/health
{
  "method": "GET",
  "url": "{{baseUrl}}/system/health",
  "headers": {
    "Content-Type": "application/json"
  }
}
```

### 2. 데이터베이스 클라이언트

#### DBeaver (무료, 추천)
```
1. https://dbeaver.io/download/ 에서 다운로드
2. 새 연결 생성:
   - Server Host: localhost
   - Port: 3306
   - Database: spring_backend_db
   - Username: spring_user
   - Password: spring_password
```

#### MySQL Workbench (MySQL 공식)
```
1. https://dev.mysql.com/downloads/workbench/ 에서 다운로드
2. 연결 설정 동일
```

### 3. Redis 클라이언트

#### RedisInsight (무료, 공식)
```
1. https://redis.com/redis-enterprise/redis-insight/ 에서 다운로드
2. 연결 설정:
   - Host: localhost
   - Port: 6379
```

#### Redis CLI (명령줄)
```bash
# 연결
redis-cli

# 기본 명령어
127.0.0.1:6379> ping
127.0.0.1:6379> info
127.0.0.1:6379> keys *
```

---

## 🌍 환경별 설정

### 1. 개발 환경 (local)

#### application-dev.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_backend_dev
    username: dev_user
    password: dev_password
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  redis:
    host: localhost
    port: 6379

logging:
  level:
    "[com.iroomclass.spring_backend]": DEBUG
    "[org.hibernate.SQL]": DEBUG
```

### 2. 테스트 환경

#### application-test.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  h2:
    console:
      enabled: true
```

### 3. 운영 환경

#### application-prod.yml
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://prod-db:3306/spring_backend}
    username: ${DB_USERNAME:prod_user}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  redis:
    host: ${REDIS_HOST:prod-redis}
    port: ${REDIS_PORT:6379}

logging:
  level:
    "[com.iroomclass.spring_backend]": INFO
    root: WARN
```

### 4. 프로필별 실행

```bash
# 개발 환경
./gradlew bootRun --args='--spring.profiles.active=dev'

# 테스트 환경
./gradlew bootRun --args='--spring.profiles.active=test'

# 운영 환경 (환경변수 필요)
export DB_PASSWORD=prod_password
export REDIS_HOST=redis.example.com
./gradlew bootRun --args='--spring.profiles.active=prod'
```

---

## ✅ 검증 및 테스트

### 1. 환경 설정 검증

#### 체크리스트
```bash
# Java 버전 확인
java -version | grep "21"

# MySQL 연결 확인
mysql -u spring_user -p spring_backend_db -e "SELECT 1;"

# Redis 연결 확인
redis-cli ping

# 프로젝트 빌드 확인
./gradlew clean build

# 애플리케이션 실행 확인
curl http://localhost:3055/api/system/health
```

### 2. 기능 테스트

#### API 엔드포인트 테스트
```bash
# 헬스 체크
curl -X GET http://localhost:3055/api/system/health

# 예상 응답
{
  "result": "SUCCESS",
  "message": "",
  "data": {
    "status": "UP",
    "timestamp": "2024-08-17T10:30:00",
    "message": "서버가 정상적으로 작동중입니다"
  }
}
```

#### 에코 API 테스트
```bash
# POST 요청
curl -X POST http://localhost:3055/api/system/echo \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello World"}'

# 예상 응답
{
  "result": "SUCCESS",
  "message": "에코 메시지 처리 성공",
  "data": {
    "originalMessage": "Hello World",
    "echoMessage": "Echo: Hello World",
    "timestamp": "2024-08-17T10:30:00"
  }
}
```

### 3. 개발 도구 검증

#### Swagger UI 확인
```
1. http://localhost:3055/api/swagger-ui.html 접속
2. "시스템 API" 섹션 확인
3. "헬스 체크" API 실행 테스트
4. 응답 확인
```

#### 데이터베이스 연결 확인
```sql
-- DBeaver 또는 MySQL Workbench에서 실행
USE spring_backend_db;
SHOW TABLES;

-- 애플리케이션 실행 후 자동 생성된 테이블 확인
```

---

## 🆘 문제 해결

### 자주 발생하는 문제들

#### 1. Java 버전 문제
```bash
# 문제: Java 21이 인식되지 않음
# 해결: JAVA_HOME 환경변수 설정

# Windows
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.1.12-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

# macOS/Linux
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```

#### 2. 포트 충돌
```bash
# 문제: 포트 3055가 이미 사용 중
# 해결: 포트 변경 또는 기존 프로세스 종료

# 포트 사용 중인 프로세스 확인
netstat -ano | findstr :3055  # Windows
lsof -i :3055                 # macOS/Linux

# 프로세스 종료
taskkill /PID [PID] /F        # Windows
kill -9 [PID]                # macOS/Linux
```

#### 3. 데이터베이스 연결 실패
```yaml
# 문제: MySQL 연결 실패
# 해결: application.yml 설정 확인

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_backend_db?useSSL=false&allowPublicKeyRetrieval=true
    username: spring_user
    password: spring_password
```

### 추가 지원

#### 팀 지원 채널
- **Slack**: #dev-support 채널
- **이메일**: dev-team@company.com
- **문서**: [문제 해결 가이드](TROUBLESHOOTING.md)

#### 외부 자료
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [MySQL 설치 가이드](https://dev.mysql.com/doc/mysql-installation-excerpt/)
- [Redis 설치 가이드](https://redis.io/docs/getting-started/installation/)

---

**🎉 축하합니다! 개발 환경 설정이 완료되었습니다.**  
**이제 [팀 협업 가이드](TEAM_COLLABORATION_GUIDE.md)를 참고하여 개발을 시작해 보세요!**