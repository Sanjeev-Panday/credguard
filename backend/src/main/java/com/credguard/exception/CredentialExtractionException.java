package com.credguard.exception;

/**
 * Exception thrown when credential extraction from files fails.
 */
public class CredentialExtractionException extends RuntimeException {
    
    public CredentialExtractionException(String message) {
        super(message);
    }
    
    public CredentialExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
