package com.credguard.web.dto;

/**
 * Response DTO for credential status check operations.
 */
public record CredentialStatusResponse(
        String credentialId,
        String exchangeId,
        String status,
        String message,
        boolean active
) {
    
    /**
     * Creates a status response.
     */
    public static CredentialStatusResponse of(
            String credentialId,
            String exchangeId,
            String status,
            String message
    ) {
        boolean active = "credential_acked".equals(status) || 
                        "issued".equals(status) || 
                        "active".equals(status);
        
        return new CredentialStatusResponse(
            credentialId,
            exchangeId,
            status,
            message,
            active
        );
    }
}