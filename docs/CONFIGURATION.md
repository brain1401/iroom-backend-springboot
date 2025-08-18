# Configuration Guide

## Application Configuration

The application uses `application.yml` for configuration. Environment-specific configurations should be added as:
- `application-dev.yml` for development  
- `application-prod.yml` for production

Run with specific profile: `./gradlew bootRun --args='--spring.profiles.active=dev'`

## Database Setup

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

## Redis Setup

Redis is required for caching. Configure in `application.yml`:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

## Environment-Specific Configuration

### Development Environment
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

### Production Environment
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