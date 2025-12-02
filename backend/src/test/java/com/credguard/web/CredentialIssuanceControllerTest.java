package com.credguard.web;

import com.credguard.application.CredentialIssuanceService;
import com.credguard.domain.CredentialIssuanceResult;
import com.credguard.domain.PhysicalDocument;
import com.credguard.domain.VerifiableCredential;
import com.credguard.domain.Issuer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CredentialIssuanceController.class)
class CredentialIssuanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CredentialIssuanceService credentialIssuanceService;

    @Test
    void shouldIssueCredentialFromDocument() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "passport.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test passport content".getBytes()
        );

        PhysicalDocument mockDocument = new PhysicalDocument(
            "doc-123",
            PhysicalDocument.DocumentType.PASSPORT,
            "passport.jpg",
            Instant.now(),
            file.getBytes(),
            Map.of("passportNumber", "A1234567", "fullName", "John Doe"),
            PhysicalDocument.ProcessingStatus.CREDENTIAL_ISSUED,
            null
        );

        VerifiableCredential mockCredential = VerifiableCredential.fromPhysicalDocument(
            mockDocument,
            new Issuer("did:web:credguard.com", "CredGuard", true),
            "did:example:wallet123",
            "conn-123"
        );

        CredentialIssuanceResult mockResult = CredentialIssuanceResult.success(
            mockCredential,
            "exchange-123",
            "https://agent.example.com/offer/123",
            1500L
        );

        when(credentialIssuanceService.issueCredentialFromDocument(
            any(byte[].class),
            eq("passport.jpg"),
            eq(PhysicalDocument.DocumentType.PASSPORT),
            eq("did:example:wallet123")
        )).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(multipart("/api/credentials/issuance/issue-from-document")
                .file(file)
                .param("documentType", "PASSPORT")
                .param("walletDid", "did:example:wallet123")
                .param("previewOnly", "false"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Credential issued successfully"))
            .andExpect(jsonPath("$.document").exists())
            .andExpect(jsonPath("$.credential").exists())
            .andExpect(jsonPath("$.issuance").exists())
            .andExpect(jsonPath("$.issuance.credentialExchangeId").value("exchange-123"));
    }

    @Test
    void shouldReturnPreviewOnlyMode() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "license.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test license content".getBytes()
        );

        PhysicalDocument mockDocument = new PhysicalDocument(
            "doc-456",
            PhysicalDocument.DocumentType.DRIVERS_LICENSE,
            "license.jpg",
            Instant.now(),
            file.getBytes(),
            Map.of("licenseNumber", "DL123456", "fullName", "Jane Smith"),
            PhysicalDocument.ProcessingStatus.EXTRACTED,
            null
        );

        VerifiableCredential mockCredential = VerifiableCredential.fromPhysicalDocument(
            mockDocument,
            new Issuer("did:web:credguard.com", "CredGuard", true),
            "did:example:wallet456",
            "preview-connection"
        );

        when(credentialIssuanceService.parseDocumentOnly(
            any(byte[].class),
            eq("license.jpg"),
            eq(PhysicalDocument.DocumentType.DRIVERS_LICENSE)
        )).thenReturn(mockDocument);

        when(credentialIssuanceService.createCredentialFromDocument(
            eq(mockDocument),
            eq("did:example:wallet456")
        )).thenReturn(mockCredential);

        // When & Then
        mockMvc.perform(multipart("/api/credentials/issuance/issue-from-document")
                .file(file)
                .param("documentType", "DRIVERS_LICENSE")
                .param("walletDid", "did:example:wallet456")
                .param("previewOnly", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Document parsed successfully - preview mode"))
            .andExpect(jsonPath("$.document").exists())
            .andExpect(jsonPath("$.credential").exists())
            .andExpect(jsonPath("$.issuance").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestForEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/credentials/issuance/issue-from-document")
                .file(emptyFile)
                .param("documentType", "PASSPORT")
                .param("walletDid", "did:example:wallet123"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("File is required and cannot be empty"));
    }

    @Test
    void shouldGetCredentialStatus() throws Exception {
        // Given
        String exchangeId = "exchange-123";
        when(credentialIssuanceService.getCredentialStatus(exchangeId))
            .thenReturn("credential_acked");

        // When & Then
        mockMvc.perform(get("/api/credentials/issuance/status/{credentialExchangeId}", exchangeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exchangeId").value(exchangeId))
            .andExpect(jsonPath("$.status").value("credential_acked"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRevokeCredential() throws Exception {
        // Given
        String credentialId = "cred-123";
        when(credentialIssuanceService.revokeCredential(credentialId))
            .thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/credentials/issuance/revoke/{credentialId}", credentialId))
            .andExpect(status().isOk())
            .andExpect(content().string("Credential revoked successfully"));
    }

    @Test
    void shouldGetConnectionStatus() throws Exception {
        // Given
        String connectionId = "conn-123";
        when(credentialIssuanceService.getConnectionStatus(connectionId))
            .thenReturn("active");

        // When & Then
        mockMvc.perform(get("/api/credentials/issuance/connection/{connectionId}/status", connectionId))
            .andExpect(status().isOk())
            .andExpect(content().string("Connection status: active"));
    }
}