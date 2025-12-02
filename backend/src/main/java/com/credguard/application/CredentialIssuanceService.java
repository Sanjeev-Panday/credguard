package com.credguard.application;

import com.credguard.application.ai.PhysicalDocumentParsingService;
import com.credguard.domain.CredentialIssuanceResult;
import com.credguard.domain.Issuer;
import com.credguard.domain.PhysicalDocument;
import com.credguard.domain.VerifiableCredential;
import com.credguard.exception.CredentialIssuanceException;
import com.credguard.infra.aries.AriesCloudAgentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for issuing verifiable credentials from physical documents.
 * Orchestrates the entire process from document parsing to credential issuance.
 */
@Service
public class CredentialIssuanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(CredentialIssuanceService.class);
    
    private final PhysicalDocumentParsingService documentParsingService;
    private final AriesCloudAgentClient ariesClient;
    
    // Default issuer for CredGuard
    private static final Issuer DEFAULT_ISSUER = new Issuer(
        "did:web:credguard.com:issuer",
        "CredGuard Identity Services",
        true
    );

    public CredentialIssuanceService(
            PhysicalDocumentParsingService documentParsingService,
            AriesCloudAgentClient ariesClient
    ) {
        this.documentParsingService = documentParsingService;
        this.ariesClient = ariesClient;
    }

    /**
     * Processes a physical document and issues a verifiable credential.
     * This is the main orchestration method for the entire credential issuance workflow.
     */
    public CredentialIssuanceResult issueCredentialFromDocument(
            byte[] documentBytes,
            String fileName,
            PhysicalDocument.DocumentType documentType,
            String walletDid
    ) {
        logger.info("Starting credential issuance process for document: {}, type: {}, wallet: {}", 
            fileName, documentType, walletDid);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Step 1: Parse the physical document to extract attributes
            PhysicalDocument document = documentParsingService.parseDocument(
                documentBytes, fileName, documentType
            );
            
            logger.info("Document parsing completed for: {}", document.id());
            
            // Step 2: Create connection with wallet
            String connectionId = ariesClient.createConnectionInvitation(walletDid);
            
            logger.info("Connection created with wallet: {} -> {}", walletDid, connectionId);
            
            // Step 3: Create verifiable credential from document
            VerifiableCredential credential = VerifiableCredential.fromPhysicalDocument(
                document, DEFAULT_ISSUER, walletDid, connectionId
            );
            
            logger.info("Verifiable credential created: {}", credential.id());
            
            // Step 4: Issue credential via Aries
            CredentialIssuanceResult result = ariesClient.issueCredential(credential);
            
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("Credential issuance process completed for document {} in {}ms. Success: {}", 
                document.id(), totalTime, result.success());
            
            return result;
            
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("Credential issuance process failed for document: {} in {}ms", fileName, totalTime, e);
            
            throw new CredentialIssuanceException(
                "Failed to issue credential from document: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Issues a verifiable credential asynchronously.
     * Returns immediately with a future that resolves when the credential is issued.
     */
    public CompletableFuture<CredentialIssuanceResult> issueCredentialFromDocumentAsync(
            byte[] documentBytes,
            String fileName,
            PhysicalDocument.DocumentType documentType,
            String walletDid
    ) {
        logger.info("Starting async credential issuance for document: {}", fileName);
        
        return CompletableFuture.supplyAsync(() -> 
            issueCredentialFromDocument(documentBytes, fileName, documentType, walletDid)
        );
    }

    /**
     * Revokes a previously issued verifiable credential.
     */
    public boolean revokeCredential(String credentialId) {
        logger.info("Revoking credential: {}", credentialId);
        
        try {
            boolean success = ariesClient.revokeCredential(credentialId);
            
            logger.info("Credential {} revocation {}", credentialId, success ? "successful" : "failed");
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to revoke credential: {}", credentialId, e);
            throw new CredentialIssuanceException(
                "Failed to revoke credential: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Checks the status of a credential exchange.
     */
    public String getCredentialStatus(String credentialExchangeId) {
        logger.debug("Checking status for credential exchange: {}", credentialExchangeId);
        
        try {
            return ariesClient.getCredentialExchangeStatus(credentialExchangeId);
        } catch (Exception e) {
            logger.error("Failed to check credential status for exchange: {}", credentialExchangeId, e);
            return "error";
        }
    }

    /**
     * Gets the connection status with a wallet.
     */
    public String getConnectionStatus(String connectionId) {
        logger.debug("Checking connection status: {}", connectionId);
        
        try {
            return ariesClient.getConnectionStatus(connectionId);
        } catch (Exception e) {
            logger.error("Failed to check connection status: {}", connectionId, e);
            return "error";
        }
    }

    /**
     * Parses a document without issuing a credential (for preview purposes).
     */
    public PhysicalDocument parseDocumentOnly(
            byte[] documentBytes,
            String fileName,
            PhysicalDocument.DocumentType documentType
    ) {
        logger.info("Parsing document for preview: {}, type: {}", fileName, documentType);
        
        try {
            return documentParsingService.parseDocument(documentBytes, fileName, documentType);
        } catch (Exception e) {
            logger.error("Failed to parse document: {}", fileName, e);
            throw new CredentialIssuanceException(
                "Failed to parse document: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Creates a verifiable credential from a parsed document (without issuing).
     */
    public VerifiableCredential createCredentialFromDocument(
            PhysicalDocument document,
            String walletDid
    ) {
        logger.info("Creating verifiable credential from document: {} for wallet: {}", 
            document.id(), walletDid);
        
        try {
            // Create a placeholder connection ID for preview
            String connectionId = "preview-connection";
            
            return VerifiableCredential.fromPhysicalDocument(
                document, DEFAULT_ISSUER, walletDid, connectionId
            );
        } catch (Exception e) {
            logger.error("Failed to create credential from document: {}", document.id(), e);
            throw new CredentialIssuanceException(
                "Failed to create credential: " + e.getMessage(),
                e
            );
        }
    }
}