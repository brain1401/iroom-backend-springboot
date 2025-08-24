#!/bin/bash

# =============================================================================
# CodeDeploy Hook: AfterInstall
# =============================================================================
# 파일 설치 후 실행되는 스크립트
# - 환경 설정
# - 설정 파일 구성
# - 권한 설정
# =============================================================================

set -e

LOG_FILE="/opt/logs/codedeploy-after-install.log"

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "⚙️ CodeDeploy AfterInstall 단계 시작"

# 작업 디렉토리 이동
cd /opt/applications/iroom-backend

# 배포 정보 읽기
if [ -f "deployment-manifest.json" ]; then
    IMAGE_URI=$(jq -r '.imageUri' deployment-manifest.json)
    IMAGE_TAG=$(jq -r '.imageTag' deployment-manifest.json)
    COMMIT_SHA=$(jq -r '.commitSha' deployment-manifest.json)
    
    log "배포 정보:"
    log "  • 이미지 URI: $IMAGE_URI"
    log "  • 이미지 태그: $IMAGE_TAG"
    log "  • 커밋 SHA: $COMMIT_SHA"
else
    log "❌ deployment-manifest.json 파일을 찾을 수 없습니다"
    exit 1
fi

# 환경 변수 설정 파일 생성
log "환경 변수 설정 파일 생성..."
cat > /opt/applications/iroom-backend/.env << EOF
# Application Environment Variables
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=3055

# Database Configuration
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_NAME=${DB_NAME:-iroom_backend_db}
DB_USERNAME=${DB_USERNAME:-iroom_user}
DB_PASSWORD=${DB_PASSWORD:-iroom_password}

# Redis Configuration
REDIS_HOST=${REDIS_HOST:-localhost}
REDIS_PORT=${REDIS_PORT:-6379}
REDIS_PASSWORD=${REDIS_PASSWORD:-redis_password}

# JVM Options
JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication

# Deployment Information
DEPLOYED_AT=$(date -Iseconds)
DEPLOYED_BY=codedeploy
COMMIT_SHA=$COMMIT_SHA
IMAGE_TAG=$IMAGE_TAG
EOF

# 스크립트 권한 설정
log "스크립트 권한 설정..."
chmod +x /opt/scripts/*.sh
chmod +x scripts/codedeploy/*.sh

# 로그 디렉토리 권한 설정
chown -R ubuntu:ubuntu /opt/logs
chmod -R 755 /opt/logs

# Docker 이미지 풀링
log "Docker 이미지 풀링 시작..."
docker pull "$IMAGE_URI"

# 이미지 태깅
log "Docker 이미지 태깅..."
docker tag "$IMAGE_URI" "iroom/spring-backend:latest"
docker tag "$IMAGE_URI" "iroom/spring-backend:$IMAGE_TAG"

# 배포 상태 파일 생성
log "배포 상태 파일 생성..."
cat > /opt/applications/iroom-backend/deployment-status.json << EOF
{
    "status": "installed",
    "imageUri": "$IMAGE_URI",
    "imageTag": "$IMAGE_TAG",
    "commitSha": "$COMMIT_SHA",
    "installedAt": "$(date -Iseconds)",
    "deploymentId": "$CODEDEPLOY_DEPLOYMENT_ID"
}
EOF

log "✅ AfterInstall 단계 완료"