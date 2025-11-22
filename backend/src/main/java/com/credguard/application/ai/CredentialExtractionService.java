package com.credguard.application.ai;

import com.credguard.domain.Credential;
import com.credguard.infra.ai.AIVisionClient;

import org.springframework.stereotype.Service;

/**
 * Service responsible for extracting credential information from files using AI.
 * <p>
 * This service acts as a bridge between the application layer and the infrastructure
 * layer, handling credential extraction from various file formats (PDF, images, etc.)
 * using AI vision capabilities.
 */
@Service
public class CredentialExtractionService {

    private final AIVisionClient aiVisionClient;

    /**
     * Constructor for dependency injection.
     *
     * @param aiVisionClient the AI vision client
     */
    public CredentialExtractionService(AIVisionClient aiVisionClient) {
        this.aiVisionClient = aiVisionClient;
    }

    /**
     * Extracts a credential from raw file bytes.
     *
     * @param fileBytes the raw file bytes
     * @param fileName the name of the file (for format detection)
     * @return extracted Credential object
     * @throws CredentialExtractionException if extraction fails
     */
    public Credential extractCredential(byte[] fileBytes, String fileName) throws CredentialExtractionException {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new CredentialExtractionException("File bytes cannot be null or empty");
        }
        
        if (fileName == null || fileName.isBlank()) {
            fileName = "unknown";
        }
        
        try {
            return aiVisionClient.extractCredential(fileBytes, fileName);
        } catch (AIVisionClient.CredentialExtractionException e) {
            throw new CredentialExtractionException(
                "Failed to extract credential: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Exception thrown when credential extraction fails.
     */
    public static class CredentialExtractionException extends Exception {
        public CredentialExtractionException(String message) {
            super(message);
        }
        
        public CredentialExtractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

