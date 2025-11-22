package com.credguard.web.dto;

import com.credguard.domain.Credential;
import com.credguard.domain.Issuer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for credential verification requests.
 * <p>
 * This DTO mirrors the Credential domain model structure for JSON deserialization.
 */
public record VerificationRequest(
        @NotBlank(message = "id must not be blank")
        String id,
        
        @NotBlank(message = "type must not be blank")
        String type,
        
        @NotNull(message = "issuer must not be null")
        @Valid
        IssuerRequest issuer,
        
        @NotBlank(message = "subject must not be blank")
        String subject,
        
        @NotNull(message = "issuedAt must not be null")
        String issuedAt, // ISO-8601 string
        
        String expiresAt, // ISO-8601 string, nullable
        
        @NotNull(message = "claims must not be null")
        Map<String, Object> claims
) {
    /**
     * Converts this DTO to a domain Credential object.
     *
     * @return Credential domain object
     */
    public Credential toCredential() {
        return new Credential(
            id,
            type,
            new Issuer(issuer.id(), issuer.displayName(), issuer.trusted()),
            subject,
            Instant.parse(issuedAt),
            expiresAt != null ? Instant.parse(expiresAt) : null,
            claims
        );
    }
    
    /**
     * Nested DTO for issuer information.
     */
    public record IssuerRequest(
            @NotBlank(message = "id must not be blank")
            String id,
            
            @NotBlank(message = "displayName must not be blank")
            String displayName,
            
            boolean trusted
    ) {}
}

