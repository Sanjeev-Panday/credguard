package com.credguard.web;

import com.credguard.application.VerificationService;
import com.credguard.application.ai.CredentialExtractionService;
import com.credguard.web.dto.VerificationRequest;
import com.credguard.web.dto.VerificationResponse;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for credential verification operations.
 * <p>
 * This controller handles HTTP requests related to credential verification.
 * It delegates business logic to the application layer services.
 */
@RestController
@RequestMapping("/api/credentials")
@CrossOrigin(origins = "http://localhost:3000")
public class CredentialController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialController.class);

    private final VerificationService verificationService;
    private final CredentialExtractionService extractionService;

    /**
     * Constructor for dependency injection.
     *
     * @param verificationService the verification service
     * @param extractionService the credential extraction service
     */
    public CredentialController(
            VerificationService verificationService,
            CredentialExtractionService extractionService
    ) {
        this.verificationService = verificationService;
        this.extractionService = extractionService;
    }

    /**
     * Verifies a credential.
     * <p>
     * Accepts a credential in JSON format and returns the verification result.
     *
     * @param request the verification request containing the credential
     * @return verification response with the result
     */
    @PostMapping("/verify")
    public ResponseEntity<VerificationResponse> verify(
            @Valid @RequestBody VerificationRequest request
    ) {
        var credential = request.toCredential();
        var result = verificationService.verify(credential);
        var response = VerificationResponse.from(result);

        HttpStatus status = result.valid() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
    @PostMapping("/upload")
    public ResponseEntity<VerificationResponse> uploadAndVerify(
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new VerificationResponse(
                    false,
                    false,
                    false,
                    false,
                    java.util.List.of("File is required and cannot be empty"),
                    java.util.List.of(),
                    "No file provided",
                    null
                ));
        }

        try {
            byte[] fileBytes = file.getBytes();
            var credential = extractionService.extractCredential(fileBytes, file.getOriginalFilename());
            var result = verificationService.verify(credential);
            var response = VerificationResponse.from(result);
            
            HttpStatus status = result.valid() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
            
        } catch (CredentialExtractionService.CredentialExtractionException e) {
            logger.error("Credential extraction failed for file={}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new VerificationResponse(
                    false,
                    false,
                    false,
                    false,
                    java.util.List.of("Failed to extract credential: " + e.getMessage()),
                    java.util.List.of(),
                    "Credential extraction failed",
                    null
                ));
        } catch (Exception e) {
            logger.error("Unexpected error processing upload for file={}",
                    file == null ? "<null>" : file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new VerificationResponse(
                    false,
                    false,
                    false,
                    false,
                    java.util.List.of("Internal server error: " + e.getMessage()),
                    java.util.List.of(),
                    "An unexpected error occurred",
                    null
                ));
        }
    }
}

