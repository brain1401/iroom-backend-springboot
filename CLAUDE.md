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

### Package Structure (Domain-Driven Organization)

**Base package**: `com.iroomclass.spring_backend`

**Architecture Approach**: Following Spring Boot 3.5 official recommendations and Spring Modulith best practices, this project uses **Package-by-Feature** (domain-driven) organization instead of traditional layer-based packaging.

#### Recommended Structure

```
com.iroomclass.spring_backend
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

#### Benefits of Domain-Driven Structure

1. **High Cohesion**: Related functionality grouped together
2. **Loose Coupling**: Clear boundaries between domains
3. **Team Scalability**: Teams can work on different domains independently
4. **Maintainability**: Easier to locate and modify domain-specific code
5. **Spring Modulith Ready**: Compatible with Spring Modulith for modular monoliths
6. **Bounded Context**: Aligns with Domain-Driven Design principles

#### Migration from Layer-Based Structure

If migrating from traditional layer-based structure:
1. Identify business domains/bounded contexts
2. Create domain packages (e.g., `user/`, `order/`, `product/`)
3. Move related Controller, Service, Repository, Entity, and DTOs into domain packages
4. Keep truly cross-cutting concerns in `common/` and `config/`
5. Update import statements and ensure no circular dependencies between domains

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

### Domain Interaction Guidelines

#### Cross-Domain Communication Patterns

1. **Service-to-Service Communication**:
   ```java
   // ✅ Correct: Use DTOs for cross-domain communication
   @Service
   public class OrderService {
       private final UserService userService;
       
       public OrderDto createOrder(OrderCreateDto dto) {
           UserDto user = userService.findById(dto.userId()); // DTO exchange
           // ... business logic
       }
   }
   ```

2. **Avoid Direct Entity Access**:
   ```java
   // ❌ Wrong: Direct access to other domain's entity
   private final UserRepository userRepository; // Don't inject cross-domain repositories
   
   // ✅ Correct: Use domain service
   private final UserService userService; // Inject service instead
   ```

3. **Domain Event Pattern** (for complex interactions):
   ```java
   // Consider using Spring's @EventListener for loose coupling
   @EventListener
   public void handleOrderCreated(OrderCreatedEvent event) {
       // Update inventory, send notifications, etc.
   }
   ```

#### Domain Dependency Management

- **Allowed Dependencies**: `common/`, `config/`, other domain services (via DTOs only)
- **Forbidden Dependencies**: Other domains' entities, repositories, or internal classes
- **Circular Dependencies**: Strictly prohibited between domains
- **Shared Logic**: Extract to `common/` package if used by multiple domains

#### Testing Domain Boundaries

- **Unit Tests**: Test each domain in isolation
- **Integration Tests**: Test cross-domain interactions through service interfaces
- **Architecture Tests**: Use ArchUnit to enforce domain boundary rules

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

5. **Auditing**:
   - For time auditing: `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate`, `@LastModifiedDate`

### Repository Guidelines

1. Write repositories as **interfaces** extending `JpaRepository<Entity, ID>`
2. `@Repository` annotation is optional (Spring Data applies it automatically)
3. For custom queries, consistently use JPQL/Specifications/QueryDSL (prefer **JPQL** with `@Query`)
4. Avoid N+1 problems: use `@EntityGraph(attributePaths = {"relation"})` where needed
5. For multiple joins and partial queries: use **DTO (record) projections** (avoid loading entire entities)
6. Return `Page<T>` for pagination, delegate sorting to `Sort` or `Pageable`

### Service Layer Guidelines

1. Use `@Service` classes to encapsulate business rules
2. **No forced Impl naming/separation** (avoid unnecessary complexity)
3. Service methods should return **DTOs (records)** or `void`, never expose entities beyond controller boundaries
4. Existence checks: use `repository.findById(id).orElseThrow(...)` pattern
5. Declare transaction boundaries on service methods:
   - `@Transactional` for writes (create/update/delete)
   - `@Transactional(readOnly = true)` for reads
6. Separate mapping logic (Entity ↔ DTO) into dedicated mappers (static factories/methods/MapStruct)

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

### Security & OWASP Best Practices

1. Separate authentication from authorization
2. **Never log sensitive information** (passwords, tokens)
3. Handle validation failures/binding errors with standardized error responses
4. Hide sensitive fields during serialization
5. **Never expose detailed stack traces** to clients
6. Configure CORS/CSRF/security headers (HSTS, X-Content-Type-Options) according to environment

### Caching with Redis

1. Use `@Cacheable` for high-read, low-change queries
2. Use `@CacheEvict`/`@CachePut` on update paths
3. Define consistent cache key rules and set TTL

### Logging & Observability

1. Use `slf4j` for logging
2. Bind request-response correlation ID (e.g., `X-Request-Id`) with MDC
3. Log exceptions in global exception handler, include only summary messages in responses

### Code Documentation & Comments

1. **JavaDoc 사용 규칙**:
   - 모든 public 클래스, 메서드에 JavaDoc 작성
   - 간결하고 명료한 한국어 명사형 주석 사용
   - 존댓말 사용 금지, 간단한 명사형 표현 사용

2. **주석 작성 스타일**:
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
   ```

3. **인라인 주석 규칙**:
   - 복잡한 비즈니스 로직에만 필요시 간단히 작성
   - 명사형 표현 사용: `// 중복 검증`, `// 권한 확인`, `// 캐시 갱신`
   - 자명한 코드에는 주석 작성 금지

4. **Entity 주석 예시**:
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

5. **주석 품질 기준**:
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

## Important Notes

- The original package name `com.iroomclass.spring-backend` was invalid; the project uses `com.iroomclass.spring_backend` instead
- Spring Security is included by default - you'll need to configure authentication endpoints
- Lombok is used for reducing boilerplate - ensure IDE has Lombok plugin installed
- DevTools is included for automatic restart during development
- Always use records for DTOs to leverage Java 21 features
- Follow the layering rules strictly to maintain clean architecture
- Prioritize code quality and security over rapid development