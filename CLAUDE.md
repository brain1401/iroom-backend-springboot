# CLAUDE.md

This file provides comprehensive guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.4 backend application built with Gradle 8.x and Java 21. The application uses Spring Data JPA with MySQL, Spring Security, Redis for caching, and includes validation support.

**Development Philosophy**: As an experienced Senior Java Developer, always adhere to SOLID, DRY, KISS, and YAGNI principles. Follow OWASP best practices for security. Break tasks into the smallest units and solve them step by step.

## Technology Stack

- **Framework**: Spring Boot 3.5.x
- **Language**: Java 21 (actively use records for DTOs)
- **Build Tool**: Gradle 8+
- **Persistence**: Spring Data JPA (MySQL)
- **Security**: Spring Security
- **Cache**: Redis
- **Validation**: jakarta.validation
- **Utilities**: Lombok, Spring Boot DevTools

## Build and Run Commands

### Development
```bash
# Run the application in development mode with hot reload
./gradlew bootRun

# Build the project
./gradlew build

# Clean and build
./gradlew clean build

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.iroomclass.spring_backend.YourTestClass"

# Run a specific test method
./gradlew test --tests "com.iroomclass.spring_backend.YourTestClass.testMethod"
```

### Production
```bash
# Build JAR for production
./gradlew bootJar

# Run the JAR
java -jar build/libs/spring-backend-0.0.1-SNAPSHOT.jar
```

## Architecture & Development Guidelines

### Core Architectural Principles

#### 1. SOLID Principles

**Single Responsibility Principle (SRP)**
- Each class, function, or module has one reason to change
- Example: UserService handles only user management, EmailService handles only email operations

**Open/Closed Principle (OCP)**
- Software entities should be open for extension but closed for modification
- Use interfaces for payment processors, notification services, etc.

**Liskov Substitution Principle (LSP)**
- Derived classes must be substitutable for their base classes
- Ensure interface implementations maintain expected behavior

**Interface Segregation Principle (ISP)**
- Clients should not be forced to depend on interfaces they don't use
- Create specific, focused interfaces rather than large, monolithic ones

**Dependency Inversion Principle (DIP)**
- Depend on abstractions, not concretions
- Use constructor injection with interfaces

#### 2. Clean Architecture Principles

**Dependency Direction**: Controllers → Services → Repositories → Entities

**Layer Responsibilities**:
- **Controller**: HTTP request/response handling, authentication/authorization
- **Service**: Business logic, transaction boundaries
- **Repository**: Data access, query execution
- **Entity**: Domain model, business rules

### Package Structure (Domain-Driven Organization)

**Base package**: `com.iroomclass.spring_backend`

**Architecture Approach**: Following Spring Boot 3.5 official recommendations and Spring Modulith best practices, this project uses **Package-by-Feature** (domain-driven) organization instead of traditional layer-based packaging.

#### Recommended Structure

```
com.iroomclass.spring_backend/
├── SpringBackendApplication.java       # Main application class
│
├── user/                              # User domain module
│   ├── User.java                      # Entity
│   ├── UserController.java            # REST endpoints
│   ├── UserService.java               # Business logic
│   ├── UserRepository.java            # Data access
│   └── dto/
│       ├── UserCreateDto.java
│       ├── UserResponseDto.java
│       └── UserUpdateDto.java
│
├── order/                             # Order domain module
│   ├── Order.java
│   ├── OrderController.java
│   ├── OrderService.java
│   ├── OrderRepository.java
│   └── dto/
│       └── ...
│
├── product/                           # Product domain module
│   └── ...
│
├── common/                            # Cross-domain shared components
│   ├── ApiResponse.java               # Standard response wrapper
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   └── CustomExceptions.java
│   └── util/
│       └── CommonUtils.java
│
└── config/                            # Application-wide configuration
    ├── SecurityConfig.java
    ├── RedisConfig.java
    ├── JpaConfig.java
    └── WebConfig.java
```

#### Domain Module Guidelines

