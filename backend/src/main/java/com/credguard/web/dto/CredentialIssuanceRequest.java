package com.credguard.web.dto;

import com.credguard.domain.PhysicalDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for issuing verifiable credentials from physical documents.
 */
public record CredentialIssuanceRequest(
        @NotNull(message = "documentType must not be null")
        PhysicalDocument.DocumentType documentType,
        
        @NotBlank(message = "walletDid must not be blank")
        String walletDid,
        
        // Optional: for preview mode without actual issuance
        Boolean previewOnly
) {
    
    public CredentialIssuanceRequest {
        // Default previewOnly to false if not specified
        if (previewOnly == null) {
            previewOnly = false;
        }
    }
}