package com.credguard.domain;

import java.util.List;

public record VerificationResult(
        boolean valid,
        boolean issuerTrusted,
        boolean signatureValid,
        boolean notExpired,
        List<String> errors,
        List<String> warnings,
        String explanation,
        Credential credential
) {

    public static VerificationResult success(
            Credential credential,
            boolean issuerTrusted,
            boolean signatureValid,
            boolean notExpired,
            List<String> warnings,
            String explanation
    ) {
        return new VerificationResult(
                true,
                issuerTrusted,
                signatureValid,
                notExpired,
                List.of(),
                warnings != null ? List.copyOf(warnings) : List.of(),
                explanation,
                credential
        );
    }

    public static VerificationResult failure(
            Credential credential,
            List<String> errors,
            List<String> warnings,
            String explanation
    ) {
        return new VerificationResult(
                false,
                false,
                false,
                false,
                errors != null ? List.copyOf(errors) : List.of("Unknown error"),
                warnings != null ? List.copyOf(warnings) : List.of(),
                explanation,
                credential
        );
    }
}
