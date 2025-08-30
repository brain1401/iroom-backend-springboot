package com.iroomclass.springbackend;

import com.iroomclass.springbackend.common.UUIDv7Generator;
import com.iroomclass.springbackend.domain.admin.unit.entity.UnitCategory;
import com.iroomclass.springbackend.domain.admin.unit.repository.UnitCategoryRepository;
import com.iroomclass.springbackend.domain.user.info.entity.User;
import com.iroomclass.springbackend.domain.user.info.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UUID 마이그레이션 검증 테스트
 * 
 * UUIDv7 생성 및 기본적인 엔티티 CRUD 작업이 정상 동작하는지 확인합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UuidMigrationTest {

    @Autowired
    private UnitCategoryRepository unitCategoryRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("UUIDv7 생성기 정상 동작 테스트")
    void testUuidv7Generator() {
        // When
        UUID uuid1 = UUIDv7Generator.generate();
        UUID uuid2 = UUIDv7Generator.generate();
        
        // Then
        assertThat(uuid1).isNotNull();
        assertThat(uuid2).isNotNull();
        assertThat(uuid1).isNotEqualTo(uuid2);
        
        // UUIDv7은 시간 순으로 정렬 가능해야 함
        String uuid1String = uuid1.toString();
        String uuid2String = uuid2.toString();
        assertThat(uuid1String.compareTo(uuid2String)).isLessThan(0);
        
        System.out.println("Generated UUIDv7 1: " + uuid1);
        System.out.println("Generated UUIDv7 2: " + uuid2);
    }

    @Test
    @DisplayName("UnitCategory UUID 자동 생성 및 저장 테스트")
    void testUnitCategoryUuidGeneration() {
        // Given
        UnitCategory category = UnitCategory.builder()
            .categoryName("테스트 대분류")
            .displayOrder(1)
            .description("UUID 테스트용 대분류")
            .build();

        // When - ID를 설정하지 않고 저장 (@PrePersist에서 자동 생성되어야 함)
        UnitCategory savedCategory = unitCategoryRepository.save(category);
        
        // Then
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getCategoryName()).isEqualTo("테스트 대분류");
        
        System.out.println("Generated UnitCategory ID: " + savedCategory.getId());
    }
    
    @Test
    @DisplayName("User UUID 자동 생성 및 조회 테스트")
    void testUserUuidGeneration() {
        // Given
        User user = User.builder()
            .name("홍길동")
            .phone("010-1234-5678")
            .birthDate(LocalDate.of(2010, 3, 15))
            .grade(1)
            .build();

        // When
        User savedUser = userRepository.save(user);
        
        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        
        // 조회 테스트
        UUID userId = savedUser.getId();
        User foundUser = userRepository.findById(userId).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("홍길동");
        
        System.out.println("Generated User ID: " + savedUser.getId());
    }
    
    @Test
    @DisplayName("UUID 바이너리 변환 및 복원 테스트")
    void testUuidBinaryConversion() {
        // Given
        UUID originalUuid = UUIDv7Generator.generate();
        
        // When - UUID를 바이너리로 변환 후 다시 복원
        byte[] binaryUuid = UUIDv7Generator.toBytes(originalUuid);
        UUID restoredUuid = UUIDv7Generator.fromBytes(binaryUuid);
        
        // Then
        assertThat(binaryUuid).hasSize(16); // BINARY(16) 확인
        assertThat(restoredUuid).isEqualTo(originalUuid);
        
        System.out.println("Original UUID: " + originalUuid);
        System.out.println("Binary length: " + binaryUuid.length);
        System.out.println("Restored UUID: " + restoredUuid);
    }
}