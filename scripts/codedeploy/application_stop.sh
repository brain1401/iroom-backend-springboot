#!/bin/bash

# =============================================================================
# CodeDeploy Hook: ApplicationStop
# =============================================================================
# 애플리케이션 중지 단계 (새 배포 시 이전 버전 정리)
# 실제로는 Blue-Green 배포에서는 이 단계에서 중지하지 않고
# ValidateService 단계에서 성공 확인 후 정리함
# =============================================================================

set -e

LOG_FILE="/opt/logs/codedeploy-application-stop.log"

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "🛑 CodeDeploy ApplicationStop 단계 시작"

# Blue-Green 배포에서는 ApplicationStop에서 실제로 중지하지 않음
# 대신 현재 상태만 로깅
log "현재 배포 상태 확인..."
/opt/scripts/blue-green-deploy.sh status

# 배포 로그 보관
log "배포 로그 보관..."
if [ -f "/opt/logs/deployment.log" ]; then
    cp /opt/logs/deployment.log "/opt/logs/deployment-$(date +%Y%m%d-%H%M%S).log"
fi

log "✅ ApplicationStop 단계 완료 (Blue-Green 배포에서는 실제 중지 없음)"