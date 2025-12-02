package com.credguard.exception;

/**
 * Exception thrown when credential issuance operations fail.
 */
public class CredentialIssuanceException extends RuntimeException {
    
    public CredentialIssuanceException(String message) {
        super(message);
    }
    
    public CredentialIssuanceException(String message, Throwable cause) {
        super(message, cause);
    }
}