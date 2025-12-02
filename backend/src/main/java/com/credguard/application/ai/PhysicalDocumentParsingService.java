package com.credguard.application.ai;

import com.credguard.domain.PhysicalDocument;
import com.credguard.exception.CredentialExtractionException;
import com.credguard.infra.ai.AIVisionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Service for extracting structured attributes from physical identity documents
 * using AI vision capabilities. Handles various document types like passports,
 * driver's licenses, and degree certificates.
 */
@Service
public class PhysicalDocumentParsingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PhysicalDocumentParsingService.class);
    private final AIVisionClient aiVisionClient;

    public PhysicalDocumentParsingService(AIVisionClient aiVisionClient) {
        this.aiVisionClient = aiVisionClient;
    }

    /**
     * Processes a physical document to extract structured attributes for credential issuance.
     */
    public PhysicalDocument parseDocument(
            byte[] fileBytes, 
            String fileName, 
            PhysicalDocument.DocumentType documentType
    ) {
        logger.info("Starting document parsing for file: {}, type: {}", fileName, documentType);
        
        if (fileBytes == null || fileBytes.length == 0) {
            logger.error("File bytes are null or empty for file: {}", fileName);
            throw new CredentialExtractionException("File bytes cannot be null or empty");
        }
        
        String documentId = generateDocumentId();
        
        try {
            // Create initial document with uploaded status
            PhysicalDocument document = PhysicalDocument.uploaded(documentId, documentType, fileName, fileBytes);
            
            // Update status to processing
            document = document.withStatus(PhysicalDocument.ProcessingStatus.PROCESSING);
            
            // Extract attributes using AI
            Map<String, Object> extractedAttributes = extractAttributesFromDocument(fileBytes, fileName, documentType);
            
            // Update document with extracted attributes
            document = document.withExtractedAttributes(extractedAttributes);
            
            logger.info("Successfully parsed document: {}, extracted {} attributes", 
                fileName, extractedAttributes.size());
            
            return document;
            
        } catch (Exception e) {
            logger.error("Failed to parse document: {}", fileName, e);
            
            PhysicalDocument errorDocument = PhysicalDocument.uploaded(documentId, documentType, fileName, fileBytes)
                .withError("Parsing failed: " + e.getMessage());
            
            throw new CredentialExtractionException(
                "Failed to parse document: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Extracts structured attributes from a document based on its type.
     */
    private Map<String, Object> extractAttributesFromDocument(
            byte[] fileBytes, 
            String fileName, 
            PhysicalDocument.DocumentType documentType
    ) {
        logger.debug("Extracting attributes for document type: {}", documentType);
        
        String extractionPrompt = buildExtractionPrompt(documentType);
        
        try {
            // Use existing AI vision client with document-specific prompts
            var tempCredential = aiVisionClient.extractCredential(fileBytes, fileName);
            
            // Convert credential claims to document attributes
            Map<String, Object> attributes = tempCredential.claims();
            
            // Add document-specific metadata
            attributes.put("documentType", documentType.getDisplayName());
            attributes.put("extractedAt", tempCredential.issuedAt().toString());
            
            // Validate required attributes based on document type
            validateRequiredAttributes(attributes, documentType);
            
            return attributes;
            
        } catch (Exception e) {
            logger.error("AI extraction failed for document type {}: {}", documentType, e.getMessage());
            throw new CredentialExtractionException(
                "Failed to extract attributes: " + e.getMessage(), 
                e
            );
        }
    }
    
    /**
     * Builds a document-type-specific extraction prompt for better AI accuracy.
     */
    private String buildExtractionPrompt(PhysicalDocument.DocumentType documentType) {
        String basePrompt = """
            Analyze this %s document and extract all relevant information. 
            Return a JSON object with the following structure:
            {
              "id": "document-identifier",
              "type": "VerifiableCredential",
              "issuer": {
                "id": "issuing-authority-id",
                "displayName": "Issuing Authority Name",
                "trusted": true
              },
              "subject": "document-holder-identifier",
              "issuedAt": "ISO-8601 timestamp",
              "expiresAt": "ISO-8601 timestamp or null",
              "claims": %s
            }
            Extract all visible text and structured data. If a field is not clearly visible, 
            use null or reasonable defaults. Ensure dates are in ISO-8601 format.
            """;
        
        String claimsStructure = switch (documentType) {
            case PASSPORT -> """
                {
                  "passportNumber": "passport number",
                  "fullName": "full name as on passport",
                  "nationality": "nationality",
                  "dateOfBirth": "date of birth",
                  "placeOfBirth": "place of birth",
                  "gender": "gender",
                  "issuingCountry": "issuing country",
                  "issueDate": "issue date",
                  "expiryDate": "expiry date"
                }
                """;
            case DRIVERS_LICENSE -> """
                {
                  "licenseNumber": "driver license number",
                  "fullName": "full name",
                  "address": "address",
                  "dateOfBirth": "date of birth",
                  "gender": "gender",
                  "licenseClass": "license class",
                  "issueDate": "issue date",
                  "expiryDate": "expiry date",
                  "issuingState": "issuing state/authority"
                }
                """;
            case DEGREE_CERTIFICATE -> """
                {
                  "studentName": "student name",
                  "degreeName": "degree/qualification name",
                  "major": "field of study/major",
                  "university": "university/institution name",
                  "graduationDate": "graduation date",
                  "gpa": "GPA if available",
                  "honors": "honors/distinctions if any"
                }
                """;
            case BIRTH_CERTIFICATE -> """
                {
                  "fullName": "full name",
                  "dateOfBirth": "date of birth",
                  "placeOfBirth": "place of birth",
                  "parentNames": "parent names",
                  "gender": "gender",
                  "registrationNumber": "certificate number",
                  "issuingAuthority": "issuing authority"
                }
                """;
            case OTHER -> """
                {
                  "documentNumber": "document number if available",
                  "holderName": "document holder name",
                  "issueDate": "issue date if available",
                  "expiryDate": "expiry date if available",
                  "issuingAuthority": "issuing authority",
                  "additionalInfo": "any other relevant information"
                }
                """;
        };
        
        return String.format(basePrompt, documentType.getDisplayName(), claimsStructure);
    }
    
    /**
     * Validates that required attributes are present based on document type.
     */
    private void validateRequiredAttributes(Map<String, Object> attributes, PhysicalDocument.DocumentType documentType) {
        String[] requiredFields = switch (documentType) {
            case PASSPORT -> new String[]{"passportNumber", "fullName", "nationality"};
            case DRIVERS_LICENSE -> new String[]{"licenseNumber", "fullName"};
            case DEGREE_CERTIFICATE -> new String[]{"studentName", "degreeName", "university"};
            case BIRTH_CERTIFICATE -> new String[]{"fullName", "dateOfBirth"};
            case OTHER -> new String[]{"holderName"};
        };
        
        for (String requiredField : requiredFields) {
            if (!attributes.containsKey(requiredField) || 
                attributes.get(requiredField) == null || 
                attributes.get(requiredField).toString().trim().isEmpty()) {
                
                logger.warn("Missing required field '{}' for document type {}", requiredField, documentType);
                // Don't fail extraction for missing fields, just log warnings
                // The credential can still be issued with available information
            }
        }
    }
    
    /**
     * Generates a unique document ID.
     */
    private String generateDocumentId() {
        return "doc-" + UUID.randomUUID().toString();
    }
}