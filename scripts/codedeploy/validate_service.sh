#!/bin/bash

# =============================================================================
# CodeDeploy Hook: ValidateService
# =============================================================================
# 서비스 검증 및 배포 완료 처리
# - 상세 헬스체크 실행
# - 서비스 검증
# - 이전 환경 정리
# - 배포 상태 최종 업데이트
# =============================================================================

set -e

LOG_FILE="/opt/logs/codedeploy-validate-service.log"

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "✅ CodeDeploy ValidateService 단계 시작"

# 작업 디렉토리 이동
cd /opt/applications/iroom-backend

# 상세 헬스체크 실행
log "상세 헬스체크 실행..."

# 1. 기본 헬스체크 엔드포인트
for i in {1..10}; do
    if curl -f -s http://localhost/api/system/health > /dev/null; then
        log "✅ 기본 헬스체크 성공 (시도: $i/10)"
        break
    fi
    
    if [ $i -eq 10 ]; then
        log "❌ 기본 헬스체크 실패"
        exit 1
    fi
    
    log "⏳ 헬스체크 재시도 중... ($i/10)"
    sleep 10
done

# 2. 상세 헬스체크
log "상세 서비스 검증 중..."

# API 응답 테스트
api_response=$(curl -s http://localhost/api/system/health | jq -r '.status' 2>/dev/null || echo "UNKNOWN")
if [ "$api_response" = "UP" ]; then
    log "✅ API 헬스체크 성공"
else
    log "❌ API 헬스체크 실패: $api_response"
    exit 1
fi

# 데이터베이스 연결 테스트
db_status=$(curl -s http://localhost/api/system/health | jq -r '.components.db.status' 2>/dev/null || echo "UNKNOWN")
if [ "$db_status" = "UP" ]; then
    log "✅ 데이터베이스 연결 정상"
else
    log "⚠️ 데이터베이스 연결 상태: $db_status"
fi

# Redis 연결 테스트
redis_status=$(curl -s http://localhost/api/system/health | jq -r '.components.redis.status' 2>/dev/null || echo "UNKNOWN")
if [ "$redis_status" = "UP" ]; then
    log "✅ Redis 연결 정상"
else
    log "⚠️ Redis 연결 상태: $redis_status"
fi

# 3. 성능 테스트
log "기본 성능 테스트 실행..."
response_time=$(curl -o /dev/null -s -w '%{time_total}' http://localhost/api/system/health)
if (( $(echo "$response_time < 2.0" | bc -l) )); then
    log "✅ 응답 시간 정상: ${response_time}초"
else
    log "⚠️ 응답 시간 느림: ${response_time}초"
fi

# 4. 메모리 및 CPU 사용량 확인
log "시스템 리소스 사용량 확인..."
memory_usage=$(free | grep Mem | awk '{printf("%.1f", $3/$2 * 100.0)}')
cpu_usage=$(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')

log "메모리 사용량: ${memory_usage}%"
log "CPU 사용량: ${cpu_usage}%"

if (( $(echo "$memory_usage > 90" | bc -l) )); then
    log "⚠️ 메모리 사용량이 높습니다: ${memory_usage}%"
fi

# 5. 이전 환경 정리 (성공적인 배포 후)
log "이전 환경 정리 시작..."
current_env=$(grep -o 'server localhost:[0-9]*' /etc/nginx/conf.d/upstream.conf | grep -o '[0-9]*' | head -1)
if [ "$current_env" = "8080" ]; then
    old_env="green"
elif [ "$current_env" = "8081" ]; then
    old_env="blue"
fi

if [ -n "$old_env" ]; then
    log "이전 환경 정리: $old_env"
    /opt/scripts/blue-green-deploy.sh cleanup "$old_env" || log "⚠️ 이전 환경 정리 실패 (무시)"
fi

# 6. 최종 상태 확인
log "최종 배포 상태 확인..."
/opt/scripts/blue-green-deploy.sh status

# 7. 배포 상태 최종 업데이트
if [ -f "deployment-status.json" ]; then
    jq '.status = "completed" | .completedAt = "'"$(date -Iseconds)"'" | .healthCheck = {
        "api": "'"$api_response"'",
        "database": "'"$db_status"'", 
        "redis": "'"$redis_status"'",
        "responseTime": "'"$response_time"'",
        "memoryUsage": "'"$memory_usage"'%",
        "cpuUsage": "'"$cpu_usage"'%"
    }' deployment-status.json > deployment-status.tmp
    mv deployment-status.tmp deployment-status.json
fi

# 8. 배포 완료 알림 준비 (옵션)
log "배포 완료 정보 생성..."
cat > /opt/logs/deployment-success.json << EOF
{
    "status": "success",
    "timestamp": "$(date -Iseconds)",
    "imageTag": "$(jq -r '.imageTag' deployment-manifest.json)",
    "commitSha": "$(jq -r '.commitSha' deployment-manifest.json)",
    "healthCheck": {
        "api": "$api_response",
        "database": "$db_status",
        "redis": "$redis_status",
        "responseTime": "$response_time"
    },
    "environment": "production",
    "deploymentId": "$CODEDEPLOY_DEPLOYMENT_ID"
}
EOF

# 9. 이전 Docker 이미지 정리 (디스크 공간 확보)
log "이전 Docker 이미지 정리..."
docker images iroom/spring-backend --format "table {{.ID}}\t{{.Tag}}" | \
    tail -n +4 | head -n -3 | awk '{print $1}' | xargs -r docker rmi || log "⚠️ 이미지 정리 중 일부 실패 (무시)"

log "✅ ValidateService 단계 완료 - 배포 성공!"
log "🌐 서비스 URL: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)/api/system/health"
log "📊 배포 상태: /opt/applications/iroom-backend/deployment-status.json"