package com.credguard.infra.ai;

import com.credguard.domain.Credential;
import com.credguard.exception.CredentialExtractionException;

/**
 * Interface for AI vision services that extract credential information from files.
 */
public interface AIVisionClient {
    
    Credential extractCredential(byte[] fileBytes, String fileName);
}

