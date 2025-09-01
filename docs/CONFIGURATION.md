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

#### Database Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `DB_URL` | Database JDBC connection URL | Current project DB | No |
| `DB_USERNAME` | Database username | Current project user | No |
| `DB_PASSWORD` | Database password | None | **Yes** |

#### AI Server Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AI_SERVER_URL` | AI 서버 기본 URL | http://localhost:8000 | No |

#### AWS S3 Configuration (for Print PDF Storage)
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AWS_S3_REGION` | AWS S3 리전 | ap-northeast-2 | No |
| `AWS_S3_BUCKET_NAME` | S3 버킷명 (PDF 저장용) | None | **Yes** |
| `AWS_S3_PRESIGNED_URL_DURATION` | 프리사인드 URL 유효시간 (분) | 60 | No |
| `AWS_S3_MULTIPART_MIN_PART_SIZE` | 멀티파트 업로드 최소 크기 (MB) | 8 | No |

#### AWS Credentials Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AWS_ACCESS_KEY_ID` | AWS 액세스 키 ID | None | **Yes*** |
| `AWS_SECRET_ACCESS_KEY` | AWS 시크릿 액세스 키 | None | **Yes*** |
| `AWS_PROFILE` | AWS CLI 프로파일명 | None | No |

*\* AWS 자격 증명은 다음 중 하나의 방법으로 제공되어야 합니다: 환경 변수, AWS CLI 프로파일, 또는 IAM 역할*|### Available Environment Variables

#### Database Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `DB_URL` | Database JDBC connection URL | Current project DB | No |
| `DB_USERNAME` | Database username | Current project user | No |
| `DB_PASSWORD` | Database password | None | **Yes** |

#### AI Server Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AI_SERVER_URL` | AI 서버 기본 URL | http://localhost:8000 | No |

#### AWS S3 Configuration (for Print PDF Storage)
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AWS_S3_REGION` | AWS S3 리전 | ap-northeast-2 | No |
| `AWS_S3_BUCKET_NAME` | S3 버킷명 (PDF 저장용) | None | **Yes** |
| `AWS_S3_PRESIGNED_URL_DURATION` | 프리사인드 URL 유효시간 (분) | 60 | No |
| `AWS_S3_MULTIPART_MIN_PART_SIZE` | 멀티파트 업로드 최소 크기 (MB) | 8 | No |

#### AWS Credentials Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AWS_ACCESS_KEY_ID` | AWS 액세스 키 ID | None | **Yes*** |
| `AWS_SECRET_ACCESS_KEY` | AWS 시크릿 액세스 키 | None | **Yes*** |
| `AWS_PROFILE` | AWS CLI 프로파일명 | None | No |

*\* AWS 자격 증명은 다음 중 하나의 방법으로 제공되어야 합니다: 환경 변수, AWS CLI 프로파일, 또는 IAM 역할*|### Available Environment Variables

#### Database Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `DB_URL` | Database JDBC connection URL | Current project DB | No |
| `DB_USERNAME` | Database username | Current project user | No |
| `DB_PASSWORD` | Database password | None | **Yes** |

#### AI Server Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AI_SERVER_URL` | AI 서버 기본 URL | http://localhost:8000 | No |

#### AWS S3 Configuration (for Print PDF Storage)
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AWS_S3_REGION` | AWS S3 리전 | ap-northeast-2 | No |
| `AWS_S3_BUCKET_NAME` | S3 버킷명 (PDF 저장용) | None | **Yes** |
| `AWS_S3_PRESIGNED_URL_DURATION` | 프리사인드 URL 유효시간 (분) | 60 | No |
| `AWS_S3_MULTIPART_MIN_PART_SIZE` | 멀티파트 업로드 최소 크기 (MB) | 8 | No |

#### AWS Credentials Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AWS_ACCESS_KEY_ID` | AWS 액세스 키 ID | None | **Yes*** |
| `AWS_SECRET_ACCESS_KEY` | AWS 시크릿 액세스 키 | None | **Yes*** |
| `AWS_PROFILE` | AWS CLI 프로파일명 | None | No |

*\* AWS 자격 증명은 다음 중 하나의 방법으로 제공되어야 합니다: 환경 변수, AWS CLI 프로파일, 또는 IAM 역할*--|### Available Environment Variables

#### Database Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `DB_URL` | Database JDBC connection URL | Current project DB | No |
| `DB_USERNAME` | Database username | Current project user | No |
| `DB_PASSWORD` | Database password | None | **Yes** |

#### AI Server Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AI_SERVER_URL` | AI 서버 기본 URL | http://localhost:8000 | No |

#### AWS S3 Configuration (for Print PDF Storage)
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AWS_S3_REGION` | AWS S3 리전 | ap-northeast-2 | No |
| `AWS_S3_BUCKET_NAME` | S3 버킷명 (PDF 저장용) | None | **Yes** |
| `AWS_S3_PRESIGNED_URL_DURATION` | 프리사인드 URL 유효시간 (분) | 60 | No |
| `AWS_S3_MULTIPART_MIN_PART_SIZE` | 멀티파트 업로드 최소 크기 (MB) | 8 | No |

#### AWS Credentials Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AWS_ACCESS_KEY_ID` | AWS 액세스 키 ID | None | **Yes*** |
| `AWS_SECRET_ACCESS_KEY` | AWS 시크릿 액세스 키 | None | **Yes*** |
| `AWS_PROFILE` | AWS CLI 프로파일명 | None | No |

