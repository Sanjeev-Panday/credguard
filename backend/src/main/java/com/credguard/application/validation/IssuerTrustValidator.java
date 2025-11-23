package com.credguard.application.validation;

import com.credguard.domain.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Validates issuer trust status.
 */
@Component
public class IssuerTrustValidator implements CredentialValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(IssuerTrustValidator.class);
    
    @Override
    public ValidationResult validate(Credential credential) {
        logger.debug("Validating issuer trust for credential: {}", credential.id());
        
        if (!credential.issuer().trusted()) {
            String errorMsg = String.format("Issuer is not trusted: %s", 
                credential.issuer().displayName());
            logger.warn("Issuer trust validation failed for credential {}: {}", 
                credential.id(), errorMsg);
            return ValidationResult.failure(errorMsg);
        }
        
        return ValidationResult.success();
    }
    
    @Override
    public String getValidatorName() {
        return "IssuerTrustValidator";
    }
}
