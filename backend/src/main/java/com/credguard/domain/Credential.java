package com.credguard.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable domain model representing a Verifiable Credential.
 * @param id        unique identifier of the credential, not blank
 * @param type      credential type, not blank
 * @param issuer    credential issuer (not null, validated)
 * @param subject   identifier of the subject, not blank
 * @param issuedAt  issuance timestamp (not null)
 * @param expiresAt expiration timestamp (nullable, may be null for perpetual credentials)
 * @param claims    claims map (not null, may be empty)
 */
public record Credential(
        @NotBlank(message = "id must not be blank")
        String id,
        
        @NotBlank(message = "type must not be blank")
        String type,
        
        @NotNull(message = "issuer must not be null")
        @Valid
        Issuer issuer,
        
        @NotBlank(message = "subject must not be blank")
        String subject,
        
        @NotNull(message = "issuedAt must not be null")
        Instant issuedAt,
        
        Instant expiresAt,
        
        @NotNull(message = "claims must not be null")
        Map<String, Object> claims
) {
}
