package com.credguard.domain;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents an issuer of Verifiable Credentials.
 * 
 * @param id           unique identifier (DID or URL)
 * @param displayName  human-readable name for display
 * @param trusted      trust flag (true if trusted in current trust context)
 */
public record Issuer(
    @NotBlank(message = "id must not be blank")
    String id,
    @NotBlank(message = "displayName must not be blank")
    String displayName,
    boolean trusted
) {}
