package com.credguard.application;

import com.credguard.domain.Credential;
import com.credguard.domain.Issuer;
import com.credguard.domain.VerificationResult;
import com.credguard.infra.crypto.SignatureVerificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for VerificationService.
 */
@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private SignatureVerificationService signatureVerificationService;

    private VerificationService verificationService;

    @BeforeEach
    void setUp() {
        verificationService = new VerificationService(signatureVerificationService);
        // Default: signature verification returns true (valid signature)
        // Use lenient() to allow different argument combinations
        lenient()
            .when(signatureVerificationService.verifyCredentialSignature(any(), any()))
            .thenReturn(true);
    }

    @Test
    void verify_ValidCredential_ReturnsSuccess() {
        // Given
        Credential credential = createValidCredential();

        // When
        VerificationResult result = verificationService.verify(credential);

        // Then
        assertTrue(result.valid());
        assertTrue(result.issuerTrusted());
        assertTrue(result.signatureValid());
        assertTrue(result.notExpired());
        assertTrue(result.errors().isEmpty());
        assertNotNull(result.explanation());
        assertEquals(credential, result.credential());
    }

    @Test
    void verify_UntrustedIssuer_ReturnsFailure() {
        // Given
        Issuer untrustedIssuer = new Issuer("did:example:untrusted", "Untrusted Issuer", false);
        Credential credential = new Credential(
            "cred-1",
            "VerifiableCredential",
            untrustedIssuer,
            "did:example:subject",
            Instant.now(),
            Instant.now().plusSeconds(86400),
            Map.of()
        );

        // When
        VerificationResult result = verificationService.verify(credential);

        // Then
        assertFalse(result.valid());
        assertFalse(result.issuerTrusted());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream()
            .anyMatch(error -> error.contains("Issuer is not trusted")));
    }

    @Test
    void verify_ExpiredCredential_ReturnsFailure() {
        // Given
        Credential credential = new Credential(
            "cred-1",
            "VerifiableCredential",
            createTrustedIssuer(),
            "did:example:subject",
            Instant.now().minusSeconds(86400 * 2), // Issued 2 days ago
            Instant.now().minusSeconds(86400), // Expired 1 day ago
            Map.of()
        );

        // When
        VerificationResult result = verificationService.verify(credential);

        // Then
        assertFalse(result.valid());
        assertFalse(result.notExpired());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream()
            .anyMatch(error -> error.contains("expired")));
    }

    @Test
    void verify_PerpetualCredential_ReturnsSuccess() {
        // Given - credential with no expiration
        Credential credential = new Credential(
            "cred-1",
            "VerifiableCredential",
            createTrustedIssuer(),
            "did:example:subject",
            Instant.now(),
            null, // No expiration
            Map.of()
        );

        // When
        VerificationResult result = verificationService.verify(credential);

        // Then
        assertTrue(result.valid());
        assertTrue(result.notExpired());
    }

    @Test
    void verify_MultipleFailures_ReturnsAllErrors() {
        // Given - untrusted issuer and expired
        Issuer untrustedIssuer = new Issuer("did:example:untrusted", "Untrusted", false);
        Credential credential = new Credential(
            "cred-1",
            "VerifiableCredential",
            untrustedIssuer,
            "did:example:subject",
            Instant.now().minusSeconds(86400 * 2),
            Instant.now().minusSeconds(86400), // Expired
            Map.of()
        );

        // When
        VerificationResult result = verificationService.verify(credential);

        // Then
        assertFalse(result.valid());
        assertEquals(2, result.errors().size());
        assertTrue(result.errors().stream()
            .anyMatch(error -> error.contains("Issuer is not trusted")));
        assertTrue(result.errors().stream()
            .anyMatch(error -> error.contains("expired")));
    }

    @Test
    void verify_ValidCredentialWithClaims_ReturnsSuccess() {
        // Given
        Credential credential = new Credential(
            "cred-1",
            "VerifiableCredential",
            createTrustedIssuer(),
            "did:example:subject",
            Instant.now(),
            Instant.now().plusSeconds(86400 * 365),
            Map.of("degree", "Bachelor of Science", "university", "Example University")
        );

        // When
        VerificationResult result = verificationService.verify(credential);

        // Then
        assertTrue(result.valid());
        assertEquals(credential, result.credential());
    }

    // Helper methods

    private Credential createValidCredential() {
        return new Credential(
            "cred-123",
            "VerifiableCredential",
            createTrustedIssuer(),
            "did:example:subject",
            Instant.now(),
            Instant.now().plusSeconds(86400 * 365), // 1 year from now
            Map.of("test", "value")
        );
    }

    private Issuer createTrustedIssuer() {
        return new Issuer("did:example:issuer", "Trusted Issuer", true);
    }
}

