package com.credguard.web.dto;

import com.credguard.domain.Credential;
import com.credguard.domain.Issuer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

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
        String issuedAt,
        
        String expiresAt,
        
        @NotNull(message = "claims must not be null")
        Map<String, Object> claims
) {
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
    
    public record IssuerRequest(
            @NotBlank(message = "id must not be blank")
            String id,
            
            @NotBlank(message = "displayName must not be blank")
            String displayName,
            
            boolean trusted
    ) {}
}

