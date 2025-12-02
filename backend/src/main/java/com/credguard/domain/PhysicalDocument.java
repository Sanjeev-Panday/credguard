package com.credguard.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a scanned physical identity document (passport, driver's license, degree certificate, etc.)
 * that will be processed to extract information for verifiable credential issuance.
 */
public record PhysicalDocument(
        @NotBlank(message = "id must not be blank")
        String id,
        
        @NotNull(message = "type must not be null")
        DocumentType type,
        
        @NotBlank(message = "fileName must not be blank")
        String fileName,
        
        @NotNull(message = "uploadedAt must not be null")
        Instant uploadedAt,
        
        @NotNull(message = "fileData must not be null")
        byte[] fileData,
        
        @NotNull(message = "extractedAttributes must not be null")
        Map<String, Object> extractedAttributes,
        
        ProcessingStatus status,
        
        String errorMessage
) {
    
    /**
     * Types of physical documents supported for credential issuance.
     */
    public enum DocumentType {
        PASSPORT("Passport"),
        DRIVERS_LICENSE("Driver's License"),
        DEGREE_CERTIFICATE("Degree Certificate"),
        BIRTH_CERTIFICATE("Birth Certificate"),
        OTHER("Other Document");
        
        private final String displayName;
        
        DocumentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Processing status of the document through the credential issuance pipeline.
     */
    public enum ProcessingStatus {
        UPLOADED("Uploaded"),
        PROCESSING("Processing"),
        EXTRACTED("Attributes Extracted"),
        CREDENTIAL_ISSUED("Credential Issued"),
        FAILED("Processing Failed");
        
        private final String displayName;
        
        ProcessingStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Creates a new PhysicalDocument with UPLOADED status.
     */
    public static PhysicalDocument uploaded(String id, DocumentType type, String fileName, byte[] fileData) {
        return new PhysicalDocument(
            id, 
            type, 
            fileName, 
            Instant.now(), 
            fileData, 
            Map.of(), 
            ProcessingStatus.UPLOADED, 
            null
        );
    }
    
    /**
     * Creates a new instance with updated status.
     */
    public PhysicalDocument withStatus(ProcessingStatus newStatus) {
        return new PhysicalDocument(
            this.id, 
            this.type, 
            this.fileName, 
            this.uploadedAt, 
            this.fileData, 
            this.extractedAttributes, 
            newStatus, 
            this.errorMessage
        );
    }
    
    /**
     * Creates a new instance with extracted attributes.
     */
    public PhysicalDocument withExtractedAttributes(Map<String, Object> attributes) {
        return new PhysicalDocument(
            this.id, 
            this.type, 
            this.fileName, 
            this.uploadedAt, 
            this.fileData, 
            attributes, 
            ProcessingStatus.EXTRACTED, 
            this.errorMessage
        );
    }
    
    /**
     * Creates a new instance with error status and message.
     */
    public PhysicalDocument withError(String errorMessage) {
        return new PhysicalDocument(
            this.id, 
            this.type, 
            this.fileName, 
            this.uploadedAt, 
            this.fileData, 
            this.extractedAttributes, 
            ProcessingStatus.FAILED, 
            errorMessage
        );
    }
}