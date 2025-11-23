package com.credguard.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

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
