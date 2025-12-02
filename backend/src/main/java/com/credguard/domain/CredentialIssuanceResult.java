package com.credguard.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Represents the result of a credential issuance operation.
 * Contains information about the issued verifiable credential and the process outcome.
 */
public record CredentialIssuanceResult(
        @NotNull(message = "verifiableCredential must not be null")
        VerifiableCredential verifiableCredential,
        
        @NotNull(message = "success must not be null")
        Boolean success,
        
        String errorMessage,
        
        @NotNull(message = "processedAt must not be null")
        Instant processedAt,
        
        // Aries-specific response data
        String credentialExchangeId,
        
        String offerUrl,
        
        Long processingTimeMs
) {
    
    /**
     * Creates a successful issuance result.
     */
    public static CredentialIssuanceResult success(
            VerifiableCredential credential, 
            String credentialExchangeId,
            String offerUrl,
            long processingTimeMs
    ) {
        return new CredentialIssuanceResult(
            credential,
            true,
            null,
            Instant.now(),
            credentialExchangeId,
            offerUrl,
            processingTimeMs
        );
    }
    
    /**
     * Creates a failed issuance result.
     */
    public static CredentialIssuanceResult failure(
            VerifiableCredential credential,
            String errorMessage,
            long processingTimeMs
    ) {
        return new CredentialIssuanceResult(
            credential,
            false,
            errorMessage,
            Instant.now(),
            null,
            null,
            processingTimeMs
        );
    }
    
    /**
     * Creates a failed issuance result with minimal information.
     */
    public static CredentialIssuanceResult failure(String errorMessage) {
        return new CredentialIssuanceResult(
            null,
            false,
            errorMessage,
            Instant.now(),
            null,
            null,
            0L
        );
    }
}