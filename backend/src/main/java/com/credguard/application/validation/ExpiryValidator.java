package com.credguard.application.validation;

import com.credguard.domain.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Validates credential expiry status.
 */
@Component
public class ExpiryValidator implements CredentialValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpiryValidator.class);
    
    @Override
    public ValidationResult validate(Credential credential) {
        logger.debug("Validating expiry for credential: {}", credential.id());
        
        if (credential.expiresAt() == null) {
            logger.debug("Credential {} has no expiration date (perpetual)", credential.id());
            return ValidationResult.success(ValidationResult.ValidationType.EXPIRY);
        }
        
        if (credential.expiresAt().isBefore(Instant.now())) {
            String errorMsg = "Credential has expired";
            logger.warn("Expiry validation failed for credential {}: expired at {}", 
                credential.id(), credential.expiresAt());
            return ValidationResult.failure(errorMsg, ValidationResult.ValidationType.EXPIRY);
        }
        
        return ValidationResult.success(ValidationResult.ValidationType.EXPIRY);
    }
    
    @Override
    public String getValidatorName() {
        return "ExpiryValidator";
    }
}
