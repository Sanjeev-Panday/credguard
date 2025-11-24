package com.credguard.infra.ai;

import com.credguard.config.AIConfiguration;
import com.credguard.domain.Credential;
import com.credguard.exception.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIVisionClientTest {

    private AIConfiguration aiConfiguration;
    private OpenAIVisionClient client;

    @BeforeEach
    void setUp() {
        aiConfiguration = new AIConfiguration();
    }

    @Test
    void extractCredential_MockModeEnabled_ReturnsMockCredential() {
        // Given
        aiConfiguration.setMockMode(true);
        client = new OpenAIVisionClient(aiConfiguration);
        byte[] fileBytes = "test file content".getBytes();

        // When
        Credential credential = client.extractCredential(fileBytes, "test.pdf");

        // Then
        assertNotNull(credential);
        assertEquals("mock-credential-123", credential.id());
        assertEquals("VerifiableCredential", credential.type());
        assertNotNull(credential.issuer());
        assertEquals("did:example:issuer", credential.issuer().id());
        assertEquals("Mock Issuer", credential.issuer().displayName());
        assertTrue(credential.issuer().trusted());
        assertEquals("did:example:subject", credential.subject());
        assertNotNull(credential.issuedAt());
        assertNotNull(credential.expiresAt());
        assertNotNull(credential.claims());
        assertEquals("Bachelor of Science", credential.claims().get("degree"));
        assertEquals("Example University", credential.claims().get("university"));
    }

    @Test
    void extractCredential_MockModeDisabledWithoutApiKey_ThrowsException() {
        // Given
        aiConfiguration.setMockMode(false);
        aiConfiguration.getOpenai().setApiKey(null);
        client = new OpenAIVisionClient(aiConfiguration);
        byte[] fileBytes = "test file content".getBytes();

        // When & Then
        InvalidConfigurationException exception = assertThrows(
            InvalidConfigurationException.class,
            () -> client.extractCredential(fileBytes, "test.pdf")
        );
        
        assertTrue(exception.getMessage().contains("OpenAI API key is not configured"));
    }

    @Test
    void extractCredential_MockModeDisabledWithEmptyApiKey_ThrowsException() {
        // Given
        aiConfiguration.setMockMode(false);
        aiConfiguration.getOpenai().setApiKey("");
        client = new OpenAIVisionClient(aiConfiguration);
        byte[] fileBytes = "test file content".getBytes();

        // When & Then
        InvalidConfigurationException exception = assertThrows(
            InvalidConfigurationException.class,
            () -> client.extractCredential(fileBytes, "test.pdf")
        );
        
        assertTrue(exception.getMessage().contains("OpenAI API key is not configured"));
    }

    @Test
    void extractCredential_MockModeEnabledWithNullFileName_ReturnsMockCredential() {
        // Given
        aiConfiguration.setMockMode(true);
        client = new OpenAIVisionClient(aiConfiguration);
        byte[] fileBytes = "test file content".getBytes();

        // When
        Credential credential = client.extractCredential(fileBytes, null);

        // Then
        assertNotNull(credential);
        assertEquals("mock-credential-123", credential.id());
    }

    @Test
    void extractCredential_MockModeEnabledWithEmptyFileName_ReturnsMockCredential() {
        // Given
        aiConfiguration.setMockMode(true);
        client = new OpenAIVisionClient(aiConfiguration);
        byte[] fileBytes = "test file content".getBytes();

        // When
        Credential credential = client.extractCredential(fileBytes, "");

        // Then
        assertNotNull(credential);
        assertEquals("mock-credential-123", credential.id());
    }

    @Test
    void extractCredential_MockModeEnabledMultipleTimes_ReturnsConsistentStructure() {
        // Given
        aiConfiguration.setMockMode(true);
        client = new OpenAIVisionClient(aiConfiguration);
        byte[] fileBytes = "test file content".getBytes();

        // When
        Credential credential1 = client.extractCredential(fileBytes, "test1.pdf");
        Credential credential2 = client.extractCredential(fileBytes, "test2.pdf");

        // Then - should return credentials with consistent structure
        assertNotNull(credential1);
        assertNotNull(credential2);
        assertEquals("mock-credential-123", credential1.id());
        assertEquals("mock-credential-123", credential2.id());
        assertEquals("VerifiableCredential", credential1.type());
        assertEquals("VerifiableCredential", credential2.type());
        assertEquals("did:example:issuer", credential1.issuer().id());
        assertEquals("did:example:issuer", credential2.issuer().id());
        assertEquals("did:example:subject", credential1.subject());
        assertEquals("did:example:subject", credential2.subject());
        assertNotNull(credential1.issuedAt());
        assertNotNull(credential2.issuedAt());
        assertNotNull(credential1.expiresAt());
        assertNotNull(credential2.expiresAt());
    }
}
