package com.iroomclass.springbackend.domain.exam.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.iroomclass.springbackend.config.S3Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * Amazon S3를 이용한 파일 저장 서비스
 * 
 * <p>PDF 파일을 S3에 업로드하고, 프리사인드 URL을 생성하여 안전한 다운로드를 제공합니다.
 * 기존의 메모리 기반 저장소를 대체하는 확장 가능한 클라우드 스토리지 솔루션입니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Config s3Config;

    /**
     * PDF 파일을 S3에 업로드
     * 
     * @param printJobId 인쇄 작업 ID (S3 키로 사용됨)
     * @param pdfContent PDF 바이트 배열
     * @return S3 업로드 성공 여부
     */
    public boolean uploadPdfFile(String printJobId, byte[] pdfContent) {
        try {
            String s3Key = generateS3Key(printJobId);
            
            log.info("S3 PDF 파일 업로드 시작: printJobId={}, s3Key={}, fileSize={}", 
                    printJobId, s3Key, pdfContent.length);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(s3Key)
                    .contentType(s3Config.getPdfContentType())
                    .contentLength((long) pdfContent.length)
                    .metadata(java.util.Map.of(
                            "print-job-id", printJobId,
                            "upload-time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            "content-type", "application/pdf"
                    ))
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(
                    new ByteArrayInputStream(pdfContent), pdfContent.length));

            log.info("S3 PDF 파일 업로드 완료: printJobId={}, s3Key={}, fileSize={}", 
                    printJobId, s3Key, pdfContent.length);
            
            return true;
            
        } catch (S3Exception e) {
            log.error("S3 PDF 파일 업로드 실패: printJobId={}, error={}", printJobId, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("PDF 파일 업로드 중 예상치 못한 오류: printJobId={}, error={}", printJobId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * PDF 파일 다운로드용 프리사인드 URL 생성
     * 
     * @param printJobId 인쇄 작업 ID
     * @return 프리사인드 다운로드 URL (실패 시 null)
     */
    public String generatePresignedDownloadUrl(String printJobId) {
        try {
            String s3Key = generateS3Key(printJobId);
            
            log.info("S3 프리사인드 URL 생성 시작: printJobId={}, s3Key={}", printJobId, s3Key);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(s3Key)
                    .responseContentType(s3Config.getPdfContentType())
                    .responseContentDisposition("attachment; filename=\"" + printJobId + ".pdf\"")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(s3Config.getPresignedUrlDurationValue())
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("S3 프리사인드 URL 생성 완료: printJobId={}, url={}", printJobId, presignedUrl);
            
            return presignedUrl;
            
        } catch (S3Exception e) {
            log.error("S3 프리사인드 URL 생성 실패: printJobId={}, error={}", printJobId, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("프리사인드 URL 생성 중 예상치 못한 오류: printJobId={}, error={}", printJobId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * S3에서 PDF 파일 삭제
     * 
     * @param printJobId 인쇄 작업 ID
     * @return 삭제 성공 여부
     */
    public boolean deletePdfFile(String printJobId) {
        try {
            String s3Key = generateS3Key(printJobId);
            
            log.info("S3 PDF 파일 삭제 시작: printJobId={}, s3Key={}", printJobId, s3Key);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            log.info("S3 PDF 파일 삭제 완료: printJobId={}, s3Key={}", printJobId, s3Key);
            
            return true;
            
        } catch (S3Exception e) {
            log.error("S3 PDF 파일 삭제 실패: printJobId={}, error={}", printJobId, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("PDF 파일 삭제 중 예상치 못한 오류: printJobId={}, error={}", printJobId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * PDF 파일의 존재 여부 확인
     * 
     * @param printJobId 인쇄 작업 ID
     * @return 파일 존재 여부
     */
    public boolean isPdfFileExists(String printJobId) {
        try {
            String s3Key = generateS3Key(printJobId);
            
            log.debug("S3 PDF 파일 존재 여부 확인: printJobId={}, s3Key={}", printJobId, s3Key);

            // HeadObject를 사용하여 객체 존재 여부만 확인 (내용 다운로드 없이)
            s3Client.headObject(builder -> builder
                    .bucket(s3Config.getBucketName())
                    .key(s3Key));

            log.debug("S3 PDF 파일 존재 확인됨: printJobId={}, s3Key={}", printJobId, s3Key);
            
            return true;
            
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.debug("S3 PDF 파일이 존재하지 않음: printJobId={}, s3Key={}", printJobId, generateS3Key(printJobId));
            } else {
                log.warn("S3 PDF 파일 존재 여부 확인 중 오류: printJobId={}, error={}", printJobId, e.getMessage());
            }
            return false;
        } catch (Exception e) {
            log.error("PDF 파일 존재 여부 확인 중 예상치 못한 오류: printJobId={}, error={}", printJobId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 인쇄 작업 ID를 기반으로 S3 키 생성
     * 
     * @param printJobId 인쇄 작업 ID
     * @return S3 객체 키
     */
    private String generateS3Key(String printJobId) {
        return s3Config.getPdfKeyPrefix() + printJobId + ".pdf";
    }

    /**
     * S3 버킷의 연결 상태 확인 (헬스체크용)
     * 
     * @return S3 연결 가능 여부
     */
    public boolean isS3Available() {
        try {
            log.debug("S3 연결 상태 확인 시작: bucket={}", s3Config.getBucketName());
            
            // 버킷 존재 여부 확인
            s3Client.headBucket(builder -> builder.bucket(s3Config.getBucketName()));
            
            log.debug("S3 연결 상태 확인 완료: bucket={}", s3Config.getBucketName());
            
            return true;
            
        } catch (S3Exception e) {
            log.error("S3 연결 실패: bucket={}, error={}", s3Config.getBucketName(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("S3 연결 상태 확인 중 예상치 못한 오류: bucket={}, error={}", 
                    s3Config.getBucketName(), e.getMessage());
            return false;
        }
    }
}