Each domain module should contain:
- **Entity**: JPA entity representing the core domain object
- **Controller**: REST API endpoints specific to this domain
- **Service**: Business logic and transaction boundaries for this domain
- **Repository**: Data access layer for this domain  
- **dto/**: All DTOs related to this domain (request/response/internal)

#### Domain Interaction Guidelines

**Cross-Domain Communication Patterns**:
1. **Service-to-Service Communication**: Use DTOs for cross-domain communication
2. **Avoid Direct Entity Access**: Don't inject cross-domain repositories
3. **Domain Event Pattern**: Use Spring's @EventListener for complex interactions

**Domain Dependency Rules**:
- **Allowed Dependencies**: `common/`, `config/`, other domain services (via DTOs only)
- **Forbidden Dependencies**: Other domains' entities, repositories, or internal classes
- **Circular Dependencies**: Strictly prohibited between domains

### Core Architecture Principles

1. **Domain & Layering Rules**:
   - **Domain Boundaries**: Each domain package is self-contained with its own Controller, Service, Repository, Entity, and DTOs
   - **Domain Isolation**: Domains should not directly depend on other domains' internal classes (Entity, Repository, Service)
   - **Cross-Domain Communication**: Use DTOs and Service interfaces for communication between domains
   - All HTTP request/response handling is in `@RestController` classes only
   - Business logic lives in the Service layer; direct DB access is only in Repositories
   - Controllers **must not** depend on Repositories directly (except for exceptional business needs)
   - Services **must not** query the database directly; they must use Repository methods
   - **Use DTOs (records) at I/O boundaries (Controller ↔ Service)**, never expose Entities externally
   - Entities are persistence models only (JPA management purpose)
   - Transaction boundaries are managed in Service layer: `@Transactional` for writes, `@Transactional(readOnly = true)` for reads
   - **Shared Components**: Common utilities, exceptions, and configurations belong in `common/` and `config/` packages

2. **Dependency Injection**:
   - Use **constructor injection** (immutable fields + Lombok `@RequiredArgsConstructor`)
   - Field injection (`@Autowired` on fields) is **prohibited**

3. **Pagination & Sorting**:
   - Use `Pageable`/`Page<T>` for pagination
   - Return responses wrapped in DTO (record) format

### Entity Guidelines

1. **Basic Configuration**:
   - Mark with `@Entity`
   - Primary key: `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`
   - **Default to `FetchType.LAZY`** for relationships (use EAGER only when clearly necessary)

2. **Lombok Usage for Entities**:
   - **DO NOT use `@Data`** on entities (risk of equals/hashCode/toString circular references)
   - Instead use:
     - `@Getter`, `@Setter` (limited to necessary fields)
     - `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
     - `@AllArgsConstructor` (when needed)
     - `@Builder` (when needed)

3. **Validation & Constraints**:
   - Apply validation annotations (`@Size`, `@NotBlank`) **preferably on DTOs**
   - Define schema constraints with `@Column(nullable = ..., length = ...)`

4. **Relationships**:
   - Clearly define the owning side of bidirectional relationships
   - Use convenience methods to maintain consistency
   - Default to LAZY loading, use @EntityGraph to solve N+1 problems

5. **Auditing**:
   - For time auditing: `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate`, `@LastModifiedDate`

#### Entity Implementation Pattern
```java
/**
 * 사용자 엔티티
 */
@Entity
@Getter  // @Data 사용 금지 (equals/hashCode 순환 참조 위험)
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 요구사항
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)  // 감사 기능
public class User {
    
