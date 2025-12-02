package com.credguard.web;

import com.credguard.application.CredentialIssuanceService;
import com.credguard.domain.CredentialIssuanceResult;
import com.credguard.domain.PhysicalDocument;
import com.credguard.domain.VerifiableCredential;
import com.credguard.exception.CredentialIssuanceException;
import com.credguard.exception.FileProcessingException;
import com.credguard.web.dto.CredentialIssuanceRequest;
import com.credguard.web.dto.CredentialIssuanceResponse;
import com.credguard.web.dto.CredentialStatusResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

/**
 * REST controller for verifiable credential issuance operations.
 */
@RestController
@RequestMapping("/api/credentials/issuance")
@CrossOrigin(origins = "http://localhost:3000")
public class CredentialIssuanceController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialIssuanceController.class);
    private final CredentialIssuanceService credentialIssuanceService;

    public CredentialIssuanceController(CredentialIssuanceService credentialIssuanceService) {
        this.credentialIssuanceService = credentialIssuanceService;
    }

    /**
     * Issues a verifiable credential from an uploaded physical document.
     */
    @PostMapping("/issue-from-document")
    public ResponseEntity<CredentialIssuanceResponse> issueFromDocument(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute CredentialIssuanceRequest request
    ) {
        logger.info("Received credential issuance request for file: {}, type: {}, wallet: {}", 
            file.getOriginalFilename(), request.documentType(), request.walletDid());
        
        if (file.isEmpty()) {
            logger.warn("Empty file uploaded for credential issuance");
            return ResponseEntity.badRequest().body(
                CredentialIssuanceResponse.failure("File is required and cannot be empty")
            );
        }

        try {
            byte[] fileBytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            
            if (request.previewOnly()) {
                // Preview mode: parse document and create credential without issuing
                PhysicalDocument document = credentialIssuanceService.parseDocumentOnly(
                    fileBytes, fileName, request.documentType()
                );
                
                VerifiableCredential credential = credentialIssuanceService.createCredentialFromDocument(
                    document, request.walletDid()
                );
                
                CredentialIssuanceResponse response = CredentialIssuanceResponse.preview(document, credential);
                
                logger.info("Document preview completed for file: {}", fileName);
                return ResponseEntity.ok(response);
            } else {
                // Full issuance
                CredentialIssuanceResult result = credentialIssuanceService.issueCredentialFromDocument(
                    fileBytes, fileName, request.documentType(), request.walletDid()
                );
                
                if (result.success()) {
                    // Get the source document from the credential's source document ID
                    // For now, create a minimal document info from the result
                    PhysicalDocument document = createDocumentFromResult(result, fileName, request.documentType());
                    
                    CredentialIssuanceResponse response = CredentialIssuanceResponse.success(result, document);
                    
                    logger.info("Credential issuance completed for file: {}", fileName);
                    return ResponseEntity.ok(response);
                } else {
                    CredentialIssuanceResponse response = CredentialIssuanceResponse.failure(result.errorMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }
            
        } catch (CredentialIssuanceException e) {
            logger.error("Credential issuance failed for file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CredentialIssuanceResponse.failure(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Unexpected error during credential issuance for file: {}", 
                file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CredentialIssuanceResponse.failure("Failed to process document: " + e.getMessage())
            );
        }
    }

    /**
     * Issues a verifiable credential asynchronously.
     */
    @PostMapping("/issue-from-document/async")
    public ResponseEntity<String> issueFromDocumentAsync(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute CredentialIssuanceRequest request
    ) {
        logger.info("Received async credential issuance request for file: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required and cannot be empty");
        }
        
        if (request.previewOnly()) {
            return ResponseEntity.badRequest().body("Preview mode is not supported for async operations");
        }

        try {
            byte[] fileBytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            
            CompletableFuture<CredentialIssuanceResult> future = 
                credentialIssuanceService.issueCredentialFromDocumentAsync(
                    fileBytes, fileName, request.documentType(), request.walletDid()
                );
            
            // In a real implementation, you'd return a job ID and provide a separate endpoint to check status
            String jobId = "job-" + System.currentTimeMillis();
            
            logger.info("Async credential issuance started for file: {}, job ID: {}", fileName, jobId);
            return ResponseEntity.accepted().body("Credential issuance started. Job ID: " + jobId);
            
        } catch (Exception e) {
            logger.error("Failed to start async credential issuance for file: {}", 
                file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "Failed to start credential issuance: " + e.getMessage()
            );
        }
    }

    /**
     * Checks the status of a credential exchange.
     */
    @GetMapping("/status/{credentialExchangeId}")
    public ResponseEntity<CredentialStatusResponse> getCredentialStatus(
            @PathVariable String credentialExchangeId
    ) {
        logger.debug("Checking status for credential exchange: {}", credentialExchangeId);
        
        try {
            String status = credentialIssuanceService.getCredentialStatus(credentialExchangeId);
            
            CredentialStatusResponse response = CredentialStatusResponse.of(
                null, // credential ID not available from exchange ID
                credentialExchangeId,
                status,
                "Status retrieved successfully"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get credential status for exchange: {}", credentialExchangeId, e);
            
            CredentialStatusResponse response = CredentialStatusResponse.of(
                null,
                credentialExchangeId,
                "error",
                "Failed to retrieve status: " + e.getMessage()
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Revokes a previously issued credential.
     */
    @PostMapping("/revoke/{credentialId}")
    public ResponseEntity<String> revokeCredential(
            @PathVariable String credentialId
    ) {
        logger.info("Revoking credential: {}", credentialId);
        
        try {
            boolean success = credentialIssuanceService.revokeCredential(credentialId);
            
            if (success) {
                return ResponseEntity.ok("Credential revoked successfully");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    "Failed to revoke credential"
                );
            }
            
        } catch (CredentialIssuanceException e) {
            logger.error("Failed to revoke credential: {}", credentialId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "Failed to revoke credential: " + e.getMessage()
            );
        }
    }

    /**
     * Gets the connection status with a wallet.
     */
    @GetMapping("/connection/{connectionId}/status")
    public ResponseEntity<String> getConnectionStatus(
            @PathVariable String connectionId
    ) {
        logger.debug("Checking connection status: {}", connectionId);
        
        try {
            String status = credentialIssuanceService.getConnectionStatus(connectionId);
            return ResponseEntity.ok("Connection status: " + status);
            
        } catch (Exception e) {
            logger.error("Failed to get connection status: {}", connectionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "Failed to get connection status: " + e.getMessage()
            );
        }
    }
    
    /**
     * Helper method to create a minimal PhysicalDocument from issuance result.
     */
    private PhysicalDocument createDocumentFromResult(
            CredentialIssuanceResult result, 
            String fileName, 
            PhysicalDocument.DocumentType documentType
    ) {
        // Extract attributes from the credential subject
        var credentialSubject = result.verifiableCredential().credentialSubject();
        
        return new PhysicalDocument(
            result.verifiableCredential().sourceDocumentId(),
            documentType,
            fileName,
            result.verifiableCredential().issuanceDate(),
            new byte[0], // Don't return file bytes in response
            credentialSubject,
            PhysicalDocument.ProcessingStatus.CREDENTIAL_ISSUED,
            null
        );
    }
}