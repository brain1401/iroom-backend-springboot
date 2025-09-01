package com.iroomclass.springbackend.domain.exam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import com.iroomclass.springbackend.config.S3Config;

import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * S3 파일 저장 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("S3 파일 저장 서비스 테스트")
class S3FileStorageServiceTest {

    @Mock
    private S3Client s3Client;
    
    @Mock
    private S3Presigner s3Presigner;
    
    @Mock
    private S3TransferManager s3TransferManager;
    
    @Mock
    private S3Config s3Config;
    
    @InjectMocks
    private S3FileStorageService s3FileStorageService;
    
    private static final String BUCKET_NAME = "test-bucket";
    private static final String PRINT_JOB_ID = "print-job-123";
    private static final String PDF_KEY = "print-pdfs/print-job-123.pdf";
    
    @BeforeEach
    void setUp() {
        when(s3Config.getBucketName()).thenReturn(BUCKET_NAME);
        when(s3Config.getPresignedUrlDuration()).thenReturn(60);
    }
    
    @Test
    @DisplayName("PDF 파일 업로드 성공 테스트")
    void testUploadPdfFile_Success() {
        // Given
        byte[] pdfData = "test pdf content".getBytes();
        
        FileUpload mockFileUpload = mock(FileUpload.class);
        CompletedFileUpload mockCompletedUpload = mock(CompletedFileUpload.class);
        
        when(s3TransferManager.uploadFile(any(UploadFileRequest.class)))
            .thenReturn(mockFileUpload);
        when(mockFileUpload.completionFuture())
            .thenReturn(CompletableFuture.completedFuture(mockCompletedUpload));
        
        // When
        boolean result = s3FileStorageService.uploadPdfFile(PRINT_JOB_ID, pdfData);
        
        // Then
        assertThat(result).isTrue();
        verify(s3TransferManager).uploadFile(any(UploadFileRequest.class));
    }
    
    @Test
    @DisplayName("PDF 파일 업로드 실패 테스트 - null 데이터")
    void testUploadPdfFile_NullData() {
        // When & Then
        assertThatThrownBy(() -> s3FileStorageService.uploadPdfFile(PRINT_JOB_ID, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("PDF 데이터가 null입니다");
        
        verify(s3TransferManager, never()).uploadFile(any(UploadFileRequest.class));
    }
    
    @Test
    @DisplayName("PDF 파일 업로드 실패 테스트 - 빈 printJobId")
    void testUploadPdfFile_EmptyPrintJobId() {
        // Given
        byte[] pdfData = "test pdf content".getBytes();
        
        // When & Then
        assertThatThrownBy(() -> s3FileStorageService.uploadPdfFile("", pdfData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("printJobId가 비어있습니다");
        
        verify(s3TransferManager, never()).uploadFile(any(UploadFileRequest.class));
    }
    
    @Test
    @DisplayName("프리사인드 다운로드 URL 생성 성공 테스트")
    void testGeneratePresignedDownloadUrl_Success() throws Exception {
        // Given
        URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/print-pdfs/print-job-123.pdf?signature=test");
        
        PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(expectedUrl);
        
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
            .thenReturn(mockPresignedRequest);
        
        // When
        String result = s3FileStorageService.generatePresignedDownloadUrl(PRINT_JOB_ID);
        
        // Then
        assertThat(result).isEqualTo(expectedUrl.toString());
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }
    
    @Test
    @DisplayName("PDF 파일 존재 확인 테스트 - 존재함")
    void testIsPdfFileExists_True() {
        // Given
        HeadObjectResponse mockResponse = HeadObjectResponse.builder().build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);
        
        // When
        boolean result = s3FileStorageService.isPdfFileExists(PRINT_JOB_ID);
        
        // Then
        assertThat(result).isTrue();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }
    
    @Test
    @DisplayName("PDF 파일 존재 확인 테스트 - 존재하지 않음")
    void testIsPdfFileExists_False() {
        // Given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
            .thenThrow(NoSuchKeyException.builder().build());
        
        // When
        boolean result = s3FileStorageService.isPdfFileExists(PRINT_JOB_ID);
        
        // Then
        assertThat(result).isFalse();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }
    
    @Test
    @DisplayName("PDF 파일 삭제 성공 테스트")
    void testDeletePdfFile_Success() {
        // Given
        DeleteObjectResponse mockResponse = DeleteObjectResponse.builder().build();
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(mockResponse);
        
        // When
        s3FileStorageService.deletePdfFile(PRINT_JOB_ID);
        
        // Then
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }
    
    @Test
    @DisplayName("PDF 파일 삭제 실패 테스트 - S3 오류")
    void testDeletePdfFile_S3Exception() {
        // Given
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
            .thenThrow(S3Exception.builder().message("삭제 실패").build());
        
        // When & Then
        assertThatThrownBy(() -> s3FileStorageService.deletePdfFile(PRINT_JOB_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("S3에서 PDF 파일 삭제 실패");
    }
    

    
    @Test
    @DisplayName("빈 printJobId로 프리사인드 URL 생성 실패 테스트")
    void testGeneratePresignedDownloadUrl_EmptyPrintJobId() {
        // When & Then
        assertThatThrownBy(() -> s3FileStorageService.generatePresignedDownloadUrl(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("printJobId가 비어있습니다");
        
        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }
    
    @Test
    @DisplayName("null printJobId로 PDF 존재 확인 실패 테스트")
    void testIsPdfFileExists_NullPrintJobId() {
        // When & Then
        assertThatThrownBy(() -> s3FileStorageService.isPdfFileExists(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("printJobId가 비어있습니다");
        
        verify(s3Client, never()).headObject(any(HeadObjectRequest.class));
    }
}