    /**
     * 사용자 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 사용자명 (로그인용)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 이메일 (중복 불가)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // 비즈니스 메서드
    public void changePassword(String newPassword, PasswordEncoder encoder) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다");
        }
        this.password = encoder.encode(newPassword);
    }
}
```

### Repository Guidelines

1. Write repositories as **interfaces** extending `JpaRepository<Entity, ID>`
2. `@Repository` annotation is optional (Spring Data applies it automatically)
3. For custom queries, consistently use JPQL/Specifications/QueryDSL (prefer **JPQL** with `@Query`)
4. Avoid N+1 problems: use `@EntityGraph(attributePaths = {"relation"})` where needed
5. For multiple joins and partial queries: use **DTO (record) projections** (avoid loading entire entities)
6. Return `Page<T>` for pagination, delegate sorting to `Sort` or `Pageable`

#### Repository Implementation Pattern
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    @EntityGraph(attributePaths = {"profile", "roles"})
    Optional<User> findWithProfileAndRolesById(Long id);
    
    Page<User> findByActiveTrue(Pageable pageable);
    
    // N+1 문제 해결
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findWithOrdersById(@Param("id") Long id);
}
```

### Service Layer Guidelines

1. Use `@Service` classes to encapsulate business rules
2. **No forced Impl naming/separation** (avoid unnecessary complexity)
3. Service methods should return **DTOs (records)** or `void`, never expose entities beyond controller boundaries
4. Existence checks: use `repository.findById(id).orElseThrow(...)` pattern
5. Declare transaction boundaries on service methods:
   - `@Transactional` for writes (create/update/delete)
   - `@Transactional(readOnly = true)` for reads
6. Separate mapping logic (Entity ↔ DTO) into dedicated mappers (static factories/methods/MapStruct)

#### Service Implementation Pattern
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    /**
     * 사용자 ID로 조회
     * 
     * @param id 사용자 고유 식별자
     * @return 사용자 DTO
     * @throws UserNotFoundException 사용자 미존재 시
     */
    public UserDto findById(Long id) {
        return userRepository.findById(id)
            .map(UserDto::from)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + id));
    }
    
    /**
     * 사용자 생성
     */
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        // 1. 비즈니스 규칙 검증
        validateUserCreation(request);
        
        // 2. 도메인 객체 생성
        User user = User.builder()
            .username(request.username())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .active(true)
            .build();
        
        // 3. 데이터 저장
        User savedUser = userRepository.save(user);
        
        // 4. 부가 작업 (이벤트 발행, 알림 등)
        emailService.sendWelcomeEmail(savedUser);
        
        // 5. DTO 변환 후 반환
        return UserDto.from(savedUser);
    }
}
```

### DTO Guidelines

1. **Use Java `record` as the default form**:
   ```java
   public record UserDto(Long id, String name) { ... }
   ```

2. Use **compact canonical constructor** for argument validation:
   ```java
   public record UserDto(Long id, String name) {
       public UserDto {
           Objects.requireNonNull(id, "id must not be null");
           Objects.requireNonNull(name, "name must not be null");
       }
   }
   ```

3. For input DTOs: use `@Valid` with jakarta.validation annotations (`@NotBlank`, `@Email`, `@Positive`, etc.)

4. Design page response DTOs as records containing metadata (total, page, size) and data collection

#### DTO Implementation Pattern
```java
/**
 * 사용자 생성 요청 DTO
 */
public record CreateUserRequest(
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    @Schema(description = "사용자명", example = "홍길동") 
    String name,
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "이메일", example = "hong@example.com")
    String email,
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8-20자여야 합니다")
    @Schema(description = "비밀번호", example = "Password123!")
    String password
) {
    public CreateUserRequest {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(password, "password must not be null");
    }
}
```

### Controller Guidelines

1. **Class structure**:
   - `@RestController` + class-level `@RequestMapping("/api/resource")`
   - Method-level: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`

2. **Resource-oriented paths** (`/users/{id}`), **no verbs** (`/createUser` is wrong)

3. Dependency injection via constructor (`@RequiredArgsConstructor`)

4. Apply `@Valid` on method parameters for validation

5. Return type: use `ResponseEntity<ApiResponse<DTO>>` as default

6. **No unnecessary try-catch in controllers** - delegate exceptions to global exception handler (`@RestControllerAdvice`)

7. Use `@PreAuthorize` for security on endpoints that require authorization

8. Accept input as DTOs (records), never bind entities directly

