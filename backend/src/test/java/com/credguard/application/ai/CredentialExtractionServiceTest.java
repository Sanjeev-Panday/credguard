package com.credguard.application.ai;

import com.credguard.domain.Credential;
import com.credguard.domain.Issuer;
import com.credguard.exception.CredentialExtractionException;
import com.credguard.infra.ai.AIVisionClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialExtractionServiceTest {

    @Mock
    private AIVisionClient aiVisionClient;

    private CredentialExtractionService service;

    @BeforeEach
    void setUp() {
        service = new CredentialExtractionService(aiVisionClient);
    }

    @Test
    void extractCredential_ValidFile_ReturnsCredential() {
        // Given
        byte[] fileBytes = "test file content".getBytes();
        String fileName = "test.pdf";
        Credential expectedCredential = createTestCredential();
        
        when(aiVisionClient.extractCredential(fileBytes, fileName))
            .thenReturn(expectedCredential);

        // When
        Credential result = service.extractCredential(fileBytes, fileName);

        // Then
        assertNotNull(result);
        assertEquals(expectedCredential.id(), result.id());
        verify(aiVisionClient).extractCredential(fileBytes, fileName);
    }

    @Test
    void extractCredential_NullFileBytes_ThrowsException() {
        // When & Then
        CredentialExtractionException exception = assertThrows(
            CredentialExtractionException.class,
            () -> service.extractCredential(null, "test.pdf")
        );
        
        assertTrue(exception.getMessage().contains("File bytes cannot be null or empty"));
    }

    @Test
    void extractCredential_EmptyFileBytes_ThrowsException() {
        // When & Then
        CredentialExtractionException exception = assertThrows(
            CredentialExtractionException.class,
            () -> service.extractCredential(new byte[0], "test.pdf")
        );
        
        assertTrue(exception.getMessage().contains("File bytes cannot be null or empty"));
    }

    @Test
    void extractCredential_NullFileName_NormalizesToUnknown() {
        // Given
        byte[] fileBytes = "test file content".getBytes();
        Credential expectedCredential = createTestCredential();
        
        when(aiVisionClient.extractCredential(any(), eq("unknown")))
            .thenReturn(expectedCredential);

        // When
        Credential result = service.extractCredential(fileBytes, null);

        // Then
        assertNotNull(result);
        verify(aiVisionClient).extractCredential(fileBytes, "unknown");
    }

    @Test
    void extractCredential_EmptyFileName_NormalizesToUnknown() {
        // Given
        byte[] fileBytes = "test file content".getBytes();
        Credential expectedCredential = createTestCredential();
        
        when(aiVisionClient.extractCredential(any(), eq("unknown")))
            .thenReturn(expectedCredential);

        // When
        Credential result = service.extractCredential(fileBytes, "");

        // Then
        assertNotNull(result);
        verify(aiVisionClient).extractCredential(fileBytes, "unknown");
    }

    @Test
    void extractCredential_BlankFileName_NormalizesToUnknown() {
        // Given
        byte[] fileBytes = "test file content".getBytes();
        Credential expectedCredential = createTestCredential();
        
        when(aiVisionClient.extractCredential(any(), eq("unknown")))
            .thenReturn(expectedCredential);

        // When
        Credential result = service.extractCredential(fileBytes, "   ");

        // Then
        assertNotNull(result);
        verify(aiVisionClient).extractCredential(fileBytes, "unknown");
    }

    @Test
    void extractCredential_AIVisionClientThrowsException_WrapsException() {
        // Given
        byte[] fileBytes = "test file content".getBytes();
        String fileName = "test.pdf";
        RuntimeException originalException = new RuntimeException("AI client error");
        
        when(aiVisionClient.extractCredential(fileBytes, fileName))
            .thenThrow(originalException);

        // When & Then
        CredentialExtractionException exception = assertThrows(
            CredentialExtractionException.class,
            () -> service.extractCredential(fileBytes, fileName)
        );
        
        assertTrue(exception.getMessage().contains("Failed to extract credential"));
        assertEquals(originalException, exception.getCause());
    }

    private Credential createTestCredential() {
        return new Credential(
            "test-cred-123",
            "VerifiableCredential",
            new Issuer("did:example:issuer", "Test Issuer", true),
            "did:example:subject",
            Instant.now(),
            Instant.now().plusSeconds(86400),
            Map.of("test", "value")
        );
    }
}