*\* AWS 자격 증명은 다음 중 하나의 방법으로 제공되어야 합니다: 환경 변수, AWS CLI 프로파일, 또는 IAM 역할*|### Available Environment Variables

#### Database Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `DB_URL` | Database JDBC connection URL | Current project DB | No |
| `DB_USERNAME` | Database username | Current project user | No |
| `DB_PASSWORD` | Database password | None | **Yes** |

#### AI Server Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AI_SERVER_URL` | AI 서버 기본 URL | http://localhost:8000 | No |

#### AWS S3 Configuration (for Print PDF Storage)
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AWS_S3_REGION` | AWS S3 리전 | ap-northeast-2 | No |
| `AWS_S3_BUCKET_NAME` | S3 버킷명 (PDF 저장용) | None | **Yes** |
| `AWS_S3_PRESIGNED_URL_DURATION` | 프리사인드 URL 유효시간 (분) | 60 | No |
| `AWS_S3_MULTIPART_MIN_PART_SIZE` | 멀티파트 업로드 최소 크기 (MB) | 8 | No |

#### AWS Credentials Configuration
| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `AWS_ACCESS_KEY_ID` | AWS 액세스 키 ID | None | **Yes*** |
| `AWS_SECRET_ACCESS_KEY` | AWS 시크릿 액세스 키 | None | **Yes*** |
| `AWS_PROFILE` | AWS CLI 프로파일명 | None | No |

*\* AWS 자격 증명은 다음 중 하나의 방법으로 제공되어야 합니다: 환경 변수, AWS CLI 프로파일, 또는 IAM 역할*

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

## AWS S3 Setup (for Print PDF Storage)

The application uses AWS S3 for storing generated PDF files from the print functionality.

### Prerequisites

1. **AWS Account**: You need an active AWS account
2. **S3 Bucket**: Create a dedicated S3 bucket for PDF storage
3. **AWS Credentials**: Configure AWS credentials using one of the methods below

### S3 Bucket Creation

1. Log in to AWS Console and navigate to S3 service
2. Create a new bucket with the following settings:
   - **Bucket name**: Choose a unique name (e.g., `your-app-print-pdfs`)
   - **Region**: `ap-northeast-2` (Seoul) or your preferred region
   - **Block public access**: Keep all public access blocked for security
   - **Versioning**: Optional (can be enabled for backup purposes)

### AWS Credentials Configuration

Choose one of the following methods to configure AWS credentials:

#### Method 1: Environment Variables (Development)
```bash
# Add to your .env file
AWS_ACCESS_KEY_ID=your-access-key-id
AWS_SECRET_ACCESS_KEY=your-secret-access-key
AWS_S3_BUCKET_NAME=your-s3-bucket-name
```

#### Method 2: AWS CLI Profile (Recommended)
```bash
# Configure AWS CLI
aws configure --profile your-profile-name
# Enter your access key, secret key, and region

# Set profile in environment
export AWS_PROFILE=your-profile-name
# Or add to .env file
AWS_PROFILE=your-profile-name
```

#### Method 3: IAM Role (Production/EC2/ECS)
When running on AWS infrastructure, attach an IAM role with the following permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:PutObject",
                "s3:DeleteObject",
                "s3:HeadObject"
            ],
            "Resource": "arn:aws:s3:::your-bucket-name/*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:ListBucket"
            ],
            "Resource": "arn:aws:s3:::your-bucket-name"
        }
    ]
}
```

### S3 Configuration in Application

The application automatically configures S3 using the environment variables:

```yaml
# application.yml
aws:
  s3:
    region: ${AWS_S3_REGION:ap-northeast-2}
    bucket-name: ${AWS_S3_BUCKET_NAME:}  # Required
    presigned-url-duration: ${AWS_S3_PRESIGNED_URL_DURATION:60}
    multipart-min-part-size: ${AWS_S3_MULTIPART_MIN_PART_SIZE:8}
```

### Testing S3 Setup

You can test the S3 integration by:

1. **Starting the application** with proper S3 configuration
2. **Making a print request** via the API:
   ```bash
   curl -X POST http://localhost:3055/api/print \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Test Document",
       "studentData": [
         {
           "studentName": "테스트 학생",
           "score": 85,
           "answers": ["A", "B", "C"]
         }
       ]
     }'
   ```

3. **Check S3 bucket** for the uploaded PDF file
4. **Download the PDF** using the returned printJobId:
   ```bash
   curl -X GET http://localhost:3055/api/print/download/{printJobId}
   ```

### Troubleshooting S3 Issues

#### Common Issues and Solutions:

**1. Access Denied Error**
```
software.amazon.awssdk.services.s3.model.S3Exception: Access Denied
```
- Check AWS credentials are correctly configured
- Verify IAM permissions for the bucket
- Ensure bucket name is correct

**2. Bucket Not Found Error**
```
software.amazon.awssdk.services.s3.model.NoSuchBucketException
```
- Verify the bucket name in environment variables
- Check the bucket exists in the specified region

**3. Region Mismatch**
```
software.amazon.awssdk.services.s3.model.S3Exception: The bucket is in this region: us-east-1
```
- Update `AWS_S3_REGION` to match your bucket's region

**4. Connection Timeout**
- Check network connectivity to AWS
- Verify AWS region is accessible from your location

### S3 File Structure

The application organizes PDF files in S3 with the following structure:

```
your-bucket-name/
└── print-pdfs/
    ├── print-job-1234567890.pdf
    ├── print-job-1234567891.pdf
    └── print-job-1234567892.pdf
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