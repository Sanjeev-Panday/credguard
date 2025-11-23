package com.credguard.application.validation;

import com.credguard.domain.Credential;
import com.credguard.infra.crypto.SignatureVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Validates credential signature.
 */
@Component
public class SignatureValidator implements CredentialValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(SignatureValidator.class);
    private final SignatureVerificationService signatureService;
    
    public SignatureValidator(SignatureVerificationService signatureService) {
        this.signatureService = signatureService;
    }
    
    @Override
    public ValidationResult validate(Credential credential) {
        logger.debug("Validating signature for credential: {}", credential.id());
        
        String publicKeyUrl = extractPublicKeyUrl(credential);
        boolean isValid = signatureService.verifyCredentialSignature(
            credential.claims(), 
            publicKeyUrl
        );
        
        if (!isValid) {
            String errorMsg = "Credential signature is invalid";
            logger.warn("Signature validation failed for credential {}", credential.id());
            return ValidationResult.failure(errorMsg, ValidationResult.ValidationType.SIGNATURE);
        }
        
        return ValidationResult.success(ValidationResult.ValidationType.SIGNATURE);
    }
    
    @Override
    public String getValidatorName() {
        return "SignatureValidator";
    }
    
    private String extractPublicKeyUrl(Credential credential) {
        Object jwkUrl = credential.claims().get("issuerPublicKeyUrl");
        if (jwkUrl instanceof String) {
            return (String) jwkUrl;
        }
        return null;
    }
}