#### Controller Implementation Pattern
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 관리 관련 API")
public class UserController {
    
    private final UserService userService;
    
    @Operation(
        summary = "사용자 정보 조회",
        description = "사용자 ID로 사용자 정보 조회",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
        }
    )
    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(
        @Parameter(description = "사용자 ID", example = "1") 
        @PathVariable Long id) {
        UserDto user = userService.findById(id);
        return ApiResponse.success("사용자 조회 성공", user);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto user = userService.createUser(request);
        return ApiResponse.success("사용자 생성 성공", user);
    }
}
```

### API Design Guidelines

#### RESTful API Principles

1. **Resource-Oriented Design**:
   ```http
   GET    /api/users           # List users
   GET    /api/users/123       # Get specific user
   POST   /api/users           # Create user
   PUT    /api/users/123       # Update user
   DELETE /api/users/123       # Delete user
   ```

2. **HTTP Status Codes**:
   - **200 OK**: GET, PUT, PATCH success
   - **201 Created**: POST success
   - **204 No Content**: DELETE success
   - **400 Bad Request**: Validation failures
   - **401 Unauthorized**: Authentication required
   - **403 Forbidden**: Access denied
   - **404 Not Found**: Resource not found
   - **409 Conflict**: Resource conflicts
   - **422 Unprocessable Entity**: Business logic violations

3. **Pagination and Sorting**:
   ```java
   @GetMapping("/users")
   public ApiResponse<Page<UserDto>> getUsers(
       @RequestParam(defaultValue = "0") int page,
       @RequestParam(defaultValue = "20") int size,
       @RequestParam(defaultValue = "id") String sort,
       @RequestParam(defaultValue = "asc") String direction
   ) {
       Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
       Pageable pageable = PageRequest.of(page, size, sortOrder);
       Page<UserDto> users = userService.findAll(pageable);
       return ApiResponse.success("사용자 목록 조회 성공", users);
   }
   ```

### Security & OWASP Best Practices

#### 1. Authentication and Authorization

**JWT-based Authentication**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        
        return http.build();
    }
}
```

**Method-level Security**:
```java
@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
public UserDto findById(Long id) {
    // 관리자이거나 본인만 조회 가능
}
```

#### 2. Input Validation and Security

- **Never log sensitive information** (passwords, tokens)
- Handle validation failures/binding errors with standardized error responses
- Hide sensitive fields during serialization
- **Never expose detailed stack traces** to clients
- Configure CORS/CSRF/security headers (HSTS, X-Content-Type-Options) according to environment
- Use parameterized queries to prevent SQL injection
- Validate and sanitize all input data

### Performance Optimization

#### 1. Database Optimization

**N+1 Problem Solutions**:
```java
// Use @EntityGraph
@EntityGraph(attributePaths = {"orders", "profile"})
List<User> findAllWithOrdersAndProfile();

// Use Fetch Join
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();

// Batch Size Configuration
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 20
```

**Pagination for Large Datasets**:
```java
// Use Slice for better performance (no count query)
public Slice<UserDto> findAllSlice(Pageable pageable) {
    return userRepository.findAll(pageable).map(UserDto::from);
}
```

#### 2. Caching with Redis

```java
@Service
public class UserService {
    
    // Cache frequently accessed data
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public UserDto findById(Long id) {
        return userRepository.findById(id)
            .map(UserDto::from)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
    
    // Evict cache on updates
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        // Update logic
    }
}
```

### Logging & Observability

1. Use `slf4j` for logging
2. Bind request-response correlation ID (e.g., `X-Request-Id`) with MDC
3. Log exceptions in global exception handler, include only summary messages in responses

#### Structured Logging Pattern
```java
@Service
@Slf4j
public class UserService {
    
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating user: username={}, email={}", 
                request.username(), request.email());
        
        try {
            UserDto user = // creation logic
            
            log.info("User created successfully: id={}, username={}", 
                    user.id(), user.username());
            
            return user;
        } catch (Exception e) {
            log.error("Failed to create user: username={}, error={}", 
                     request.username(), e.getMessage(), e);
            throw e;
        }
    }
}
```

