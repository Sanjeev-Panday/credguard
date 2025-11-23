package com.credguard.domain;

import jakarta.validation.constraints.NotBlank;

public record Issuer(
    @NotBlank(message = "id must not be blank")
    String id,
    @NotBlank(message = "displayName must not be blank")
    String displayName,
    boolean trusted
) {}
