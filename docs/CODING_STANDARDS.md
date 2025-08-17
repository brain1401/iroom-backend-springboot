# 💻 코딩 표준 가이드

> **Spring Boot 3.5.4 + Java 21 기반 프로젝트의 코딩 컨벤션 및 스타일 가이드**

## 📋 목차

1. [패키지 구조 규칙](#-패키지-구조-규칙)
2. [Java 21 활용 규칙](#-java-21-활용-규칙)
3. [의존성 주입 패턴](#-의존성-주입-패턴)
4. [문서화 규칙](#-문서화-규칙)
5. [Entity 설계 규칙](#-entity-설계-규칙)
6. [응답 표준화](#-응답-표준화)
7. [예외 처리](#-예외-처리)
8. [테스트 작성 규칙](#-테스트-작성-규칙)

---

## 🏗️ 패키지 구조 규칙

### Domain-Driven Package 구조

#### ✅ 올바른 구조
```
com.iroomclass.spring_backend/
├── domain/                    # 도메인별 패키지
│   ├── user/                 # 사용자 도메인
│   │   ├── controller/       # REST API 컨트롤러
│   │   ├── service/          # 비즈니스 로직
│   │   ├── repository/       # 데이터 접근 계층
│   │   ├── entity/           # JPA 엔티티
│   │   └── dto/              # 데이터 전송 객체
│   ├── order/                # 주문 도메인
│   └── product/              # 상품 도메인
├── common/                   # 공통 유틸리티
│   ├── ApiResponse.java      # 표준 응답 래퍼
│   ├── ResultStatus.java     # 응답 상태
│   └── exception/            # 공통 예외 처리
└── config/                   # 설정 클래스
    ├── SecurityConfig.java
    └── OpenApiConfig.java
```

#### ❌ 잘못된 구조 (Layer-First)
```
com.iroomclass.spring_backend/
├── controller/               # 계층별 분리 (금지)
├── service/
├── repository/
└── dto/
```

### 패키지 명명 규칙

| 구분 | 규칙 | 예시 |
|------|------|------|
| **도메인 패키지** | 소문자, 단수형 | `user`, `order`, `product` |
| **계층 패키지** | 계층명 복수형 | `controllers`, `services`, `repositories` |
| **DTO 패키지** | `dto` 고정 | `dto` |

---

## ☕ Java 21 활용 규칙

### 1. Record 사용 규칙

#### ✅ DTO는 반드시 Record 사용
```java
/**
 * 사용자 응답 DTO
 */
public record UserDto(
    @Schema(description = "사용자 ID", example = "1") 
    Long id,
    
    @Schema(description = "사용자명", example = "홍길동") 
    String name,
    
    @Schema(description = "이메일", example = "hong@example.com") 
    String email) {
    
    // Compact Constructor로 Validation
    public UserDto {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(email, "email must not be null");
    }
}
```

#### ✅ 정적 팩토리 메서드 제공
```java
/**
 * 시스템 헬스체크 응답 DTO
 */
public record SystemHealthDto(
    @Schema(description = "상태") String status,
    @Schema(description = "타임스탬프") LocalDateTime timestamp,
    @Schema(description = "메시지") String message) {
    
    /**
     * 정상 상태 헬스체크 응답 생성
     */
    public static SystemHealthDto up(String message) {
        return new SystemHealthDto("UP", LocalDateTime.now(), message);
    }
    
    /**
     * 오류 상태 헬스체크 응답 생성
     */
    public static SystemHealthDto down(String message) {
        return new SystemHealthDto("DOWN", LocalDateTime.now(), message);
    }
}
```

#### ❌ DTO에 일반 클래스 사용 금지
```java
// 잘못된 방식
public class UserDto {
    private Long id;
    private String name;
    private String email;
    
    // getter, setter, equals, hashCode, toString...
}
```

### 2. Pattern Matching 활용

#### ✅ Switch Expression 사용
```java
public String getStatusMessage(ResultStatus status) {
    return switch (status) {
        case SUCCESS -> "처리 성공";
        case ERROR -> "처리 실패";
    };
}
```

### 3. Text Blocks 활용

#### ✅ 긴 문자열은 Text Blocks 사용
```java
private static final String COMPLEX_QUERY = """
    SELECT u.id, u.name, u.email
    FROM users u
    JOIN orders o ON u.id = o.user_id
    WHERE u.active = true
    AND o.created_at >= :startDate
    """;
```

---

## 🔧 의존성 주입 패턴

### 생성자 주입 + @RequiredArgsConstructor

#### ✅ 권장: 생성자 주입
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor  // Lombok으로 생성자 생성
@Tag(name = "사용자 API", description = "사용자 관리 API")
public class UserController {
    
    // final로 불변성 보장
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(@PathVariable Long id) {
        UserDto user = userService.findById(id);
        return ApiResponse.success("사용자 조회 성공", user);
    }
}
```

#### ✅ Service 계층도 동일한 패턴
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본적으로 읽기 전용
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional  // 쓰기 작업시에만 명시
    public UserDto createUser(CreateUserRequest request) {
        // 구현 내용
    }
}
```

#### ❌ 금지: 필드 주입
```java
// 잘못된 방식
@Autowired
private UserService userService;  // 필드 주입 금지

@Autowired
private UserRepository userRepository;  // 필드 주입 금지
```

---

## 📚 문서화 규칙

### 1. JavaDoc 작성 규칙

#### 한국어 명사형 표현 사용
```java
/**
 * 사용자 정보 조회
 * 
 * @param id 사용자 고유 식별자
 * @return 사용자 DTO
 * @throws UserNotFoundException 사용자가 존재하지 않을 때
 */
@Transactional(readOnly = true)
public UserDto findById(Long id) {
    return userRepository.findById(id)
        .map(this::convertToDto)
        .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + id));
}
```

#### 클래스 수준 JavaDoc
```java
/**
 * 사용자 관리 서비스
 * 
 * <p>사용자의 생성, 조회, 수정, 삭제 기능을 제공합니다.
 * 모든 비즈니스 로직과 검증 규칙이 포함되어 있습니다.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {
    // 구현 내용
}
```

### 2. OpenAPI 문서화

#### Controller 문서화
```java
@Tag(name = "사용자 API", description = "사용자 관리 관련 API")
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Operation(
        summary = "사용자 정보 조회",
        description = "사용자 ID로 사용자 정보를 조회합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
        }
    )
    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(
        @Parameter(description = "사용자 ID", example = "1") 
        @PathVariable Long id) {
        // 구현 내용
    }
}
```

#### DTO 문서화
```java
public record CreateUserRequest(
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    @Schema(description = "사용자명", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED) 
    String name,
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "이메일", example = "hong@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    String email
) {
    // Compact constructor validation
}
```

---

## 🗃️ Entity 설계 규칙

### 1. 기본 Entity 패턴

#### ✅ 권장: Entity 설계
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
}
```

#### ❌ 금지: @Data 사용
```java
// 잘못된 방식
@Entity
@Data  // 금지: equals/hashCode/toString 순환 참조 위험
public class User {
    // 구현 내용
}
```

### 2. 연관관계 설정

#### ✅ 기본적으로 LAZY 로딩
```java
@Entity
public class Order {
    
    @ManyToOne(fetch = FetchType.LAZY)  // 기본값이지만 명시적 표현
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // 연관관계 편의 메서드
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
```

---

## 📤 응답 표준화

### 1. ApiResponse 표준 사용

#### ✅ 모든 API는 ApiResponse로 래핑
```java
// 성공 응답 (데이터 포함)
@GetMapping("/{id}")
public ApiResponse<UserDto> getUser(@PathVariable Long id) {
    UserDto user = userService.findById(id);
    return ApiResponse.success("사용자 조회 성공", user);
}

// 성공 응답 (데이터 없음)
@DeleteMapping("/{id}")
public ApiResponse<Void> deleteUser(@PathVariable Long id) {
    userService.deleteById(id);
    return ApiResponse.success("사용자 삭제 성공");
}

// 페이징 응답
@GetMapping
public ApiResponse<Page<UserDto>> getUsers(Pageable pageable) {
    Page<UserDto> users = userService.findAll(pageable);
    return ApiResponse.success("사용자 목록 조회 성공", users);
}
```

### 2. HTTP 상태 코드 사용

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)  // 201 Created
public ApiResponse<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
    UserDto user = userService.createUser(request);
    return ApiResponse.success("사용자 생성 성공", user);
}
```

---

## ⚠️ 예외 처리

### 1. 커스텀 예외 정의

```java
/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 2. 전역 예외 처리

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleUserNotFoundException(UserNotFoundException e) {
        return ApiResponse.error(e.getMessage());
    }
    
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(ValidationException e) {
        return ApiResponse.error("입력 데이터가 올바르지 않습니다: " + e.getMessage());
    }
}
```

---

## 🧪 테스트 작성 규칙

### 1. 단위 테스트

#### Service 계층 테스트
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("사용자 ID로 조회 - 성공")
    void findById_Success() {
        // Given
        Long userId = 1L;
        User user = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When
        UserDto result = userService.findById(userId);
        
        // Then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository).findById(userId);
    }
    
    @Test
    @DisplayName("사용자 ID로 조회 - 사용자 없음")
    void findById_UserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userService.findById(userId))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("사용자를 찾을 수 없습니다: " + userId);
    }
}
```

### 2. 통합 테스트

#### Controller 계층 테스트
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(OrderAnnotation.class)
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @Order(1)
    @DisplayName("사용자 생성 - 통합 테스트")
    void createUser_IntegrationTest() {
        // Given
        CreateUserRequest request = new CreateUserRequest("홍길동", "hong@example.com");
        
        // When
        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.postForEntity(
            "/api/users", 
            request, 
            new ParameterizedTypeReference<ApiResponse<UserDto>>() {}
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().result()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(response.getBody().data().name()).isEqualTo("홍길동");
    }
}
```

---

## 🔍 코드 품질 체크리스트

### 개발 완료 시 확인사항

#### ✅ 패키지 구조
- [ ] Domain-Driven 패키지 구조 사용
- [ ] 적절한 계층 분리 (Controller-Service-Repository)
- [ ] 공통 기능은 common 패키지에 위치

#### ✅ Java 21 활용
- [ ] DTO는 Record로 구현
- [ ] Compact Constructor에서 Validation
- [ ] Switch Expression 활용 (해당하는 경우)
- [ ] Text Blocks 활용 (긴 문자열의 경우)

#### ✅ 의존성 주입
- [ ] @RequiredArgsConstructor 사용
- [ ] final 키워드로 불변성 보장
- [ ] 필드 주입 사용하지 않음

#### ✅ 문서화
- [ ] 모든 public 메서드에 한국어 JavaDoc 작성
- [ ] @Schema 어노테이션으로 API 문서화
- [ ] @Tag, @Operation 어노테이션 추가

#### ✅ 응답 표준화
- [ ] 모든 API는 ApiResponse로 래핑
- [ ] 적절한 HTTP 상태 코드 사용
- [ ] 일관된 메시지 형식 사용

#### ✅ 테스트
- [ ] 단위 테스트 작성 (Service 계층)
- [ ] 통합 테스트 작성 (Controller 계층)
- [ ] @DisplayName으로 테스트 의도 명확히 표현

---

**📝 이 코딩 표준은 실제 프로젝트 구조를 분석하여 작성되었습니다.**  
**새로운 패턴이나 개선 사항이 있다면 팀 논의를 통해 문서를 업데이트해 주세요.**