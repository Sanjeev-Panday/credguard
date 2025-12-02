package com.credguard.application;

import com.credguard.application.ai.PhysicalDocumentParsingService;
import com.credguard.domain.CredentialIssuanceResult;
import com.credguard.domain.Issuer;
import com.credguard.domain.PhysicalDocument;
import com.credguard.domain.VerifiableCredential;
import com.credguard.infra.aries.AriesCloudAgentClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialIssuanceServiceTest {

    @Mock
    private PhysicalDocumentParsingService documentParsingService;

    @Mock
    private AriesCloudAgentClient ariesClient;

    private CredentialIssuanceService credentialIssuanceService;

    @BeforeEach
    void setUp() {
        credentialIssuanceService = new CredentialIssuanceService(documentParsingService, ariesClient);
    }

    @Test
    void shouldSuccessfullyIssueCredentialFromDocument() {
        // Given
        byte[] documentBytes = "test document content".getBytes();
        String fileName = "passport.jpg";
        PhysicalDocument.DocumentType documentType = PhysicalDocument.DocumentType.PASSPORT;
        String walletDid = "did:example:wallet123";

        PhysicalDocument mockDocument = new PhysicalDocument(
            "doc-123",
            documentType,
            fileName,
            Instant.now(),
            documentBytes,
            Map.of("passportNumber", "A1234567", "fullName", "John Doe"),
            PhysicalDocument.ProcessingStatus.EXTRACTED,
            null
        );

        VerifiableCredential mockCredential = VerifiableCredential.fromPhysicalDocument(
            mockDocument,
            new Issuer("did:web:credguard.com", "CredGuard", true),
            walletDid,
            "conn-123"
        );

        CredentialIssuanceResult mockResult = CredentialIssuanceResult.success(
            mockCredential,
            "exchange-123",
            "https://agent.example.com/offer/123",
            1500L
        );

        when(documentParsingService.parseDocument(documentBytes, fileName, documentType))
            .thenReturn(mockDocument);
        when(ariesClient.createConnectionInvitation(walletDid))
            .thenReturn("conn-123");
        when(ariesClient.issueCredential(any(VerifiableCredential.class)))
            .thenReturn(mockResult);

        // When
        CredentialIssuanceResult result = credentialIssuanceService.issueCredentialFromDocument(
            documentBytes, fileName, documentType, walletDid
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.verifiableCredential()).isNotNull();
        assertThat(result.credentialExchangeId()).isEqualTo("exchange-123");
        assertThat(result.offerUrl()).isEqualTo("https://agent.example.com/offer/123");
        assertThat(result.processingTimeMs()).isGreaterThan(0);
    }

    @Test
    void shouldParseDocumentOnlyInPreviewMode() {
        // Given
        byte[] documentBytes = "test document content".getBytes();
        String fileName = "drivers_license.jpg";
        PhysicalDocument.DocumentType documentType = PhysicalDocument.DocumentType.DRIVERS_LICENSE;

        PhysicalDocument mockDocument = new PhysicalDocument(
            "doc-456",
            documentType,
            fileName,
            Instant.now(),
            documentBytes,
            Map.of("licenseNumber", "DL123456", "fullName", "Jane Smith"),
            PhysicalDocument.ProcessingStatus.EXTRACTED,
            null
        );

        when(documentParsingService.parseDocument(documentBytes, fileName, documentType))
            .thenReturn(mockDocument);

        // When
        PhysicalDocument result = credentialIssuanceService.parseDocumentOnly(
            documentBytes, fileName, documentType
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("doc-456");
        assertThat(result.type()).isEqualTo(documentType);
        assertThat(result.extractedAttributes()).containsEntry("licenseNumber", "DL123456");
        assertThat(result.extractedAttributes()).containsEntry("fullName", "Jane Smith");
    }

    @Test
    void shouldCreateCredentialFromParsedDocument() {
        // Given
        PhysicalDocument document = new PhysicalDocument(
            "doc-789",
            PhysicalDocument.DocumentType.DEGREE_CERTIFICATE,
            "degree.pdf",
            Instant.now(),
            new byte[0],
            Map.of("studentName", "Alice Johnson", "degreeName", "Bachelor of Science", "university", "Example University"),
            PhysicalDocument.ProcessingStatus.EXTRACTED,
            null
        );
        String walletDid = "did:example:alice";

        // When
        VerifiableCredential result = credentialIssuanceService.createCredentialFromDocument(
            document, walletDid
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.walletDid()).isEqualTo(walletDid);
        assertThat(result.sourceDocumentId()).isEqualTo("doc-789");
        assertThat(result.type()).contains("VerifiableCredential", "EducationCredential");
        assertThat(result.credentialSubject()).containsEntry("id", walletDid);
        assertThat(result.credentialSubject()).containsKey("attributes");
        assertThat(result.issuanceStatus()).isEqualTo(VerifiableCredential.IssuanceStatus.CREATED);
    }

    @Test
    void shouldCheckCredentialStatus() {
        // Given
        String exchangeId = "exchange-123";
        when(ariesClient.getCredentialExchangeStatus(exchangeId))
            .thenReturn("credential_acked");

        // When
        String status = credentialIssuanceService.getCredentialStatus(exchangeId);

        // Then
        assertThat(status).isEqualTo("credential_acked");
    }

    @Test
    void shouldRevokeCredential() {
        // Given
        String credentialId = "cred-123";
        when(ariesClient.revokeCredential(credentialId))
            .thenReturn(true);

        // When
        boolean result = credentialIssuanceService.revokeCredential(credentialId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldCheckConnectionStatus() {
        // Given
        String connectionId = "conn-123";
        when(ariesClient.getConnectionStatus(connectionId))
            .thenReturn("active");

        // When
        String status = credentialIssuanceService.getConnectionStatus(connectionId);

        // Then
        assertThat(status).isEqualTo("active");
    }
}