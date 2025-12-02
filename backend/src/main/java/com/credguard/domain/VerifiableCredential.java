package com.credguard.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a W3C Verifiable Credential that can be issued to a mobile wallet.
 * Extends the basic Credential with additional properties required for verifiable credentials.
 */
public record VerifiableCredential(
        @NotBlank(message = "id must not be blank")
        String id,
        
        @NotNull(message = "context must not be null")
        String[] context,
        
        @NotNull(message = "type must not be null")
        String[] type,
        
        @NotNull(message = "issuer must not be null")
        @Valid
        Issuer issuer,
        
        @NotNull(message = "credentialSubject must not be null")
        Map<String, Object> credentialSubject,
        
        @NotNull(message = "issuanceDate must not be null")
        Instant issuanceDate,
        
        Instant expirationDate,
        
        @NotNull(message = "proof must not be null")
        Map<String, Object> proof,
        
        // Connection details for wallet delivery
        String connectionId,
        
        String walletDid,
        
        // Status tracking
        @NotNull(message = "issuanceStatus must not be null")
        IssuanceStatus issuanceStatus,
        
        String statusMessage,
        
        // Reference to the source document
        String sourceDocumentId
) {
    
    /**
     * Status of the verifiable credential issuance process.
     */
    public enum IssuanceStatus {
        CREATED("Created"),
        OFFER_SENT("Offer Sent"),
        ACCEPTED("Accepted"),
        ISSUED("Issued"),
        REVOKED("Revoked"),
        FAILED("Failed");
        
        private final String displayName;
        
        IssuanceStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Standard W3C Verifiable Credentials context.
     */
    public static final String[] DEFAULT_CONTEXT = {
        "https://www.w3.org/2018/credentials/v1",
        "https://www.w3.org/2018/credentials/examples/v1"
    };
    
    /**
     * Creates a new VerifiableCredential from a physical document's extracted attributes.
     */
    public static VerifiableCredential fromPhysicalDocument(
            PhysicalDocument document, 
            Issuer issuer,
            String walletDid,
            String connectionId
    ) {
        String credentialId = "urn:credential:" + document.id();
        
        // Build credential subject from extracted attributes
        Map<String, Object> credentialSubject = Map.of(
            "id", walletDid,
            "documentType", document.type().getDisplayName(),
            "attributes", document.extractedAttributes()
        );
        
        // Determine credential type based on document type
        String[] credentialType = buildCredentialType(document.type());
        
        // Create basic proof structure (will be populated by Aries agent)
        Map<String, Object> proof = Map.of(
            "type", "JsonWebSignature2020",
            "created", Instant.now().toString(),
            "proofPurpose", "assertionMethod"
        );
        
        return new VerifiableCredential(
            credentialId,
            DEFAULT_CONTEXT,
            credentialType,
            issuer,
            credentialSubject,
            Instant.now(),
            calculateExpirationDate(document.type()),
            proof,
            connectionId,
            walletDid,
            IssuanceStatus.CREATED,
            "Credential created from document: " + document.fileName(),
            document.id()
        );
    }
    
    /**
     * Creates a new instance with updated status.
     */
    public VerifiableCredential withStatus(IssuanceStatus newStatus, String message) {
        return new VerifiableCredential(
            this.id,
            this.context,
            this.type,
            this.issuer,
            this.credentialSubject,
            this.issuanceDate,
            this.expirationDate,
            this.proof,
            this.connectionId,
            this.walletDid,
            newStatus,
            message,
            this.sourceDocumentId
        );
    }
    
    /**
     * Creates a new instance with updated proof.
     */
    public VerifiableCredential withProof(Map<String, Object> newProof) {
        return new VerifiableCredential(
            this.id,
            this.context,
            this.type,
            this.issuer,
            this.credentialSubject,
            this.issuanceDate,
            this.expirationDate,
            newProof,
            this.connectionId,
            this.walletDid,
            this.issuanceStatus,
            this.statusMessage,
            this.sourceDocumentId
        );
    }
    
    private static String[] buildCredentialType(PhysicalDocument.DocumentType documentType) {
        String specificType = switch (documentType) {
            case PASSPORT -> "PassportCredential";
            case DRIVERS_LICENSE -> "DriversLicenseCredential";
            case DEGREE_CERTIFICATE -> "EducationCredential";
            case BIRTH_CERTIFICATE -> "BirthCertificateCredential";
            case OTHER -> "DocumentCredential";
        };
        
        return new String[]{"VerifiableCredential", specificType};
    }
    
    private static Instant calculateExpirationDate(PhysicalDocument.DocumentType documentType) {
        // Different document types have different validity periods
        long daysToAdd = switch (documentType) {
            case PASSPORT -> 365 * 10; // 10 years for passport
            case DRIVERS_LICENSE -> 365 * 5; // 5 years for driver's license
            case DEGREE_CERTIFICATE -> 365 * 50; // 50 years for degree (long-lasting)
            case BIRTH_CERTIFICATE -> 365 * 100; // 100 years for birth certificate
            case OTHER -> 365 * 2; // 2 years default
        };
        
        return Instant.now().plusSeconds(daysToAdd * 24 * 60 * 60);
    }
}