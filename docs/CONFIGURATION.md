# Configuration Guide

## Application Configuration

The application uses `application.yml` for configuration. Environment-specific configurations should be added as:
- `application-dev.yml` for development  
- `application-prod.yml` for production

Run with specific profile: `./gradlew bootRun --args='--spring.profiles.active=dev'`

## Environment Variables

The application now uses environment variables for sensitive configuration like database credentials. This provides better security and flexibility across different environments.

### Setup Environment Variables

1. Copy the example environment file:
```bash
cp .env.example .env
```

2. Edit `.env` with your actual values:
```bash
# Database configuration
DB_URL=jdbc:mysql://localhost:3306/your_database_name
DB_USERNAME=your_username
DB_PASSWORD=your_secure_password
```

3. The `.env` file is automatically ignored by git for security.

### Available Environment Variables

| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `DB_URL` | Database JDBC connection URL | Current project DB | No |
| `DB_USERNAME` | Database username | Current project user | No |
| `DB_PASSWORD` | Database password | None | **Yes** |

## Database Setup

Before running the application:

1. Ensure MySQL is running and create a database
2. Set up environment variables as described above, or use defaults
3. The application.yml now uses environment variables with fallback defaults:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://project-db-campus.smhrd.com:3307/campus_25SW_FS_p3_4}
    username: ${DB_USERNAME:campus_25SW_FS_p3_4}
    password: ${DB_PASSWORD:}  # No default - must be set via environment
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

For production, always use environment variables instead of hardcoded values:

```bash
# Set environment variables in production
export DB_URL=jdbc:mysql://prod-db:3306/spring_backend_prod
export DB_USERNAME=prod_user
export DB_PASSWORD=your_secure_production_password
export SPRING_PROFILES_ACTIVE=prod
```

```yaml
# application-prod.yml
spring:
  datasource:
    # These now use environment variables from main application.yml
    # No need to override here as they're already externalized
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
logging:
  level:
    "[com.iroomclass.spring_backend]": INFO
    root: WARN
```

### Running with Environment Variables

```bash
# Method 1: Using .env file (recommended for development)
cp .env.example .env
# Edit .env with your values
./gradlew bootRun

# Method 2: Setting variables inline (for CI/CD or production)
DB_PASSWORD=your_password ./gradlew bootRun

# Method 3: Export variables in shell
export DB_PASSWORD=your_password
./gradlew bootRun
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