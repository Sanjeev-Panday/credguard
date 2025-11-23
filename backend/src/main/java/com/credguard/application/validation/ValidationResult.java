package com.credguard.application.validation;

/**
 * Represents the result of a validation check.
 */
public record ValidationResult(
    boolean valid,
    String errorMessage,
    ValidationType validationType
) {
    
    public static ValidationResult success(ValidationType type) {
        return new ValidationResult(true, null, type);
    }
    
    public static ValidationResult failure(String errorMessage, ValidationType type) {
        return new ValidationResult(false, errorMessage, type);
    }
    
    public enum ValidationType {
        ISSUER_TRUST,
        EXPIRY,
        SIGNATURE
    }
}
