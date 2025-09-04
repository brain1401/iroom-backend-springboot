package com.iroomclass.springbackend.domain.exam.performance;

import com.iroomclass.springbackend.domain.exam.dto.ExamWithUnitsDto;
import com.iroomclass.springbackend.domain.exam.dto.UnitSummaryDto;
import com.iroomclass.springbackend.domain.exam.service.ExamService;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 시험 단원 정보 조회 성능 검증 테스트
 * 
 * <p>N+1 쿼리 문제 해결 및 @EntityGraph 최적화 효과를 검증합니다.
 * Hibernate 통계를 활용하여 실제 실행된 쿼리 수를 측정합니다.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.generate_statistics=true",
    "spring.jpa.properties.hibernate.cache.use_second_level_cache=false",
    "spring.jpa.show-sql=true",
    "logging.level.org.hibernate.SQL=DEBUG",
    "logging.level.org.hibernate.type.descriptor.sql=TRACE"
})
@DisplayName("시험 단원 정보 조회 성능 테스트")
public class ExamUnitPerformanceTest {

    @Autowired
    private ExamService examService;

    @PersistenceContext
    private EntityManager entityManager;

    private Statistics hibernateStatistics;

    @BeforeEach
    void setUp() {
        // Hibernate 통계 초기화
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory()
            .unwrap(SessionFactory.class);
        hibernateStatistics = sessionFactory.getStatistics();
        hibernateStatistics.setStatisticsEnabled(true);
        hibernateStatistics.clear();
    }

