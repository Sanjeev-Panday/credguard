package com.credguard.application.validation;

import com.credguard.domain.Credential;

/**
 * Strategy interface for credential validation.
 */
public interface CredentialValidator {
    
    /**
     * Validates a credential according to specific criteria.
     * 
     * @param credential the credential to validate
     * @return validation result
     */
    ValidationResult validate(Credential credential);
    
    /**
     * Returns the name of this validator for logging and error reporting.
     */
    String getValidatorName();
}
