# 🌐 API 개발 가이드라인

> **RESTful API 설계 및 구현을 위한 종합 가이드**

## 📋 목차

1. [RESTful API 설계 원칙](#-restful-api-설계-원칙)
2. [URL 설계 규칙](#-url-설계-규칙)
3. [HTTP 메서드 사용 규칙](#-http-메서드-사용-규칙)
4. [상태 코드 활용](#-상태-코드-활용)
5. [요청/응답 형식](#-요청응답-형식)
6. [입력 검증 및 에러 처리](#-입력-검증-및-에러-처리)
7. [페이징 및 정렬](#-페이징-및-정렬)
8. [API 문서화](#-api-문서화)
9. [보안 고려사항](#-보안-고려사항)
10. [성능 최적화](#-성능-최적화)

---

## 🎯 RESTful API 설계 원칙

### 1. 리소스 중심 설계

#### ✅ 올바른 리소스 식별
```http
GET    /api/users           # 사용자 목록
GET    /api/users/123       # 특정 사용자
POST   /api/users           # 사용자 생성
PUT    /api/users/123       # 사용자 수정
DELETE /api/users/123       # 사용자 삭제
```

#### ❌ 잘못된 동사 중심 설계
```http
GET  /api/getUsers          # 동사 사용 금지
POST /api/createUser        # 동사 사용 금지
POST /api/deleteUser/123    # 동사 사용 금지
```

### 2. 계층적 리소스 구조

#### ✅ 중첩 리소스 표현
```http
GET    /api/users/123/orders           # 특정 사용자의 주문 목록
POST   /api/users/123/orders           # 특정 사용자의 주문 생성
GET    /api/users/123/orders/456       # 특정 사용자의 특정 주문
PUT    /api/users/123/orders/456       # 특정 사용자의 특정 주문 수정
DELETE /api/users/123/orders/456       # 특정 사용자의 특정 주문 삭제
```

---

## 🔗 URL 설계 규칙

### 1. 기본 URL 구조

```
{protocol}://{host}:{port}/{context-path}/{version}/{resource}
```

#### 실제 예시
```http
http://localhost:3055/api/v1/users
https://api.example.com/api/v1/users/123/orders
```

### 2. 네이밍 규칙

| 구분              | 규칙                       | 예시                            |
| ----------------- | -------------------------- | ------------------------------- |
| **리소스명**      | 복수형, 소문자, 케밥케이스 | `/users`, `/user-profiles`      |
| **경로 매개변수** | 소문자, 케밥케이스         | `/{user-id}`, `/{order-id}`     |
| **쿼리 매개변수** | 카멜케이스                 | `?sortBy=createdAt&pageSize=20` |

### 3. 표준 엔드포인트 패턴

#### 기본 CRUD 작업
```java
@RestController
@RequestMapping("/users")
@Tag(name = "사용자 API", description = "사용자 관리 API")
public class UserController {
    
    // 목록 조회 (페이징, 정렬, 필터링 지원)
    @GetMapping
    public ApiResponse<Page<UserDto>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sort,
        @RequestParam(required = false) String search) {
        // 구현 내용
    }
    
    // 단건 조회
    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(@PathVariable Long id) {
        // 구현 내용
    }
    
    // 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        // 구현 내용
    }
    
    // 전체 수정
    @PutMapping("/{id}")
    public ApiResponse<UserDto> updateUser(
        @PathVariable Long id, 
        @Valid @RequestBody UpdateUserRequest request) {
        // 구현 내용
    }
    
    // 부분 수정
    @PatchMapping("/{id}")
    public ApiResponse<UserDto> patchUser(
        @PathVariable Long id, 
        @Valid @RequestBody PatchUserRequest request) {
        // 구현 내용
    }
    
    // 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // 구현 내용
    }
}
```

---

## 🔧 HTTP 메서드 사용 규칙

### HTTP 메서드별 특성

| 메서드     | 용도       | 멱등성 | 안전성 | 요청 본문 | 응답 본문 |
| ---------- | ---------- | ------ | ------ | --------- | --------- |
| **GET**    | 조회       | ✅      | ✅      | ❌         | ✅         |
| **POST**   | 생성, 처리 | ❌      | ❌      | ✅         | ✅         |
| **PUT**    | 전체 수정  | ✅      | ❌      | ✅         | ✅         |
| **PATCH**  | 부분 수정  | ❌      | ❌      | ✅         | ✅         |
| **DELETE** | 삭제       | ✅      | ❌      | ❌         | ❌         |

### 특수한 경우의 HTTP 메서드 활용

#### 검색 API (복잡한 조건)
```java
// ✅ 복잡한 검색 조건은 POST 사용
@PostMapping("/search")
public ApiResponse<Page<UserDto>> searchUsers(@RequestBody UserSearchRequest request) {
    // 복잡한 검색 조건이나 민감한 데이터가 포함된 검색
}

// ✅ 간단한 검색은 GET 사용
@GetMapping("/search")
public ApiResponse<Page<UserDto>> searchUsers(
    @RequestParam String keyword,
    @RequestParam(required = false) String category) {
    // 간단한 키워드 검색
}
```

#### 배치 작업
```java
// 여러 항목 생성
@PostMapping("/batch")
public ApiResponse<List<UserDto>> createUsers(@RequestBody List<CreateUserRequest> requests) {
    // 구현 내용
}

// 여러 항목 수정
@PutMapping("/batch")
public ApiResponse<List<UserDto>> updateUsers(@RequestBody List<UpdateUserRequest> requests) {
    // 구현 내용
}
```

---

## 📊 상태 코드 활용

### 1. 표준 상태 코드 매핑

#### 2xx 성공
| 코드               | 설명            | 사용 시점            | 응답 본문 |
| ------------------ | --------------- | -------------------- | --------- |
| **200 OK**         | 요청 성공       | GET, PUT, PATCH 성공 | 있음      |
| **201 Created**    | 생성 성공       | POST 성공            | 있음      |
| **204 No Content** | 성공, 응답 없음 | DELETE 성공          | 없음      |

#### 4xx 클라이언트 오류
| 코드                         | 설명        | 사용 시점              | 응답 본문 |
| ---------------------------- | ----------- | ---------------------- | --------- |
| **400 Bad Request**          | 잘못된 요청 | 검증 실패, 잘못된 형식 | 있음      |
| **401 Unauthorized**         | 인증 필요   | 로그인 필요            | 있음      |
| **403 Forbidden**            | 권한 없음   | 접근 권한 없음         | 있음      |
| **404 Not Found**            | 리소스 없음 | 리소스를 찾을 수 없음  | 있음      |
| **409 Conflict**             | 충돌        | 중복 생성, 동시성 문제 | 있음      |
| **422 Unprocessable Entity** | 처리 불가   | 비즈니스 로직 위반     | 있음      |

#### 5xx 서버 오류
| 코드                          | 설명      | 사용 시점             | 응답 본문 |
| ----------------------------- | --------- | --------------------- | --------- |
| **500 Internal Server Error** | 서버 오류 | 예상치 못한 서버 오류 | 있음      |

### 2. 상태 코드 적용 예시

```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
        try {
            UserDto user = userService.findById(id);
            return ResponseEntity.ok(ApiResponse.success("사용자 조회 성공", user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("사용자를 찾을 수 없습니다"));
        }
    }
    
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("사용자 생성 성공", user));
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 📤 요청/응답 형식

### 1. 표준 응답 형식 (ApiResponse)

#### 성공 응답
```json
{
  "result": "SUCCESS",
  "message": "사용자 조회 성공",
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "createdAt": "2024-08-17T10:30:00"
  }
}
```

#### 오류 응답
```json
{
  "result": "ERROR",
  "message": "사용자를 찾을 수 없습니다",
  "data": null
}
```

#### 페이징 응답
```json
{
  "result": "SUCCESS",
  "message": "사용자 목록 조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "홍길동",
        "email": "hong@example.com"
      }
    ],
    "pageable": {
      "sort": {"empty": false, "sorted": true, "unsorted": false},
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 100,
    "totalPages": 5,
    "last": false,
    "first": true,
    "numberOfElements": 20
  }
}
```

### 2. 요청 DTO 설계

#### 생성 요청 DTO
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
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]", 
             message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다")
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

---

## ✅ 입력 검증 및 에러 처리

### 1. 입력 검증 규칙

#### Bean Validation 활용
```java
public record UpdateUserRequest(
    @NotBlank
    @Size(max = 50)
    String name,
    
    @Email
    String email,
    
    @Min(18) @Max(100)
    Integer age,
    
    @Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$")
    String phoneNumber
) {}
```

#### 커스텀 검증
```java
public record CreateUserRequest(
    @ValidUsername  // 커스텀 검증 어노테이션
    String username
) {}

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameValidator.class)
public @interface ValidUsername {
    String message() default "사용자명은 영문자와 숫자만 포함할 수 있습니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### 2. 에러 응답 표준화

#### 전역 예외 처리
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Validation 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        return ApiResponse.error("입력 데이터 검증 실패");
    }
    
    // 비즈니스 로직 에러
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleUserNotFoundException(UserNotFoundException e) {
        return ApiResponse.error(e.getMessage());
    }
    
    // 일반적인 서버 에러
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ApiResponse.error("서버 내부 오류가 발생했습니다");
    }
}
```

---

## 📄 페이징 및 정렬

### 1. 페이징 파라미터

#### 표준 페이징 파라미터
```java
@GetMapping("/users")
public ApiResponse<Page<UserDto>> getUsers(
    @RequestParam(defaultValue = "0") 
    @Min(0) @Schema(description = "페이지 번호 (0부터 시작)", example = "0") 
    int page,
    
    @RequestParam(defaultValue = "20") 
    @Min(1) @Max(100) @Schema(description = "페이지 크기", example = "20") 
    int size,
    
    @RequestParam(defaultValue = "id") 
    @Schema(description = "정렬 기준 필드", example = "createdAt") 
    String sort,
    
    @RequestParam(defaultValue = "asc") 
    @Schema(description = "정렬 방향", example = "desc", allowableValues = {"asc", "desc"}) 
    String direction
) {
    Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
    Pageable pageable = PageRequest.of(page, size, sortOrder);
    
    Page<UserDto> users = userService.findAll(pageable);
    return ApiResponse.success("사용자 목록 조회 성공", users);
}
```

### 2. 복합 정렬

```java
// 여러 필드로 정렬
@GetMapping("/users")
public ApiResponse<Page<UserDto>> getUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(required = false) List<String> sort
) {
    Sort sortOrder = Sort.unsorted();
    
    if (sort != null && !sort.isEmpty()) {
        List<Sort.Order> orders = sort.stream()
            .map(s -> {
                String[] parts = s.split(",");
                String property = parts[0];
                Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
                return new Sort.Order(direction, property);
            })
            .collect(Collectors.toList());
        sortOrder = Sort.by(orders);
    }
    
    Pageable pageable = PageRequest.of(page, size, sortOrder);
    // ...
}
```

### 3. 커서 기반 페이징

```java
// 대용량 데이터용 커서 페이징
@GetMapping("/users/cursor")
public ApiResponse<CursorPage<UserDto>> getUsersWithCursor(
    @RequestParam(required = false) Long cursor,
    @RequestParam(defaultValue = "20") int size
) {
    CursorPage<UserDto> users = userService.findAllWithCursor(cursor, size);
    return ApiResponse.success("사용자 목록 조회 성공", users);
}
```

---

## 📚 API 문서화

### 1. OpenAPI 어노테이션

#### 컨트롤러 수준 문서화
```java
@Tag(
    name = "사용자 관리 API", 
    description = """
        사용자 관리 관련 API입니다.
        
        주요 기능:
        - 사용자 조회, 생성, 수정, 삭제
        - 사용자 검색 및 페이징
        - 사용자 프로필 관리
        """
)
@RestController
@RequestMapping("/users")
public class UserController {
    // ...
}
```

#### 메서드 수준 문서화
```java
@Operation(
    summary = "사용자 정보 조회",
    description = """
        사용자 ID를 통해 특정 사용자의 상세 정보를 조회합니다.
        
        조회 가능한 정보:
        - 기본 프로필 정보 (이름, 이메일)
        - 계정 생성/수정 시간
        - 활성화 상태
        """,
    responses = {
        @ApiResponse(
            responseCode = "200", 
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "사용자를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    }
)
@GetMapping("/{id}")
public ApiResponse<UserDto> getUser(
    @Parameter(description = "사용자 고유 식별자", example = "1", required = true)
    @PathVariable Long id
) {
    // 구현 내용
}
```

### 2. 예시 응답 정의

```java
@Schema(
    name = "UserDto",
    description = "사용자 정보 응답 DTO",
    example = """
        {
          "id": 1,
          "name": "홍길동",
          "email": "hong@example.com",
          "active": true,
          "createdAt": "2024-08-17T10:30:00",
          "updatedAt": "2024-08-17T15:45:00"
        }
        """
)
public record UserDto(
    @Schema(description = "사용자 ID", example = "1")
    Long id,
    
    @Schema(description = "사용자명", example = "홍길동")
    String name,
    
    @Schema(description = "이메일", example = "hong@example.com")
    String email
) {}
```

---

## 🔐 보안 고려사항

### 1. 인증 및 인가

#### JWT 기반 인증
```java
@PostMapping("/auth/login")
public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse authResponse = authService.login(request);
    return ApiResponse.success("로그인 성공", authResponse);
}

@GetMapping("/users/me")
@PreAuthorize("hasRole('USER')")
public ApiResponse<UserDto> getCurrentUser(Authentication authentication) {
    UserDto user = userService.findByUsername(authentication.getName());
    return ApiResponse.success("현재 사용자 정보 조회 성공", user);
}
```

### 2. 입력 데이터 보안

#### SQL Injection 방지
```java
// ✅ JPA Repository 사용 (자동 파라미터 바인딩)
@Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
List<User> findByNameContaining(@Param("name") String name);

// ❌ 직접 쿼리 문자열 연결 금지
@Query("SELECT u FROM User u WHERE u.name LIKE '%" + name + "%'")  // 위험!
```

#### XSS 방지
```java
// HTML 이스케이프 처리
public record CreateUserRequest(
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s]*$", message = "특수 문자는 사용할 수 없습니다")
    String name
) {}
```

### 3. 민감 정보 보호

#### 응답에서 민감 정보 제외
```java
public record UserDto(
    Long id,
    String name,
    String email,
    // password는 절대 포함하지 않음
    LocalDateTime createdAt
) {
    public static UserDto from(User user) {
        return new UserDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt()
        );
    }
}
```

---

## ⚡ 성능 최적화

### 1. N+1 문제 해결

#### @EntityGraph 사용
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @EntityGraph(attributePaths = {"orders", "profile"})
    Optional<User> findWithOrdersAndProfileById(Long id);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findWithOrdersById(@Param("id") Long id);
}
```

### 2. 캐싱 전략

#### Redis 캐싱 적용
```java
@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#id")
    public UserDto findById(Long id) {
        // 데이터베이스 조회
    }
    
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        // 수정 후 캐시 무효화
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public void clearAllUsersCache() {
        // 전체 사용자 캐시 무효화
    }
}
```

### 3. 대용량 데이터 처리

#### Slice를 통한 효율적 페이징
```java
// count 쿼리 없이 다음 페이지 존재 여부만 확인
@GetMapping("/users/slice")
public ApiResponse<Slice<UserDto>> getUsersSlice(Pageable pageable) {
    Slice<UserDto> users = userService.findAllSlice(pageable);
    return ApiResponse.success("사용자 목록 조회 성공", users);
}
```

---

## 📋 API 개발 체크리스트

### 설계 단계
- [ ] RESTful 원칙에 따른 URL 설계
- [ ] 적절한 HTTP 메서드 선택
- [ ] 표준 상태 코드 매핑
- [ ] 요청/응답 DTO 설계

### 구현 단계
- [ ] ApiResponse로 응답 래핑
- [ ] 입력 검증 추가 (@Valid, Bean Validation)
- [ ] 예외 처리 구현
- [ ] 페이징 및 정렬 지원 (필요 시)

### 문서화 단계
- [ ] @Tag, @Operation 어노테이션 추가
- [ ] @Schema로 DTO 문서화
- [ ] 예시 요청/응답 작성
- [ ] Swagger UI에서 동작 확인

### 보안 단계
- [ ] 인증/인가 처리
- [ ] 입력 데이터 검증 및 이스케이프
- [ ] 민감 정보 노출 방지
- [ ] SQL Injection 방지

### 성능 단계
- [ ] N+1 문제 검토
- [ ] 캐싱 전략 적용 (필요 시)
- [ ] 대용량 데이터 처리 최적화

---

**📝 이 API 가이드라인은 실제 프로젝트의 요구사항과 성능 특성을 고려하여 작성되었습니다.**  
**새로운 API 패턴이나 개선 사항이 있다면 팀 논의를 통해 문서를 업데이트해 주세요.**