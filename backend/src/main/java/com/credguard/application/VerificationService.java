package com.credguard.application;

import com.credguard.domain.Credential;
import com.credguard.domain.VerificationResult;
import com.credguard.infra.crypto.SignatureVerificationService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Service responsible for verifying digital credentials.
 * <p>
 * This service performs three main validation checks:
 * <ul>
 *   <li>Issuer trust validation - checks if the issuer is trusted</li>
 *   <li>Expiry validation - checks if the credential has expired</li>
 *   <li>Signature validation - checks if the credential signature is valid using Nimbus JOSE</li>
 * </ul>
 * All checks are combined into a single VerificationResult.
 */
@Service
public class VerificationService {

    private final SignatureVerificationService signatureVerificationService;

    /**
     * Constructor for dependency injection.
     *
     * @param signatureVerificationService the signature verification service
     */
    public VerificationService(SignatureVerificationService signatureVerificationService) {
        this.signatureVerificationService = signatureVerificationService;
    }

    /**
     * Verifies a credential by performing all validation checks.
     *
     * @param credential the credential to verify
     * @return VerificationResult containing the outcome of all checks
     */
    public VerificationResult verify(Credential credential) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        boolean issuerTrusted = validateIssuer(credential);
        boolean notExpired = validateExpiry(credential);
        boolean signatureValid = validateSignature(credential);
        
        if (!issuerTrusted) {
            errors.add("Issuer is not trusted: " + credential.issuer().displayName());
        }
        
        if (!notExpired) {
            errors.add("Credential has expired");
        }
        
        if (!signatureValid) {
            errors.add("Credential signature is invalid");
        }
        
        boolean valid = errors.isEmpty();
        
        String explanation = buildExplanation(credential, issuerTrusted, notExpired, signatureValid, errors);
        
        if (valid) {
            return VerificationResult.success(
                credential,
                issuerTrusted,
                signatureValid,
                notExpired,
                warnings,
                explanation
            );
        } else {
            return VerificationResult.failure(
                credential,
                errors,
                warnings,
                explanation
            );
        }
    }
    
    /**
     * Validates if the issuer is trusted.
     *
     * @param credential the credential to check
     * @return true if the issuer is trusted, false otherwise
     */
    private boolean validateIssuer(Credential credential) {
        return credential.issuer().trusted();
    }
    
    /**
     * Validates if the credential has expired.
     * <p>
     * A credential with no expiration date (expiresAt is null) is considered
     * to never expire.
     *
     * @param credential the credential to check
     * @return true if the credential has not expired, false otherwise
     */
    private boolean validateExpiry(Credential credential) {
        if (credential.expiresAt() == null) {
            return true; // Perpetual credential
        }
        return credential.expiresAt().isAfter(Instant.now());
    }
    
    /**
     * Validates the credential signature.
     * <p>
     * Uses Nimbus JOSE to verify JWT/JWS signatures. If the credential
     * contains JWT data in its claims, it will be verified. Otherwise,
     * signature verification is considered not applicable.
     *
     * @param credential the credential to check
     * @return true if the signature is valid or verification is not applicable
     */
    private boolean validateSignature(Credential credential) {
        // Try to get issuer's public key URL from claims or issuer metadata
        String issuerPublicKeyUrl = extractPublicKeyUrl(credential);
        
        // Verify signature using the signature verification service
        return signatureVerificationService.verifyCredentialSignature(
            credential.claims(),
            issuerPublicKeyUrl
        );
    }
    
    /**
     * Extracts the issuer's public key URL from credential claims or issuer metadata.
     *
     * @param credential the credential
     * @return public key URL if found, null otherwise
     */
    private String extractPublicKeyUrl(Credential credential) {
        // Check claims for public key URL
        Object jwkUrl = credential.claims().get("issuerPublicKeyUrl");
        if (jwkUrl instanceof String) {
            return (String) jwkUrl;
        }
        
        // Could also construct from issuer ID if it's a DID
        // For now, return null and let the service handle it gracefully
        return null;
    }
    
    /**
     * Builds a human-readable explanation of the verification result.
     *
     * @param credential the credential being verified
     * @param issuerTrusted whether the issuer is trusted
     * @param notExpired whether the credential has not expired
     * @param signatureValid whether the signature is valid
     * @param errors list of errors encountered
     * @return explanation string
     */
    private String buildExplanation(
            Credential credential,
            boolean issuerTrusted,
            boolean notExpired,
            boolean signatureValid,
            List<String> errors
    ) {
        if (errors.isEmpty()) {
            return String.format(
                "Credential '%s' issued by '%s' is valid. All checks passed: issuer trusted, not expired, signature valid.",
                credential.id(),
                credential.issuer().displayName()
            );
        } else {
            return String.format(
                "Credential '%s' verification failed. Issues: %s",
                credential.id(),
                String.join("; ", errors)
            );
        }
    }
}