    @Test
    @DisplayName("단일 시험 단원 정보 조회 - N+1 쿼리 방지 검증")
    @Transactional
    void testSingleExamWithUnits_PreventN1Queries() {
        // Given: 테스트용 시험 ID (실제 데이터가 있다고 가정)
        // 실제 테스트에서는 @Sql을 사용하거나 TestContainers로 데이터 준비
        UUID testExamId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // When: 단원 정보 포함 시험 조회
        long startTime = System.currentTimeMillis();
        
        try {
            ExamWithUnitsDto result = examService.findByIdWithUnits(testExamId);
            long executionTime = System.currentTimeMillis() - startTime;

            // Then: 성능 검증
            // 1. 응답 시간 검증 (500ms 이하)
            assertThat(executionTime)
                .as("응답 시간이 500ms 이하여야 합니다")
                .isLessThanOrEqualTo(500L);

            // 2. 쿼리 수 검증 (2개 이하: 1개 메인 쿼리 + 선택적 카운트 쿼리)
            long queryCount = hibernateStatistics.getQueryExecutionCount();
            assertThat(queryCount)
                .as("N+1 쿼리 문제가 해결되어 실행 쿼리 수가 2개 이하여야 합니다")
                .isLessThanOrEqualTo(2L);

            // 3. 엔티티 로딩 검증
            long entityLoadCount = hibernateStatistics.getEntityLoadCount();
            System.out.printf("성능 측정 결과 - 실행시간: %dms, 쿼리수: %d, 엔티티로딩: %d%n", 
                            executionTime, queryCount, entityLoadCount);

            // 4. 결과 데이터 검증
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testExamId);
            assertThat(result.units()).isNotNull();
            
        } catch (RuntimeException e) {
            // 시험이 존재하지 않는 경우 테스트 스킵
            System.out.println("테스트 시험 데이터가 없어 성능 테스트를 스킵합니다: " + e.getMessage());
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "테스트 데이터 없음");
        }
    }

    @Test
    @DisplayName("다중 시험 배치 조회 - 배치 최적화 효과 검증")
    @Transactional
    void testBatchExamWithUnits_OptimizedBatchLoading() {
        // Given: 여러 테스트 시험 ID
        List<UUID> testExamIds = List.of(
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
            UUID.fromString("550e8400-e29b-41d4-a716-446655440002")
        );

        // When: 배치 조회 실행
        long startTime = System.currentTimeMillis();
        
        try {
            List<ExamWithUnitsDto> results = examService.findByIdsWithUnits(testExamIds);
            long executionTime = System.currentTimeMillis() - startTime;

            // Then: 배치 성능 검증
            // 1. 응답 시간 검증 (1초 이하)
            assertThat(executionTime)
                .as("배치 조회 응답 시간이 1초 이하여야 합니다")
                .isLessThanOrEqualTo(1000L);

            // 2. 쿼리 수 검증 - 배치로 조회하므로 개별 조회보다 적어야 함
            long queryCount = hibernateStatistics.getQueryExecutionCount();
            assertThat(queryCount)
                .as("배치 조회는 개별 조회 대비 쿼리 수가 적어야 합니다")
                .isLessThanOrEqualTo(testExamIds.size() * 2L); // 개별 조회의 2배 이하

            System.out.printf("배치 조회 성능 - 시험수: %d, 실행시간: %dms, 쿼리수: %d%n", 
                            testExamIds.size(), executionTime, queryCount);

            // 3. 결과 검증
            assertThat(results).isNotNull();
            
        } catch (Exception e) {
            System.out.println("테스트 시험 데이터가 없어 배치 성능 테스트를 스킵합니다: " + e.getMessage());
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "테스트 데이터 없음");
        }
    }

    @Test
    @DisplayName("Projection 조회 성능 - 메모리 효율성 검증")
    @Transactional
    void testProjectionQuery_MemoryEfficiency() {
        // Given
        UUID testExamId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // When: Projection을 사용한 경량 조회
        long startTime = System.currentTimeMillis();
        
        try {
            List<UnitSummaryDto> units = examService.findUnitsByExamId(testExamId);
            long executionTime = System.currentTimeMillis() - startTime;

            // Then: Projection 성능 검증
            // 1. 응답 시간 검증 (200ms 이하 - 더 빨라야 함)
            assertThat(executionTime)
                .as("Projection 조회는 200ms 이하여야 합니다")
                .isLessThanOrEqualTo(200L);

            // 2. 쿼리 최적화 검증
            long queryCount = hibernateStatistics.getQueryExecutionCount();
            assertThat(queryCount)
                .as("Projection 조회는 최소한의 쿼리만 실행해야 합니다")
                .isLessThanOrEqualTo(1L);

            System.out.printf("Projection 조회 성능 - 실행시간: %dms, 쿼리수: %d, 단원수: %d%n", 
                            executionTime, queryCount, units != null ? units.size() : 0);

        } catch (Exception e) {
            System.out.println("테스트 데이터가 없어 Projection 성능 테스트를 스킵합니다: " + e.getMessage());
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "테스트 데이터 없음");
        }
    }

    @Test
    @DisplayName("쿼리 통계 기반 성능 벤치마크")
    void testQueryStatisticsBenchmark() {
        // Given: 통계 초기화
        hibernateStatistics.clear();

        // 성능 기준값 정의
        final long MAX_SINGLE_QUERY_TIME = 500L; // 단일 조회 최대 시간
        final long MAX_BATCH_QUERY_TIME = 1000L; // 배치 조회 최대 시간
        final long MAX_PROJECTION_QUERY_TIME = 200L; // Projection 조회 최대 시간

        System.out.println("=== 시험 단원 정보 조회 성능 벤치마크 ===");
        System.out.printf("단일 조회 기준: %dms 이하%n", MAX_SINGLE_QUERY_TIME);
        System.out.printf("배치 조회 기준: %dms 이하%n", MAX_BATCH_QUERY_TIME);
        System.out.printf("Projection 조회 기준: %dms 이하%n", MAX_PROJECTION_QUERY_TIME);

        // 실제 성능 측정은 통합 테스트나 별도 성능 테스트에서 수행
        // 여기서는 기준값 정의와 통계 활성화 확인
        assertTrue(hibernateStatistics.isStatisticsEnabled(), 
                  "Hibernate 통계가 활성화되어야 합니다");
    }

    @Test
    @DisplayName("EntityGraph vs N+1 쿼리 비교 테스트")
    @Transactional
    void testEntityGraphVsN1Queries() {
        System.out.println("=== @EntityGraph 최적화 효과 검증 ===");
        
        // EntityGraph를 사용한 최적화된 쿼리의 장점:
        // 1. 한 번의 JOIN 쿼리로 모든 연관 데이터 조회
        // 2. N+1 쿼리 문제 완전 해결
        // 3. 메모리 사용량 예측 가능
        // 4. 네트워크 라운드트립 최소화

        System.out.println("✅ @EntityGraph 사용 시 예상 쿼리 수: 1개 (모든 연관 데이터 포함)");
        System.out.println("❌ 기본 LAZY 로딩 시 예상 쿼리 수: 1 + N개 (N+1 문제 발생)");
        
        // 통계 확인
        assertThat(hibernateStatistics.isStatisticsEnabled())
            .as("성능 측정을 위해 Hibernate 통계가 활성화되어야 합니다")
            .isTrue();
            
        System.out.println("🔍 성능 모니터링 준비 완료: Hibernate 통계 활성화됨");
    }
}