package com.credguard.web;

import com.credguard.application.VerificationService;
import com.credguard.application.ai.CredentialExtractionService;
import com.credguard.exception.CredentialExtractionException;
import com.credguard.exception.FileProcessingException;
import com.credguard.web.dto.VerificationRequest;
import com.credguard.web.dto.VerificationResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for credential verification operations.
 */
@RestController
@RequestMapping("/api/credentials")
@CrossOrigin(origins = "http://localhost:3000")
public class CredentialController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialController.class);
    private final VerificationService verificationService;
    private final CredentialExtractionService extractionService;

    public CredentialController(
            VerificationService verificationService,
            CredentialExtractionService extractionService
    ) {
        this.verificationService = verificationService;
        this.extractionService = extractionService;
    }

    @PostMapping("/verify")
    public ResponseEntity<VerificationResponse> verify(
            @Valid @RequestBody VerificationRequest request
    ) {
        logger.info("Received verification request for credential: {}", request.id());
        
        var credential = request.toCredential();
        var result = verificationService.verify(credential);
        var response = VerificationResponse.from(result);

        HttpStatus status = result.valid() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        logger.info("Verification completed for credential {}: valid={}", 
            request.id(), result.valid());
        
        return ResponseEntity.status(status).body(response);
    }
    
    @PostMapping("/upload")
    public ResponseEntity<VerificationResponse> uploadAndVerify(
            @RequestParam("file") MultipartFile file
    ) {
        logger.info("Received file upload: {}, size: {} bytes", 
            file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            logger.warn("Empty file uploaded");
            throw new FileProcessingException("File is required and cannot be empty");
        }

        try {
            byte[] fileBytes = file.getBytes();
            var credential = extractionService.extractCredential(fileBytes, file.getOriginalFilename());
            var result = verificationService.verify(credential);
            var response = VerificationResponse.from(result);
            
            HttpStatus status = result.valid() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            logger.info("Upload and verification completed for file {}: valid={}", 
                file.getOriginalFilename(), result.valid());
            
            return ResponseEntity.status(status).body(response);
            
        } catch (CredentialExtractionException e) {
            logger.error("Credential extraction failed for file: {}", file.getOriginalFilename(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error processing upload for file: {}", 
                file.getOriginalFilename(), e);
            throw new FileProcessingException("Failed to process uploaded file", e);
        }
    }
}

