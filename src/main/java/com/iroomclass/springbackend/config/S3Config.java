package com.iroomclass.springbackend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

/**
 * Amazon S3 연동을 위한 설정
 * 
 * <p>S3 클라이언트, 전송 관리자, 프리사인드 URL 생성기 등 
 * S3와의 연동에 필요한 빈들을 설정합니다.</p>
 * 
 * @author 이룸클래스 
 * @since 2025
 */
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
@Slf4j
public class S3Config {
    
    /**
     * AWS 리전
     */
    private String region = "ap-northeast-2";
    
    /**
     * S3 버킷 이름
     */
    private String bucketName;
    
    /**
     * 프리사인드 URL 유효시간 (분)
     */
    private int presignedUrlDuration = 60;
    
    /**
     * 멀티파트 업로드 최소 파트 크기 (MB)
     */
    private int multipartMinPartSize = 8;
    
    /**
     * S3 클라이언트 빈 생성
     * 
     * @return 설정된 S3Client 인스턴스
     */
    @Bean
    public S3Client s3Client() {
        log.info("S3 클라이언트 생성 시작: region={}, bucketName={}", region, bucketName);
        
        S3Configuration s3Config = S3Configuration.builder()
            .checksumValidationEnabled(true)
            .chunkedEncodingEnabled(true)
            .build();
            
        S3Client s3Client = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .serviceConfiguration(s3Config)
            .build();
            
        log.info("S3 클라이언트 생성 완료");
        return s3Client;
    }
    
    /**
     * S3 프리사인드 URL 생성기 빈 생성
     * 
     * @return 설정된 S3Presigner 인스턴스
     */
    @Bean
    public S3Presigner s3Presigner() {
        log.info("S3 프리사인드 URL 생성기 생성 시작: region={}, urlDuration={}분", region, presignedUrlDuration);
        
        S3Presigner presigner = S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
            
        log.info("S3 프리사인드 URL 생성기 생성 완료");
        return presigner;
    }
    
    /**
     * S3 비동기 클라이언트 빈 생성 (Transfer Manager용)
     * 
     * @return 설정된 S3AsyncClient 인스턴스
     */
    @Bean
    public S3AsyncClient s3AsyncClient() {
        log.info("S3 비동기 클라이언트 생성 시작: region={}", region);
        
        S3Configuration s3Config = S3Configuration.builder()
            .checksumValidationEnabled(true)
            .chunkedEncodingEnabled(true)
            .build();
            
        S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .serviceConfiguration(s3Config)
            .build();
            
        log.info("S3 비동기 클라이언트 생성 완료");
        return s3AsyncClient;
    }

    /**
     * S3 Transfer Manager 빈 생성
     * 
     * @return 설정된 S3TransferManager 인스턴스
     */
    @Bean
    public S3TransferManager s3TransferManager() {
        log.info("S3 Transfer Manager 생성 시작: minPartSize={}MB", multipartMinPartSize);
        
        S3TransferManager transferManager = S3TransferManager.builder()
            .s3Client(s3AsyncClient())
            .build();
            
        log.info("S3 Transfer Manager 생성 완료");
        return transferManager;
    }
    
    /**
     * 프리사인드 URL 유효시간을 Duration으로 반환
     * 
     * @return 프리사인드 URL 유효시간
     */
    public Duration getPresignedUrlDurationValue() {
        return Duration.ofMinutes(presignedUrlDuration);
    }
    
    /**
     * 멀티파트 업로드 최소 파트 크기를 바이트로 반환
     * 
     * @return 바이트 단위 파트 크기
     */
    public long getMultipartMinPartSizeBytes() {
        return (long) multipartMinPartSize * 1024 * 1024; // MB to bytes
    }
    
    /**
     * PDF 파일 저장을 위한 S3 키 접두사 반환
     * 
     * @return PDF 파일 키 접두사
     */
    public String getPdfKeyPrefix() {
        return "pdfs/";
    }
    
    /**
     * PDF 다운로드용 Content-Type 반환
     * 
     * @return PDF Content-Type
     */
    public String getPdfContentType() {
        return "application/pdf";
    }
}