package com.credguard.application;

import com.credguard.application.validation.CredentialValidator;
import com.credguard.application.validation.ValidationResult;
import com.credguard.domain.Credential;
import com.credguard.domain.VerificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for verifying digital credentials using configurable validation
 * strategies.
 */
@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);
    private final List<CredentialValidator> validators;

    public VerificationService(List<CredentialValidator> validators) {
        this.validators = validators;
        logger.info("VerificationService initialized with {} validators", validators.size());
    }

    public VerificationResult verify(Credential credential) {
        logger.info("Starting verification for credential: {}", credential.id());

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        boolean issuerTrusted = true;
        boolean notExpired = true;
        boolean signatureValid = true;

        for (CredentialValidator validator : validators) {
            ValidationResult result = validator.validate(credential);

            if (!result.valid()) {
                errors.add(result.errorMessage());
                logger.debug("Validator {} failed for credential {}",
                        validator.getValidatorName(), credential.id());

                // Java 21: Switch expression with arrow syntax (already using it, but ensure
                // consistency)
                switch (result.validationType()) {
                    case ISSUER_TRUST -> issuerTrusted = false;
                    case EXPIRY -> notExpired = false;
                    case SIGNATURE -> signatureValid = false;
                }
            }
        }

        boolean valid = errors.isEmpty();
        String explanation = buildExplanation(credential, valid, errors);

        logger.info("Verification completed for credential {}: valid={}, errors={}",
                credential.id(), valid, errors.size());

        if (valid) {
            return VerificationResult.success(
                    credential,
                    issuerTrusted,
                    signatureValid,
                    notExpired,
                    warnings,
                    explanation);
        } else {
            return VerificationResult.failure(
                    credential,
                    errors,
                    warnings,
                    explanation);
        }
    }

    private String buildExplanation(Credential credential, boolean valid, List<String> errors) {
        if (valid) {
            return String.format(
                    "Credential '%s' issued by '%s' is valid. All checks passed.",
                    credential.id(),
                    credential.issuer().displayName());
        } else {
            return String.format(
                    "Credential '%s' verification failed. Issues: %s",
                    credential.id(),
                    String.join("; ", errors));
        }
    }
}
