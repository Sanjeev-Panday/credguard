package com.credguard.exception;

/**
 * Exception thrown when application configuration is invalid or missing.
 */
public class InvalidConfigurationException extends RuntimeException {
    
    public InvalidConfigurationException(String message) {
        super(message);
    }
    
    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
