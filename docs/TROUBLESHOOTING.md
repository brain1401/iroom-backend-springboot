# 🚨 문제 해결 가이드

> **Spring Boot 3.5.4 프로젝트에서 자주 발생하는 문제와 해결 방법**

## 📋 목차

1. [개발 환경 문제](#-개발-환경-문제)
2. [빌드 및 실행 문제](#-빌드-및-실행-문제)
3. [데이터베이스 연결 문제](#-데이터베이스-연결-문제)
4. [API 관련 문제](#-api-관련-문제)
5. [인증/보안 문제](#-인증보안-문제)
6. [성능 문제](#-성능-문제)
7. [로깅 및 디버깅](#-로깅-및-디버깅)
8. [배포 관련 문제](#-배포-관련-문제)

---

## 🛠️ 개발 환경 문제

### Q1: Lombok이 작동하지 않습니다

#### 증상
```java
// 컴파일 에러 발생
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;  // Cannot resolve symbol 'userRepository'
}
```

#### 해결 방법

**IntelliJ IDEA**
```
1. Settings → Plugins → Lombok 플러그인 설치
2. Settings → Build → Compiler → Annotation Processors
   ✅ Enable annotation processing 체크
3. 프로젝트 재시작
```

**Eclipse**
```
1. Help → Eclipse Marketplace → "Lombok" 검색 후 설치
2. 또는 lombok.jar 다운로드 후 java -jar lombok.jar 실행
3. Eclipse 재시작
```

**VS Code**
```
1. Extensions → "Lombok Annotations Support for VS Code" 설치
2. Java Extension Pack 최신 버전 확인
```

#### 검증
```java
// 다음 코드가 정상 작동하는지 확인
@Data
public class TestClass {
    private String name;
}

// IDE에서 getName(), setName() 메서드가 인식되는지 확인
```

---

### Q2: Java 21이 인식되지 않습니다

#### 증상
```bash
java -version
# openjdk version "11.0.x" 또는 다른 버전 출력
```

#### 해결 방법

**Windows**
```cmd
# JAVA_HOME 환경변수 설정
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.1.12-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

# 영구 설정: 시스템 환경변수에서 설정
```

**macOS**
```bash
# ~/.zshrc 또는 ~/.bash_profile에 추가
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 적용
source ~/.zshrc
```

**Ubuntu/Linux**
```bash
# ~/.bashrc에 추가
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# 적용
source ~/.bashrc
```

#### 검증
```bash
java -version
# openjdk version "21.0.1" 출력 확인

echo $JAVA_HOME
# Java 21 경로 출력 확인
```

---

## 🔨 빌드 및 실행 문제

### Q3: Gradle 빌드가 실패합니다

#### 증상
```bash
./gradlew build
# BUILD FAILED 메시지와 함께 실패
```

#### 해결 방법

**권한 문제 (Unix/Linux/macOS)**
```bash
# gradlew 실행 권한 부여
chmod +x gradlew

# 다시 빌드 시도
./gradlew build
```

**의존성 문제**
```bash
# Gradle 캐시 정리
./gradlew clean

# 의존성 새로고침
./gradlew build --refresh-dependencies

# 또는 Gradle Wrapper 재설정
./gradlew wrapper --gradle-version 8.14.3
```

**메모리 부족**
```bash
# gradle.properties 파일에 추가
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m

# 또는 환경변수 설정
export GRADLE_OPTS="-Xmx2048m"
```

---

### Q4: 애플리케이션 실행 시 포트 충돌이 발생합니다

#### 증상
```
***************************
APPLICATION FAILED TO START
***************************

Description:
Web server failed to start. Port 3055 was already in use.
```

#### 해결 방법

**포트 사용 프로세스 확인 및 종료**
```bash
# Windows
netstat -ano | findstr :3055
taskkill /PID [PID번호] /F

# macOS/Linux
lsof -i :3055
kill -9 [PID번호]
```

**다른 포트 사용**
```yaml
# application.yml
server:
  port: 3056  # 다른 포트로 변경
```

**랜덤 포트 사용 (개발 시)**
```yaml
# application.yml
server:
  port: 0  # 사용 가능한 랜덤 포트 자동 할당
```

---

## 🗄️ 데이터베이스 연결 문제

### Q5: MySQL 연결이 실패합니다

#### 증상
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: 
Communications link failure
```

#### 해결 방법

**MySQL 서비스 상태 확인**
```bash
# Windows
net start mysql80

# macOS
brew services start mysql

# Ubuntu/Linux
sudo systemctl start mysql
sudo systemctl status mysql
```

**연결 정보 확인**
```yaml
# application.yml - 올바른 설정
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_backend_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
    username: spring_user
    password: spring_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**데이터베이스 및 사용자 생성 확인**
```sql
-- MySQL에 접속하여 확인
mysql -u root -p

-- 데이터베이스 존재 확인
SHOW DATABASES;

-- 사용자 권한 확인
SELECT User, Host FROM mysql.user WHERE User = 'spring_user';
SHOW GRANTS FOR 'spring_user'@'localhost';
```

---

### Q6: JPA 엔티티가 테이블로 생성되지 않습니다

#### 증상
```
Table 'spring_backend_db.users' doesn't exist
```

#### 해결 방법

**DDL 설정 확인**
```yaml
# application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # create, create-drop, update, validate 중 선택
    show-sql: true
```

**엔티티 스캔 경로 확인**
```java
@SpringBootApplication
@EntityScan(basePackages = "com.iroomclass.spring_backend.domain.*.entity")
public class SpringBackendApplication {
    // ...
}
```

**엔티티 어노테이션 확인**
```java
@Entity  // 필수
@Table(name = "users")  // 테이블명 명시적 지정
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ...
}
```

---

## 🌐 API 관련 문제

### Q7: Swagger UI에 접근할 수 없습니다

#### 증상
```
http://localhost:3055/api/swagger-ui.html → 404 Not Found
```

#### 해결 방법

**URL 확인**
```
✅ 올바른 URL: http://localhost:3055/api/swagger-ui.html
❌ 잘못된 URL: http://localhost:3055/swagger-ui.html
```

**SpringDoc 의존성 확인**
```gradle
// build.gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
```

**Security 설정 확인**
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**").permitAll()
            // ...
        );
        return http.build();
    }
}
```

---

### Q8: CORS 에러가 발생합니다

#### 증상
```
Access to XMLHttpRequest at 'http://localhost:3055/api/users' 
from origin 'http://localhost:3000' has been blocked by CORS policy
```

#### 해결 방법

**CORS 설정 확인**
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**컨트롤러 레벨 CORS**
```java
@RestController
@CrossOrigin(origins = "http://localhost:3000")  // 특정 컨트롤러만
public class UserController {
    // ...
}
```

---

### Q9: JSON 직렬화/역직렬화 문제

#### 증상
```json
{
  "timestamp": "2024-08-17T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot deserialize value"
}
```

#### 해결 방법

**LocalDateTime 직렬화 설정**
```yaml
# application.yml
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false
    time-zone: Asia/Seoul
```

**Record validation 문제**
```java
// ✅ 올바른 validation
public record CreateUserRequest(
    @NotBlank String name,
    @Email String email
) {
    // Compact constructor는 validation 후에 실행됨
    public CreateUserRequest {
        Objects.requireNonNull(name, "name must not be null");
    }
}
```

---

## 🔐 인증/보안 문제

### Q10: JWT 토큰 인증이 실패합니다

#### 증상
```json
{
  "error": "Unauthorized",
  "message": "JWT signature does not match locally computed signature"
}
```

#### 해결 방법

**JWT 설정 확인**
```yaml
# application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-auth-server.com
          jwk-set-uri: https://your-auth-server.com/.well-known/jwks.json
```

**헤더 형식 확인**
```http
# ✅ 올바른 형식
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

# ❌ 잘못된 형식
Authorization: eyJhbGciOiJIUzI1NiIs...  # Bearer 누락
```

---

### Q11: @PreAuthorize가 작동하지 않습니다

#### 증상
```java
@PreAuthorize("hasRole('ADMIN')")
public UserDto deleteUser(Long id) {
    // 권한 검사 없이 실행됨
}
```

#### 해결 방법

**메서드 보안 활성화**
```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // ...
}
```

**권한 이름 확인**
```java
// Spring Security는 기본적으로 ROLE_ 접두사를 추가
@PreAuthorize("hasRole('USER')")  // 실제로는 ROLE_USER 확인
// 또는
@PreAuthorize("hasAuthority('ROLE_USER')")  // 명시적으로 전체 이름 사용
```

---

## ⚡ 성능 문제

### Q12: N+1 쿼리 문제가 발생합니다

#### 증상
```
Hibernate: select user0_.id, user0_.name from users user0_
Hibernate: select orders0_.user_id, orders0_.id from orders orders0_ where orders0_.user_id=?
Hibernate: select orders0_.user_id, orders0_.id from orders orders0_ where orders0_.user_id=?
# 사용자 수만큼 반복...
```

#### 해결 방법

**Fetch Join 사용**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
    List<User> findAllWithOrders();
    
    // 또는 @EntityGraph 사용
    @EntityGraph(attributePaths = {"orders"})
    List<User> findAll();
}
```

**Batch Size 설정**
```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 20
```

---

### Q13: 메모리 누수가 발생합니다

#### 증상
```
java.lang.OutOfMemoryError: Java heap space
```

#### 해결 방법

**대용량 데이터 처리 최적화**
```java
// ❌ 메모리 문제 발생 가능
public List<UserDto> getAllUsers() {
    return userRepository.findAll()  // 모든 데이터를 메모리에 로드
        .stream()
        .map(UserDto::from)
        .collect(Collectors.toList());
}

// ✅ 페이징 처리
public Page<UserDto> getUsers(Pageable pageable) {
    return userRepository.findAll(pageable)
        .map(UserDto::from);
}

// ✅ 스트림 처리
@Transactional(readOnly = true)
public void processAllUsers() {
    try (Stream<User> userStream = userRepository.streamAll()) {
        userStream
            .map(this::processUser)
            .forEach(this::saveResult);
    }
}
```

**JVM 메모리 설정**
```bash
# 실행 시 메모리 설정
java -Xms512m -Xmx2048m -jar spring-backend.jar

# 또는 application.yml
server:
  tomcat:
    max-threads: 200
    min-spare-threads: 10
```

---

## 📝 로깅 및 디버깅

### Q14: 로그가 파일에 기록되지 않습니다

#### 증상
```
콘솔에는 로그가 출력되지만 logs/ 폴더에 파일이 생성되지 않음
```

#### 해결 방법

**로그 설정 확인**
```yaml
# application.yml
logging:
  file:
    path: logs  # 폴더 경로
  level:
    "[com.iroomclass.spring_backend]": DEBUG
```

**logback-spring.xml 확인**
```xml
<configuration>
    <springProperty scope="context" name="LOG_PATH" source="logging.file.path" defaultValue="logs"/>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/spring-backend.log</file>
        <!-- 설정 내용 -->
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

**폴더 권한 확인**
```bash
# 로그 폴더 생성 및 권한 설정
mkdir -p logs
chmod 755 logs
```

---

### Q15: 디버깅 정보가 부족합니다

#### 증상
```
예외가 발생했지만 상세한 정보를 알 수 없음
```

#### 해결 방법

**개발 환경 로그 레벨 조정**
```yaml
# application-dev.yml
logging:
  level:
    "[org.springframework.web]": DEBUG
    "[org.hibernate.SQL]": DEBUG
    "[org.hibernate.type.descriptor.sql.BasicBinder]": TRACE
    "[com.iroomclass.spring_backend]": DEBUG
```

**예외 처리 개선**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);  // 스택 트레이스 포함
        
        // 개발 환경에서만 상세 정보 노출
        if (isDevelopmentMode()) {
            return ApiResponse.error("서버 오류: " + e.getMessage());
        }
        return ApiResponse.error("서버 내부 오류가 발생했습니다");
    }
}
```

---

## 🚀 배포 관련 문제

### Q16: Docker 컨테이너에서 애플리케이션이 시작되지 않습니다

#### 증상
```bash
docker logs spring-backend-container
# Error: Could not find or load main class
```

#### 해결 방법

**Dockerfile 확인**
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# JAR 파일명 확인
COPY build/libs/spring-backend-0.0.1-SNAPSHOT.jar app.jar

# 포트 노출
EXPOSE 3055

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose 네트워크 설정**
```yaml
version: '3.8'
services:
  app:
    build: .
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=mysql  # 서비스명 사용
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
    networks:
      - app-network

  mysql:
    image: mysql:8.0
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

---

### Q17: 운영 환경에서 성능이 저하됩니다

#### 증상
```
로컬에서는 빠르지만 운영 환경에서 응답 시간이 느림
```

#### 해결 방법

**JVM 성능 튜닝**
```bash
# 운영 환경 JVM 옵션
java -Xms1g -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+PrintGC \
     -XX:+PrintGCDetails \
     -jar spring-backend.jar
```

**데이터베이스 연결 풀 조정**
```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**캐시 설정 최적화**
```yaml
# application-prod.yml
spring:
  cache:
    type: redis
  redis:
    timeout: 2000ms
    jedis:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

---

## 🆘 긴급 상황 대응

### 심각한 오류 발생 시 체크리스트

#### 1. 즉시 확인사항
- [ ] 서비스 상태 확인 (`/api/system/health`)
- [ ] 에러 로그 확인 (`logs/spring-backend-error.log`)
- [ ] 데이터베이스 연결 상태 확인
- [ ] Redis 연결 상태 확인
- [ ] 시스템 리소스 확인 (CPU, 메모리, 디스크)

#### 2. 롤백 절차
```bash
# 1. 이전 버전으로 롤백
docker stop spring-backend-container
docker run -d --name spring-backend-container-rollback previous-image:tag

# 2. 데이터베이스 롤백 (필요 시)
mysql -u root -p < backup_before_deployment.sql

# 3. 모니터링 및 검증
curl http://localhost:3055/api/system/health
```

#### 3. 문제 보고
```markdown
## 긴급 이슈 보고

### 발생 시간
2024-08-17 14:30:00 KST

### 증상
- API 응답 시간 지연 (5초 이상)
- 데이터베이스 연결 오류 급증

### 영향 범위
- 사용자 인증 API (/api/auth/*)
- 주문 처리 API (/api/orders/*)

### 임시 조치
- 서버 재시작 완료
- 캐시 초기화 완료

### 근본 원인 분석 필요
- [ ] 데이터베이스 성능 분석
- [ ] 애플리케이션 메모리 사용량 분석
- [ ] 네트워크 연결 상태 확인
```

---

## 📞 추가 지원

### 팀 내 지원

| 문제 유형 | 담당자 | 연락처 | 응답 시간 |
|-----------|--------|--------|-----------|
| **인프라 문제** | DevOps 팀 | #devops-support | 30분 내 |
| **데이터베이스** | DBA 팀 | #database-support | 1시간 내 |
| **애플리케이션** | 백엔드 팀 리드 | @backend-lead | 2시간 내 |
| **보안 문제** | 보안 팀 | #security-team | 즉시 |

### 외부 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Boot 트러블슈팅](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.troubleshooting)
- [Baeldung Spring 가이드](https://www.baeldung.com/spring-boot)
- [Stack Overflow - Spring Boot](https://stackoverflow.com/questions/tagged/spring-boot)

### 로그 분석 도구

- **ELK Stack**: 로그 수집 및 분석
- **Grafana**: 메트릭 시각화
- **Sentry**: 에러 추적 및 알림
- **New Relic**: APM 모니터링

---

**⚡ 문제가 해결되지 않으면 주저하지 말고 팀에 도움을 요청하세요!**  
**모든 문제는 학습의 기회이며, 해결 과정을 문서화하여 팀 전체의 지식이 됩니다.**