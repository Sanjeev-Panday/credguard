package com.credguard.web.dto;

import com.credguard.domain.CredentialIssuanceResult;
import com.credguard.domain.PhysicalDocument;
import com.credguard.domain.VerifiableCredential;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for credential issuance operations.
 */
public record CredentialIssuanceResponse(
        boolean success,
        String message,
        DocumentInfo document,
        CredentialInfo credential,
        IssuanceInfo issuance
) {
    
    /**
     * Information about the processed document.
     */
    public record DocumentInfo(
            String id,
            String documentType,
            String fileName,
            String status,
            Map<String, Object> extractedAttributes,
            Instant uploadedAt
    ) {
        
        public static DocumentInfo from(PhysicalDocument document) {
            return new DocumentInfo(
                document.id(),
                document.type().getDisplayName(),
                document.fileName(),
                document.status().getDisplayName(),
                document.extractedAttributes(),
                document.uploadedAt()
            );
        }
    }
    
    /**
     * Information about the verifiable credential.
     */
    public record CredentialInfo(
            String id,
            String[] context,
            String[] type,
            String issuer,
            Map<String, Object> credentialSubject,
            Instant issuanceDate,
            Instant expirationDate,
            String status
    ) {
        
        public static CredentialInfo from(VerifiableCredential credential) {
            return new CredentialInfo(
                credential.id(),
                credential.context(),
                credential.type(),
                credential.issuer().displayName(),
                credential.credentialSubject(),
                credential.issuanceDate(),
                credential.expirationDate(),
                credential.issuanceStatus().getDisplayName()
            );
        }
    }
    
    /**
     * Information about the issuance process.
     */
    public record IssuanceInfo(
            String credentialExchangeId,
            String offerUrl,
            String connectionId,
            String walletDid,
            Long processingTimeMs,
            Instant processedAt
    ) {
        
        public static IssuanceInfo from(CredentialIssuanceResult result) {
            return new IssuanceInfo(
                result.credentialExchangeId(),
                result.offerUrl(),
                result.verifiableCredential() != null ? result.verifiableCredential().connectionId() : null,
                result.verifiableCredential() != null ? result.verifiableCredential().walletDid() : null,
                result.processingTimeMs(),
                result.processedAt()
            );
        }
    }
    
    /**
     * Creates a successful response from a credential issuance result.
     */
    public static CredentialIssuanceResponse success(
            CredentialIssuanceResult result,
            PhysicalDocument document
    ) {
        return new CredentialIssuanceResponse(
            true,
            "Credential issued successfully",
            DocumentInfo.from(document),
            CredentialInfo.from(result.verifiableCredential()),
            IssuanceInfo.from(result)
        );
    }
    
    /**
     * Creates a preview response (document parsed but credential not issued).
     */
    public static CredentialIssuanceResponse preview(
            PhysicalDocument document,
            VerifiableCredential credential
    ) {
        return new CredentialIssuanceResponse(
            true,
            "Document parsed successfully - preview mode",
            DocumentInfo.from(document),
            CredentialInfo.from(credential),
            null // No issuance info in preview mode
        );
    }
    
    /**
     * Creates a failure response.
     */
    public static CredentialIssuanceResponse failure(
            String errorMessage,
            PhysicalDocument document
    ) {
        return new CredentialIssuanceResponse(
            false,
            errorMessage,
            document != null ? DocumentInfo.from(document) : null,
            null,
            null
        );
    }
    
    /**
     * Creates a simple failure response with just an error message.
     */
    public static CredentialIssuanceResponse failure(String errorMessage) {
        return new CredentialIssuanceResponse(
            false,
            errorMessage,
            null,
            null,
            null
        );
    }
}