## Code Documentation & Comments

### 1. JavaDoc 사용 규칙

**주석**:
- **존댓말 사용 금지**, 간단한 명사형 표현 사용
- JavaDoc 작성 시 불렛 포인트 사용.
- 일반 주석 작성 시, 간결하고 명료한 **한국어 명사형** 주석 사용.
- 주석 작성 시 주석 내용을 최대한 간결하게 작성.

#### JavaDoc 작성 스타일
```java
/**
 * 사용자 정보 조회
 * @param id 사용자 ID
 * @return 사용자 DTO
 * @throws UserNotFoundException 사용자 미존재 시
 */
public UserDto findUser(Long id) { ... }

/**
 * 게시글 목록 페이징 조회
 * @param pageable 페이징 정보
 * @return 게시글 페이지 응답
 */
@Transactional(readOnly = true)
public Page<PostDto> findPosts(Pageable pageable) { ... }

/**
 * 사용자 권한 검증 및 인증
 * • JWT 토큰 유효성 확인
 * • 사용자 권한 레벨 체크
 * • 접근 가능 리소스 검증
 * @param token JWT 토큰
 * @param resource 접근 리소스
 * @return 인증 결과
 */
public AuthResult validateAccess(String token, String resource) { ... }
```

#### Entity 주석 예시
```java
/**
 * 사용자 엔티티
 */
@Entity
public class User {
    /**
     * 사용자 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 사용자명 (로그인용)
     */
    @Column(unique = true, nullable = false)
    private String username;
}
```

### 2. 인라인 주석 규칙

- 복잡한 비즈니스 로직에만 필요시 간단히 작성
- **명사형 표현** 사용: `// 중복 검증`, `// 권한 확인`, `// 캐시 갱신`
- 자명한 코드에는 주석 작성 금지
- **복잡한 로직의 경우 불렛 포인트 사용**:
  ```java
  // 사용자 인증 처리 과정:
  // • 토큰 유효성 검증
  // • 사용자 권한 확인
  // • 세션 갱신
  // • 로그 기록
  ```

### 3. 주석 품질 기준

- 한 줄에 핵심 내용 요약
- 매개변수/반환값 명확히 설명
- 예외 상황 명시
- 비즈니스 규칙이나 제약사항 설명

## Configuration

### Application Configuration

The application uses `application.yml` for configuration. Environment-specific configurations should be added as:
- `application-dev.yml` for development
- `application-prod.yml` for production

Run with specific profile: `./gradlew bootRun --args='--spring.profiles.active=dev'`

### Database Setup

Before running the application, ensure MySQL is running and create a database. Configure the connection in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database_name
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update  # or validate for production
```

### Redis Setup

Redis is required for caching. Configure in `application.yml`:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

### Environment-Specific Configuration

#### Development Environment
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_backend_dev
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
logging:
  level:
    "[com.iroomclass.spring_backend]": DEBUG
```

#### Production Environment
```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://prod-db:3306/spring_backend}
    username: ${DB_USERNAME:prod_user}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
logging:
  level:
    "[com.iroomclass.spring_backend]": INFO
    root: WARN
```

## Standard Response Wrapper

**Location**: `com.iroomclass.spring_backend.common.ApiResponse`

```java
package com.iroomclass.spring_backend.common;

public record ApiResponse<T>(String result, String message, T data) {
    public ApiResponse {
        // Immutability: result, message cannot be null
        if (result == null || result.isBlank()) 
            throw new IllegalArgumentException("result must not be blank");
        if (message == null) 
            throw new IllegalArgumentException("message must not be null");
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }
    
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }
    
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}
```

## Development Workflow

### Feature Development Process

