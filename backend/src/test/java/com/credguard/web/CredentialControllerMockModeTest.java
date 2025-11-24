package com.credguard.web;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for CredentialController with mock mode enabled.
 * This validates that the mock mode feature works correctly end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "ai.mock-mode=true"
})
class CredentialControllerMockModeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadCredential_MockModeEnabled_ReturnsValidMockCredential() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-credential.pdf",
            "application/pdf",
            "test file content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/credentials/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.issuerTrusted").value(true))
            .andExpect(jsonPath("$.signatureValid").value(true))
            .andExpect(jsonPath("$.notExpired").value(true))
            .andExpect(jsonPath("$.credential").exists())
            .andExpect(jsonPath("$.credential.id").value("mock-credential-123"))
            .andExpect(jsonPath("$.credential.type").value("VerifiableCredential"))
            .andExpect(jsonPath("$.credential.issuer.id").value("did:example:issuer"))
            .andExpect(jsonPath("$.credential.issuer.displayName").value("Mock Issuer"))
            .andExpect(jsonPath("$.credential.issuer.trusted").value(true))
            .andExpect(jsonPath("$.credential.subject").value("did:example:subject"))
            .andExpect(jsonPath("$.credential.issuedAt").exists())
            .andExpect(jsonPath("$.credential.expiresAt").exists())
            .andExpect(jsonPath("$.credential.claims.degree").value("Bachelor of Science"))
            .andExpect(jsonPath("$.credential.claims.university").value("Example University"))
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void uploadCredential_MockModeEnabledWithImageFile_ReturnsValidMockCredential() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-credential.png",
            "image/png",
            "fake png content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/credentials/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.credential.id").value("mock-credential-123"));
    }

    @Test
    void uploadCredential_MockModeEnabledEmptyFile_ReturnsBadRequest() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.pdf",
            "application/pdf",
            new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/credentials/upload").file(emptyFile))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.valid").value(false))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors[0]").value(Matchers.containsString("File is required and cannot be empty")));
    }

    @Test
    void uploadCredential_MockModeEnabledWithDifferentFileNames_AllReturnMockCredential() throws Exception {
        // Test with different file types to ensure mock mode works regardless of file type
        String[] fileNames = {"test.pdf", "test.png", "test.jpg", "test.jpeg"};
        
        for (String fileName : fileNames) {
            MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                "application/octet-stream",
                "test content".getBytes()
            );

            mockMvc.perform(multipart("/api/credentials/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.credential.id").value("mock-credential-123"));
        }
    }
}
