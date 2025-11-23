package com.credguard.exception;

/**
 * Exception thrown when file processing operations fail.
 */
public class FileProcessingException extends RuntimeException {
    
    public FileProcessingException(String message) {
        super(message);
    }
    
    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
