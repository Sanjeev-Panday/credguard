package com.credguard.infra.ai;

import com.credguard.domain.Credential;

/**
 * Interface for AI vision services that extract credential information from files.
 * <p>
 * Implementations should handle the conversion of raw file bytes (PDF, images, etc.)
 * into structured Credential domain objects using AI vision capabilities.
 */
public interface AIVisionClient {
    
    /**
     * Extracts credential information from a file using AI vision.
     *
     * @param fileBytes the raw file bytes (PDF, image, etc.)
     * @param fileName the name of the file (for context and format detection)
     * @return extracted Credential object
     * @throws CredentialExtractionException if extraction fails
     */
    Credential extractCredential(byte[] fileBytes, String fileName) throws CredentialExtractionException;
    
    /**
     * Exception thrown when credential extraction fails.
     */
    class CredentialExtractionException extends Exception {
        public CredentialExtractionException(String message) {
            super(message);
        }
        
        public CredentialExtractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

