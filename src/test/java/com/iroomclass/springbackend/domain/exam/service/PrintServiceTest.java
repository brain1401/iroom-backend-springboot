package com.iroomclass.springbackend.domain.exam.service;

import com.iroomclass.springbackend.domain.exam.dto.print.PrintRequest;
import com.iroomclass.springbackend.domain.exam.dto.print.PrintResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 인쇄 서비스 단위 테스트 (S3 통합)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("인쇄 서비스 테스트 (S3 통합)")
class PrintServiceTest {

    @Mock
    private S3FileStorageService s3FileStorageService;
    
    @InjectMocks
    private PrintService printService;
    
    private PrintRequest printRequest;
    private static final String PRINT_JOB_ID = "print-job-123";
    private static final String PRESIGNED_URL = "https://test-bucket.s3.amazonaws.com/print-pdfs/print-job-123.pdf?signature=test";
    
    @BeforeEach
    void setUp() {
        UUID examSheetId = UUID.randomUUID();
        printRequest = new PrintRequest(
            examSheetId,
            List.of("EXAM_SHEET", "STUDENT_ANSWER_SHEET"),
            "TestDocument"
        );
    }
    
    @Test
    @DisplayName("PDF 파일 존재 확인 테스트 - 존재함")
    void testGetPdfFile_Exists() {
        // Given
        when(s3FileStorageService.isPdfFileExists(PRINT_JOB_ID)).thenReturn(true);
        
        // When
        boolean result = printService.getPdfFile(PRINT_JOB_ID);
        
        // Then
        assertThat(result).isTrue();
        verify(s3FileStorageService).isPdfFileExists(PRINT_JOB_ID);
    }
    
    @Test
    @DisplayName("PDF 파일 존재 확인 테스트 - 존재하지 않음")
    void testGetPdfFile_NotExists() {
        // Given
        when(s3FileStorageService.isPdfFileExists(PRINT_JOB_ID)).thenReturn(false);
        
        // When
        boolean result = printService.getPdfFile(PRINT_JOB_ID);
        
        // Then
        assertThat(result).isFalse();
        verify(s3FileStorageService).isPdfFileExists(PRINT_JOB_ID);
    }
    
    @Test
    @DisplayName("프리사인드 다운로드 URL 생성 테스트")
    void testGeneratePresignedDownloadUrl_Success() {
        // Given
        when(s3FileStorageService.generatePresignedDownloadUrl(PRINT_JOB_ID)).thenReturn(PRESIGNED_URL);
        
        // When
        String result = printService.generatePresignedDownloadUrl(PRINT_JOB_ID);
        
        // Then
        assertThat(result).isEqualTo(PRESIGNED_URL);
        verify(s3FileStorageService).generatePresignedDownloadUrl(PRINT_JOB_ID);
    }
    
    @Test
    @DisplayName("PDF 파일 삭제 성공 테스트")
    void testDeletePdfFile_Success() {
        // Given
        // S3FileStorageService의 deletePdfFile이 호출만 되면 됨
        
        // When
        printService.deletePdfFile(PRINT_JOB_ID);
        
        // Then
        verify(s3FileStorageService).deletePdfFile(PRINT_JOB_ID);
    }
    
    @Test
    @DisplayName("PDF 파일 저장 성공 테스트")
    void testSavePdfFile_Success() {
        // Given
        byte[] pdfData = "test pdf content".getBytes();
        
        when(s3FileStorageService.uploadPdfFile(any(String.class), eq(pdfData))).thenReturn(true);
        
        // When & Then
        printService.savePdfFile(PRINT_JOB_ID, pdfData);
        
        verify(s3FileStorageService).uploadPdfFile(any(String.class), eq(pdfData));
    }
    
    @Test
    @DisplayName("PDF 파일 저장 실패 테스트")
    void testSavePdfFile_Failed() {
        // Given
        byte[] pdfData = "test pdf content".getBytes();
        
        when(s3FileStorageService.uploadPdfFile(any(String.class), eq(pdfData))).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> printService.savePdfFile(PRINT_JOB_ID, pdfData))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("S3에 PDF 파일 저장에 실패했습니다");
        
        verify(s3FileStorageService).uploadPdfFile(any(String.class), eq(pdfData));
    }
    
    @Test
    @DisplayName("빈 printJobId로 PDF 파일 존재 확인 실패 테스트")
    void testGetPdfFile_EmptyPrintJobId() {
        // When & Then
        assertThatThrownBy(() -> printService.getPdfFile(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("printJobId가 비어있습니다");
        
        verify(s3FileStorageService, never()).isPdfFileExists(any());
    }
    
    @Test
    @DisplayName("null printJobId로 프리사인드 URL 생성 실패 테스트")
    void testGeneratePresignedDownloadUrl_NullPrintJobId() {
        // When & Then
        assertThatThrownBy(() -> printService.generatePresignedDownloadUrl(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("printJobId가 비어있습니다");
        
        verify(s3FileStorageService, never()).generatePresignedDownloadUrl(any());
    }
    
    @Test
    @DisplayName("빈 printJobId로 PDF 파일 삭제 실패 테스트")
    void testDeletePdfFile_EmptyPrintJobId() {
        // When & Then
        assertThatThrownBy(() -> printService.deletePdfFile(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("printJobId가 비어있습니다");
        
        verify(s3FileStorageService, never()).deletePdfFile(any());
    }
}