1. **Issue Creation**: Create detailed issue with acceptance criteria
2. **Branch Creation**: `feature/기능명` or `bugfix/버그명`
3. **Domain Package Creation**: Set up domain-driven structure
4. **DTO Design & Implementation**: Design request/response DTOs
5. **Controller Implementation**: REST endpoints with validation
6. **Service Implementation**: Business logic with transactions
7. **Repository Implementation**: Data access layer
8. **Test Creation**: Unit and integration tests
9. **API Documentation**: Swagger/OpenAPI documentation
10. **Code Review**: Team review following standards
11. **Merge**: Integration into main branch

### Code Review Checklist

#### Architecture & Design
- [ ] Domain-Driven Package structure used
- [ ] Proper layer separation (Controller-Service-Repository)
- [ ] Single responsibility principle adherence

#### Coding Style
- [ ] Java 21 Record usage for DTOs
- [ ] Constructor injection pattern used
- [ ] Korean JavaDoc documentation

#### API Design
- [ ] RESTful principles applied
- [ ] Standard response format (ApiResponse) used
- [ ] Appropriate HTTP status codes

#### Security & Validation
- [ ] Input data validation
- [ ] Authorization checks (@PreAuthorize)
- [ ] SQL Injection prevention
- [ ] No sensitive information logging

#### Performance & Quality
- [ ] N+1 problem prevention
- [ ] Appropriate transaction scope
- [ ] Test coverage verification

## Team Collaboration Guidelines

### Git Branch Strategy
- **main**: Production-ready code
- **develop**: Integration branch for features
- **feature/{기능명}**: New features
- **bugfix/{버그명}**: Bug fixes
- **hotfix/{수정명}**: Critical production fixes

### Commit Message Convention
```
{타입}: {제목}

{본문}

{푸터}
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**Example**:
```
feat: 사용자 관리 API 구현

- 사용자 조회, 생성, 수정, 삭제 API 추가
- UserDto, CreateUserRequest, UpdateUserRequest 구현
- 입력 검증 및 에러 처리 로직 추가
- Swagger API 문서화 완료

Resolves: #123
```

## Important Notes

- The original package name `com.iroomclass.spring-backend` was invalid; the project uses `com.iroomclass.spring_backend` instead
- Spring Security is included by default - you'll need to configure authentication endpoints
- Lombok is used for reducing boilerplate - ensure IDE has Lombok plugin installed
- DevTools is included for automatic restart during development
- Always use records for DTOs to leverage Java 21 features
- Follow the layering rules strictly to maintain clean architecture
- Prioritize code quality and security over rapid development

## Troubleshooting

### Common Issues

#### 1. Lombok Not Working
**Solution**: Install Lombok plugin and enable annotation processing in IDE

#### 2. Java 21 Not Recognized
**Solution**: Set JAVA_HOME environment variable correctly

#### 3. Port Conflicts
**Solution**: Check running processes on port 3055 or change port in configuration

#### 4. Database Connection Failed
**Solution**: Verify MySQL service is running and connection parameters are correct

#### 5. N+1 Query Problems
**Solution**: Use @EntityGraph or fetch joins in repository methods

### Performance Issues

#### Memory Leaks
**Solution**: Use pagination for large datasets, implement streaming for batch processing

#### Slow Queries
**Solution**: Add appropriate database indexes, use query optimization techniques

## Related Documentation

- **[Team Collaboration Guide](docs/TEAM_COLLABORATION_GUIDE.md)** - Workflow and processes
- **[Setup Guide](docs/SETUP_GUIDE.md)** - Development environment setup
- **[Coding Standards](docs/CODING_STANDARDS.md)** - Java 21 and Spring Boot conventions
- **[API Guidelines](docs/API_GUIDELINES.md)** - RESTful API design rules
- **[Architecture Document](docs/ARCHITECTURE.md)** - System design principles
- **[Troubleshooting Guide](docs/TROUBLESHOOTING.md)** - FAQ and solutions

# important-instruction-reminders
Do what has been asked; nothing more, nothing less.
NEVER create files unless they're absolutely necessary for achieving your goal.
ALWAYS prefer editing an existing file to creating a new one.
NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.