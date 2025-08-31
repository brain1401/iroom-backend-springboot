# 도메인 응집도 개선 리팩토링 할 일 목록

## Phase 1: 분석 완료 ✅
- [x] 현재 도메인 구조 파악
- [x] 주요 엔티티 및 서비스 분석  
- [x] 도메인 간 의존성 매트릭스 생성
- [x] 응집도 문제점 식별

## Phase 2: 설계 (진행 중)
- [ ] 새로운 도메인 구조 설계
- [ ] 마이그레이션 전략 수립
- [ ] 의존성 해결 방안 설계
- [ ] 테스트 영향도 분석

## Phase 3: 리팩토링 실행  
- [ ] exam 도메인 통합 (admin.exam + user.exam + admin.question)
- [ ] user 도메인 통합 (admin.info + user.info)  
- [ ] analysis 도메인 생성 (admin.statistics + admin.dashboard)
- [ ] 의존성 업데이트 및 순환 참조 제거

## Phase 4: 검증
- [ ] 컴파일 오류 해결
- [ ] 테스트 실행 및 수정
- [ ] 기능 동일성 검증
- [ ] 성능 영향도 확인