package com.iroomclass.springbackend.domain.dashboard.service;

import com.iroomclass.springbackend.domain.dashboard.dto.DashboardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iroomclass.springbackend.domain.admin.repository.AdminRepository;
import com.iroomclass.springbackend.domain.admin.entity.Admin;

/**
 * 대시보드 서비스 (간단 버전)
 * 
 * <p>현재는 학원명만 반환하는 간단한 서비스입니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final AdminRepository adminRepository;

    /**
     * 대시보드 데이터 생성 (간단 버전)
     * 
     * @return 학원명이 포함된 대시보드 정보
     */
    public DashboardDto getDashboardData() {
        log.info("대시보드 데이터 조회 요청");
        
        // admin 계정에서 학원명 조회
        Admin admin = adminRepository.findByUsername("admin")
            .orElseThrow(() -> new RuntimeException("관리자 계정을 찾을 수 없습니다"));
        
        String academyName = admin.getAcademyName() != null ? 
            admin.getAcademyName() : "이룸클래스";
        
        return new DashboardDto(academyName);
    }
}