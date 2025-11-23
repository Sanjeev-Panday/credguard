package com.credguard.application.ai;

import com.credguard.domain.Credential;
import com.credguard.exception.CredentialExtractionException;
import com.credguard.infra.ai.AIVisionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for extracting credential information from files using AI.
 */
@Service
public class CredentialExtractionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CredentialExtractionService.class);
    private final AIVisionClient aiVisionClient;

    public CredentialExtractionService(AIVisionClient aiVisionClient) {
        this.aiVisionClient = aiVisionClient;
    }

    public Credential extractCredential(byte[] fileBytes, String fileName) {
        logger.info("Extracting credential from file: {}", fileName);
        
        if (fileBytes == null || fileBytes.length == 0) {
            logger.error("File bytes are null or empty");
            throw new CredentialExtractionException("File bytes cannot be null or empty");
        }
        
        String normalizedFileName = (fileName == null || fileName.isBlank()) ? "unknown" : fileName;
        
        try {
            Credential credential = aiVisionClient.extractCredential(fileBytes, normalizedFileName);
            logger.info("Successfully extracted credential: {}", credential.id());
            return credential;
        } catch (Exception e) {
            logger.error("Failed to extract credential from file: {}", normalizedFileName, e);
            throw new CredentialExtractionException(
                "Failed to extract credential: " + e.getMessage(),
                e
            );
        }
    }
}

