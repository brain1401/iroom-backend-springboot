#!/bin/bash

# Student API Test Script
# Application running on: http://localhost:3057/api

BASE_URL="http://localhost:3057/api/student"

echo "========================================"
echo "Student API 테스트 시작"
echo "========================================"

# Test data - using example from StudentAuthRequest
AUTH_DATA='{
  "name": "홍길동",
  "birthDate": "2000-01-01", 
  "phone": "010-1234-5678"
}'

echo -e "\n1. 학생 로그인 테스트"
echo "POST $BASE_URL/login"
echo "Request: $AUTH_DATA"
echo "----------------------------------------"
curl -X POST \
  "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d "$AUTH_DATA" \
  -w "\nStatus: %{http_code}\nResponse Time: %{time_total}s\n" | jq '.' 2>/dev/null || cat

echo -e "\n========================================"

echo -e "\n2. 최근 시험 제출 내역 조회 테스트 (기본 페이징)"
echo "POST $BASE_URL/recent-submissions"
echo "Request: $AUTH_DATA"
echo "----------------------------------------" 
curl -X POST \
  "$BASE_URL/recent-submissions" \
  -H "Content-Type: application/json" \
  -d "$AUTH_DATA" \
  -w "\nStatus: %{http_code}\nResponse Time: %{time_total}s\n" | jq '.' 2>/dev/null || cat

echo -e "\n========================================"

echo -e "\n3. 최근 시험 제출 내역 조회 테스트 (커스텀 페이징)"
echo "POST $BASE_URL/recent-submissions?page=0&size=5"
echo "Request: $AUTH_DATA"
echo "----------------------------------------"
curl -X POST \
  "$BASE_URL/recent-submissions?page=0&size=5" \
  -H "Content-Type: application/json" \
  -d "$AUTH_DATA" \
  -w "\nStatus: %{http_code}\nResponse Time: %{time_total}s\n" | jq '.' 2>/dev/null || cat

echo -e "\n========================================"

echo -e "\n4. 시험 결과 요약 목록 조회 테스트"
echo "POST $BASE_URL/exam-results"  
echo "Request: $AUTH_DATA"
echo "----------------------------------------"
curl -X POST \
  "$BASE_URL/exam-results" \
  -H "Content-Type: application/json" \
  -d "$AUTH_DATA" \
  -w "\nStatus: %{http_code}\nResponse Time: %{time_total}s\n" | jq '.' 2>/dev/null || cat

echo -e "\n========================================"

echo -e "\n5. 학생 정보 조회 테스트"
echo "POST $BASE_URL/info"
echo "Request: $AUTH_DATA"
echo "----------------------------------------"
curl -X POST \
  "$BASE_URL/info" \
  -H "Content-Type: application/json" \
  -d "$AUTH_DATA" \
  -w "\nStatus: %{http_code}\nResponse Time: %{time_total}s\n" | jq '.' 2>/dev/null || cat

echo -e "\n========================================"

echo -e "\n6. 학생 로그아웃 테스트"
echo "POST $BASE_URL/logout"
echo "Request: $AUTH_DATA"
echo "----------------------------------------"
curl -X POST \
  "$BASE_URL/logout" \
  -H "Content-Type: application/json" \
  -d "$AUTH_DATA" \
  -w "\nStatus: %{http_code}\nResponse Time: %{time_total}s\n" | jq '.' 2>/dev/null || cat

echo -e "\n========================================"

# Note: exam-detail endpoint requires examId parameter
echo -e "\n7. 시험 상세 결과 조회 테스트 (예시 UUID 사용)"
echo "POST $BASE_URL/exam-detail/123e4567-e89b-12d3-a456-426614174000"
echo "Request: $AUTH_DATA" 
echo "Note: 실제 존재하는 examId가 필요합니다"
echo "----------------------------------------"
curl -X POST \
  "$BASE_URL/exam-detail/123e4567-e89b-12d3-a456-426614174000" \
  -H "Content-Type: application/json" \
  -d "$AUTH_DATA" \
  -w "\nStatus: %{http_code}\nResponse Time: %{time_total}s\n" | jq '.' 2>/dev/null || cat

echo -e "\n========================================"
echo "Student API 테스트 완료"
echo "========================================"

echo -e "\n추가 테스트 케이스:"
echo "- 잘못된 인증 정보로 테스트"
echo "- 존재하지 않는 학생 정보로 테스트"
echo "- 필수 필드 누락 테스트"
echo "- 잘못된 전화번호 형식 테스트"