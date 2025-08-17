1. docs 폴더의 파일 내용 기반으로 README.md 파일 생성
2. docs 폴더의 파일 내용 기반으로 CLAUDE.md 파일 개선

---

## 헬스체크

내 ai 서버는 이 주소고 "http://localhost:8000" 아래와 같아. 이걸 HealthController에서 너 자신과 이 ai서버의 헬스까지 체크해서 프론트로 보내줘.
db도 체크해주고.
정상적이지 않으면 down() 으로 프론트로 보내야겠지?
---
Responses
Curl

curl -X 'GET' \
  'http://localhost:8000/health' \
  -H 'accept: application/json'
Request URL
http://localhost:8000/health
Server response
Code	Details
200	
Response body
Download
{
  "status": "healthy",
  "timestamp": "2025-08-17T16:51:27.066353+00:00",
  "version": "1.0.0",
  "uptime_seconds": 13.063398838043213,
  "details": {
    "app_name": "Gemini AI Backend",
    "debug_mode": true,
    "rate_limiting_enabled": true,
    "authentication_required": false
  }
}
Response headers
 content-length: 243 
 content-type: application/json 
 date: Sun,17 Aug 2025 16:51:26 GMT 
 server: uvicorn 




