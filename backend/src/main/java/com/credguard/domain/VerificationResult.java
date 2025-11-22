package com.credguard.domain;

import java.util.List;

/**
 * Represents the result of verifying a credential.
 * <p>
 * This record captures the details about the credential verification process, including flags for various checks (validity, trust, signature, expiry),
 * associated errors and warnings, an optional explanation, and the credential itself.
 * <p>
 * Use the {@link #success} and {@link #failure} static factory methods for convenient creation of results.
 *
 * @param valid          true if all verification checks passed
 * @param issuerTrusted  true if the issuer is trusted in current context
 * @param signatureValid true if the credential's signature is valid
 * @param notExpired     true if the credential has not expired
 * @param errors         list of errors encountered during verification (empty if none)
 * @param warnings       list of warnings produced during verification (empty if none)
 * @param explanation    detailed explanation or summary of the verification outcome
 * @param credential     the credential being verified (may be null in case of failure to parse)
 */
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

    /**
     * Creates a successful verification result.
     *
     * @param credential      The credential that was verified
     * @param issuerTrusted   Whether the issuer is trusted
     * @param signatureValid  Whether the signature is valid
     * @param notExpired      Whether the credential is not expired
     * @param warnings        Any non-fatal warnings to include (may be null)
     * @param explanation     Explanatory text or summary
     * @return a successful {@code VerificationResult}
     */
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

    /**
     * Creates a failed verification result.
     *
     * @param credential     The credential being verified (may be null if parse failed)
     * @param errors         List of error messages (must be non-empty)
     * @param warnings       List of warnings (may be empty or null)
     * @param explanation    Explanation of the failure
     * @return a failed {@code VerificationResult}
     */
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
