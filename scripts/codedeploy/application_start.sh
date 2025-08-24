#!/bin/bash

# =============================================================================
# CodeDeploy Hook: ApplicationStart
# =============================================================================
# 애플리케이션 시작 단계
# - Blue-Green 배포 실행
# - 새 버전 컨테이너 시작
# - 초기 헬스체크
# =============================================================================

set -e

LOG_FILE="/opt/logs/codedeploy-application-start.log"

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "🚀 CodeDeploy ApplicationStart 단계 시작"

# 작업 디렉토리 이동
cd /opt/applications/iroom-backend

# 배포 정보 읽기
IMAGE_TAG=$(jq -r '.imageTag' deployment-manifest.json)

# Blue-Green 배포 실행
log "Blue-Green 배포 실행 (이미지 태그: $IMAGE_TAG)..."
if /opt/scripts/blue-green-deploy.sh deploy "$IMAGE_TAG" skip_cleanup; then
    log "✅ Blue-Green 배포 성공"
    
    # 배포 상태 업데이트
    jq '.status = "started" | .startedAt = "'"$(date -Iseconds)"'"' deployment-status.json > deployment-status.tmp
    mv deployment-status.tmp deployment-status.json
else
    log "❌ Blue-Green 배포 실패"
    
    # 배포 상태 업데이트
    jq '.status = "failed" | .failedAt = "'"$(date -Iseconds)"'"' deployment-status.json > deployment-status.tmp
    mv deployment-status.tmp deployment-status.json
    
    exit 1
fi

log "✅ ApplicationStart 단계 완료"