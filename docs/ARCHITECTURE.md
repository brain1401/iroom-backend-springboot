# 🏗️ 시스템 아키텍처 문서

> **Spring Boot 3.5.4 기반 백엔드 시스템의 아키텍처 설계 및 구현 원칙**

## 📋 목차

1. [시스템 개요](#-시스템-개요)
2. [아키텍처 원칙](#-아키텍처-원칙)
3. [계층 구조](#-계층-구조)
4. [패키지 설계](#-패키지-설계)
5. [데이터 아키텍처](#-데이터-아키텍처)
6. [보안 아키텍처](#-보안-아키텍처)
7. [성능 및 확장성](#-성능-및-확장성)
8. [배포 아키텍처](#-배포-아키텍처)
9. [모니터링 및 로깅](#-모니터링-및-로깅)

---

## 🎯 시스템 개요

### 아키텍처 스타일

```
┌─────────────────────────────────────────────────────────┐
│                    Client Layer                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ Web Browser │  │ Mobile App  │  │ Third Party │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                      API Gateway                        │
│           ┌─────────────────────────────────┐           │
│           │        Load Balancer            │           │
│           └─────────────────────────────────┘           │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                 Application Layer                       │
│  ┌─────────────────────────────────────────────────┐   │
│  │          Spring Boot Application                │   │
│  │                                                 │   │
│  │  ┌───────────┐ ┌───────────┐ ┌───────────┐     │   │
│  │  │Controller │ │ Service   │ │Repository │     │   │
│  │  │  Layer    │ │  Layer    │ │  Layer    │     │   │
│  │  └───────────┘ └───────────┘ └───────────┘     │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                   Data Layer                            │
│  ┌─────────────┐                 ┌─────────────┐       │
│  │    MySQL    │                 │    Redis    │       │
│  │  (Primary)  │                 │   (Cache)   │       │
│  └─────────────┘                 └─────────────┘       │
└─────────────────────────────────────────────────────────┘
```

### 핵심 특징

- **Monolithic Architecture**: 초기 개발 및 운영 단순화
- **Domain-Driven Design**: 비즈니스 도메인 중심 구조
- **Layered Architecture**: 명확한 계층 분리
- **RESTful API**: 표준 REST 원칙 준수
- **Event-Driven**: 향후 마이크로서비스 전환 준비

---

## 📐 아키텍처 원칙

### 1. SOLID 원칙

#### Single Responsibility Principle (SRP)
```java
// ✅ 단일 책임: 사용자 관리만 담당
@Service
public class UserService {
    public UserDto createUser(CreateUserRequest request) { ... }
    public UserDto findById(Long id) { ... }
    public void deleteUser(Long id) { ... }
}

// ✅ 단일 책임: 이메일 발송만 담당
@Service
public class EmailService {
    public void sendWelcomeEmail(User user) { ... }
    public void sendPasswordResetEmail(User user) { ... }
}
```

#### Open/Closed Principle (OCP)
```java
// ✅ 확장에는 열려있고 수정에는 닫혀있음
public interface PaymentProcessor {
    PaymentResult process(PaymentRequest request);
}

@Service
public class CreditCardProcessor implements PaymentProcessor {
    // 신용카드 결제 구현
}

@Service
public class BankTransferProcessor implements PaymentProcessor {
    // 계좌이체 결제 구현
}
```

#### Dependency Inversion Principle (DIP)
```java
// ✅ 추상화에 의존, 구체화에 의존하지 않음
@Service
public class OrderService {
    private final PaymentProcessor paymentProcessor;  // 인터페이스에 의존
    private final NotificationService notificationService;  // 인터페이스에 의존
    
    // 생성자 주입으로 의존성 역전
    public OrderService(PaymentProcessor paymentProcessor, 
                       NotificationService notificationService) {
        this.paymentProcessor = paymentProcessor;
        this.notificationService = notificationService;
    }
}
```

### 2. Clean Architecture 원칙

#### 의존성 방향
```
┌─────────────────┐
│   Controllers   │ ──┐
└─────────────────┘   │
                      ▼
┌─────────────────┐   │
│    Services     │ ◄─┘
└─────────────────┘   │
                      ▼
┌─────────────────┐   │
│  Repositories   │ ◄─┘
└─────────────────┘   │
                      ▼
┌─────────────────┐   │
│    Entities     │ ◄─┘
└─────────────────┘
```

#### 계층별 책임
| 계층 | 책임 | 의존성 |
|------|------|--------|
| **Controller** | HTTP 요청/응답 처리, 인증/인가 | Service |
| **Service** | 비즈니스 로직, 트랜잭션 관리 | Repository, Entity |
| **Repository** | 데이터 접근, 쿼리 실행 | Entity |
| **Entity** | 도메인 모델, 비즈니스 규칙 | 없음 |

---

## 🔄 계층 구조

### 1. Presentation Layer (Controller)

#### 책임과 역할
- HTTP 요청/응답 처리
- 입력 데이터 검증 (`@Valid`)
- 인증/인가 처리 (`@PreAuthorize`)
- API 문서화 (`@Tag`, `@Operation`)

#### 구현 패턴
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자 API")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto user = userService.createUser(request);
        return ApiResponse.success("사용자 생성 성공", user);
    }
}
```

### 2. Business Layer (Service)

#### 책임과 역할
- 비즈니스 로직 실행
- 트랜잭션 경계 관리
- 도메인 객체 조작
- 외부 서비스 연동

#### 구현 패턴
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
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

### 3. Persistence Layer (Repository)

#### 책임과 역할
- 데이터 접근 추상화
- 쿼리 실행 및 최적화
- 트랜잭션 경계 내에서 동작

#### 구현 패턴
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    @EntityGraph(attributePaths = {"profile", "roles"})
    Optional<User> findWithProfileAndRolesById(Long id);
    
    Page<User> findByActiveTrue(Pageable pageable);
}
```

### 4. Domain Layer (Entity)

#### 책임과 역할
- 도메인 모델 표현
- 비즈니스 규칙 캡슐화
- 데이터 무결성 보장

#### 구현 패턴
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    // 비즈니스 메서드
    public void changePassword(String newPassword, PasswordEncoder encoder) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다");
        }
        this.password = encoder.encode(newPassword);
    }
    
    public boolean isActive() {
        return this.active;
    }
}
```

---

## 📦 패키지 설계

### Domain-Driven Package Structure

```
com.iroomclass.spring_backend/
├── domain/                           # 도메인별 패키지
│   ├── user/                        # 사용자 도메인
│   │   ├── controller/
│   │   │   └── UserController.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   └── UserQueryService.java    # CQRS 패턴
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   └── UserProfile.java
│   │   └── dto/
│   │       ├── UserDto.java
│   │       ├── CreateUserRequest.java
│   │       └── UpdateUserRequest.java
│   ├── order/                       # 주문 도메인
│   └── product/                     # 상품 도메인
├── common/                          # 공통 모듈
│   ├── ApiResponse.java            # 표준 응답
│   ├── ResultStatus.java           # 응답 상태
│   ├── exception/                   # 예외 처리
│   │   ├── GlobalExceptionHandler.java
│   │   └── BusinessException.java
│   └── util/                       # 유틸리티
│       └── DateUtils.java
└── config/                         # 설정 모듈
    ├── SecurityConfig.java
    ├── JpaConfig.java
    ├── RedisConfig.java
    └── OpenApiConfig.java
```

### 도메인 간 통신 규칙

#### ✅ 허용되는 의존성
```java
// Service 간 통신 (DTO 사용)
@Service
public class OrderService {
    private final UserService userService;  // 다른 도메인 서비스 사용 가능
    
    public OrderDto createOrder(CreateOrderRequest request) {
        UserDto user = userService.findById(request.userId());  // DTO 교환
        // 주문 생성 로직
    }
}
```

#### ❌ 금지되는 의존성
```java
// Entity 직접 참조 금지
@Service
public class OrderService {
    private final UserRepository userRepository;  // 다른 도메인 Repository 직접 사용 금지
    
    public OrderDto createOrder(CreateOrderRequest request) {
        User user = userRepository.findById(request.userId());  // 금지!
    }
}
```

---

## 🗄️ 데이터 아키텍처

### 1. 데이터베이스 설계

#### 논리적 구조
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      Users      │    │     Orders      │    │   Order_Items   │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ id (PK)         │    │ id (PK)         │    │ id (PK)         │
│ username        │◄───┤ user_id (FK)    │    │ order_id (FK)   │──┐
│ email           │    │ total_amount    │◄───┤ product_id (FK) │  │
│ password_hash   │    │ status          │    │ quantity        │  │
│ active          │    │ created_at      │    │ price           │  │
│ created_at      │    │ updated_at      │    └─────────────────┘  │
│ updated_at      │    └─────────────────┘                       │
└─────────────────┘                                              │
                                                                 │
┌─────────────────┐                                              │
│    Products     │◄─────────────────────────────────────────────┘
├─────────────────┤
│ id (PK)         │
│ name            │
│ description     │
│ price           │
│ stock_quantity  │
│ active          │
│ created_at      │
│ updated_at      │
└─────────────────┘
```

#### 물리적 최적화
```sql
-- 인덱스 전략
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status_created_at ON orders(status, created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- 파티셔닝 전략 (대용량 데이터 시)
CREATE TABLE orders_2024 PARTITION OF orders
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
```

### 2. 캐싱 전략

#### Redis 캐시 구조
```
Redis Cache Layer
├── User Cache
│   ├── user:1 → UserDto
│   ├── user:email:john@example.com → User ID
│   └── user:session:abc123 → Session Data
├── Product Cache
│   ├── product:1 → ProductDto
│   ├── products:category:electronics → Product List
│   └── products:hot → Hot Products
└── System Cache
    ├── config:settings → Application Settings
    └── stats:daily → Daily Statistics
```

#### 캐시 정책
```java
@Service
public class UserService {
    
    // 자주 조회되는 사용자 정보 캐시 (TTL: 1시간)
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public UserDto findById(Long id) {
        return userRepository.findById(id)
            .map(UserDto::from)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
    
    // 사용자 정보 변경 시 캐시 무효화
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        // 업데이트 로직
    }
}
```

---

## 🔐 보안 아키텍처

### 1. 인증/인가 구조

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │    │  API Gateway    │    │ Spring Security │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ JWT Token       │───▶│ Token           │───▶│ Authentication  │
│ Store           │    │ Validation      │    │ Filter Chain    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
                                              ┌─────────────────┐
                                              │ Authorization   │
                                              │ Manager         │
                                              └─────────────────┘
```

### 2. Spring Security 설정

#### JWT 기반 인증
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
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        
        return http.build();
    }
}
```

#### 메서드 수준 보안
```java
@Service
public class UserService {
    
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public UserDto findById(Long id) {
        // 관리자이거나 본인만 조회 가능
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> findAllUsers() {
        // 관리자만 전체 사용자 조회 가능
    }
}
```

### 3. 데이터 보안

#### 민감 정보 암호화
```java
@Entity
public class User {
    
    @Column(nullable = false)
    private String password;  // BCrypt 해싱
    
    @Column
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;  // AES 암호화
    
    @Column
    @Convert(converter = EncryptedStringConverter.class)
    private String ssn;  // 주민번호 AES 암호화
}
```

---

## ⚡ 성능 및 확장성

### 1. 성능 최적화 전략

#### 데이터베이스 최적화
```java
// N+1 문제 해결
@EntityGraph(attributePaths = {"orders", "profile"})
List<User> findAllWithOrdersAndProfile();

// 배치 처리
@Modifying
@Query("UPDATE User u SET u.lastLoginAt = :now WHERE u.id IN :ids")
int updateLastLoginBatch(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);

// 읽기 전용 트랜잭션
@Transactional(readOnly = true)
public Page<UserDto> findAllUsers(Pageable pageable) {
    // 읽기 전용으로 성능 향상
}
```

#### 응용 프로그램 최적화
```java
// 비동기 처리
@Async
public CompletableFuture<Void> sendWelcomeEmail(User user) {
    emailService.send(user.getEmail(), "Welcome!");
    return CompletableFuture.completedFuture(null);
}

// 캐싱 계층
@Cacheable(value = "products", key = "#category")
public List<ProductDto> findByCategory(String category) {
    return productRepository.findByCategory(category);
}
```

### 2. 확장성 설계

#### 수평 확장 준비
```yaml
# application.yml - 스테이트리스 설계
spring:
  session:
    store-type: redis  # 세션 외부 저장
  
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20  # 배치 처리 최적화
```

#### 마이크로서비스 전환 준비
```java
// 도메인 이벤트 패턴
@Entity
public class Order {
    
    @DomainEvents
    Collection<Object> domainEvents() {
        return List.of(new OrderCreatedEvent(this.id, this.userId));
    }
}

@EventListener
public void handleOrderCreated(OrderCreatedEvent event) {
    // 다른 도메인으로 이벤트 전파
    userService.updateOrderCount(event.getUserId());
}
```

---

## 🚀 배포 아키텍처

### 1. 환경별 구성

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Development   │    │    Staging      │    │   Production    │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ H2 Database     │    │ MySQL (Single)  │    │ MySQL (Master-  │
│ Embedded Redis  │    │ Redis (Single)  │    │ Slave)          │
│ Local Files     │    │ File Storage    │    │ Redis Cluster   │
│                 │    │                 │    │ CDN             │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 2. 컨테이너화

#### Dockerfile
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/spring-backend-*.jar app.jar

EXPOSE 3055

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Docker Compose
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "3055:3055"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: spring_backend
      MYSQL_ROOT_PASSWORD: root_password

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

---

## 📊 모니터링 및 로깅

### 1. 로깅 전략

#### 구조화된 로깅
```java
@Service
@Slf4j
public class UserService {
    
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating user: username={}, email={}", 
                request.username(), request.email());
        
        try {
            UserDto user = // 생성 로직
            
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

### 2. 메트릭 수집

#### Micrometer 활용
```java
@RestController
public class UserController {
    
    private final MeterRegistry meterRegistry;
    private final Counter userCreationCounter;
    
    public UserController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.userCreationCounter = Counter.builder("user.creation")
            .description("Number of users created")
            .register(meterRegistry);
    }
    
    @PostMapping
    public ApiResponse<UserDto> createUser(@RequestBody CreateUserRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            UserDto user = userService.createUser(request);
            userCreationCounter.increment();
            return ApiResponse.success("User created", user);
        } finally {
            sample.stop(Timer.builder("user.creation.time")
                .description("User creation time")
                .register(meterRegistry));
        }
    }
}
```

---

## 🔮 미래 확장 계획

### 1. 마이크로서비스 전환

#### 도메인 분리 전략
```
Current Monolith              Future Microservices
┌─────────────────┐          ┌─────────────────┐
│                 │          │  User Service   │
│   Spring Boot   │   ───▶   ├─────────────────┤
│   Application   │          │ Order Service   │
│                 │          ├─────────────────┤
│                 │          │Product Service  │
│                 │          ├─────────────────┤
└─────────────────┘          │ Payment Service │
                              └─────────────────┘
```

### 2. 이벤트 기반 아키텍처

#### 이벤트 스토밍 결과
```java
// 도메인 이벤트 정의
public record UserCreatedEvent(Long userId, String username, String email) {}
public record OrderPlacedEvent(Long orderId, Long userId, BigDecimal amount) {}
public record PaymentProcessedEvent(Long paymentId, Long orderId, PaymentStatus status) {}

// 이벤트 처리
@EventListener
@Async
public void handleUserCreated(UserCreatedEvent event) {
    // 웰컴 이메일 발송
    // 초기 포인트 적립
    // 분석 데이터 수집
}
```

---

**📝 이 아키텍처 문서는 실제 프로젝트 구조와 업계 모범 사례를 바탕으로 작성되었습니다.**  
**시스템 진화에 따라 지속적으로 업데이트됩니다.**