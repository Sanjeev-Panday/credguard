package com.credguard.web;

import com.credguard.exception.CredentialExtractionException;
import com.credguard.exception.FileProcessingException;
import com.credguard.exception.InvalidConfigurationException;
import com.credguard.web.dto.VerificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for consistent error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<VerificationResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        logger.warn("Validation failed: {}", ex.getMessage());
        
        Map<String, String> errors = ex.getBindingResult().getAllErrors().stream()
            .collect(Collectors.toMap(
                error -> ((FieldError) error).getField(),
                error -> error.getDefaultMessage()
            ));
        
        List<String> errorMessages = errors.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .toList();
        
        VerificationResponse response = new VerificationResponse(
            false, false, false, false,
            errorMessages,
            List.of(),
            "Validation failed",
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CredentialExtractionException.class)
    public ResponseEntity<VerificationResponse> handleExtractionException(
            CredentialExtractionException ex
    ) {
        logger.error("Credential extraction failed", ex);
        
        VerificationResponse response = new VerificationResponse(
            false, false, false, false,
            List.of("Credential extraction failed: " + ex.getMessage()),
            List.of(),
            "Failed to extract credential information from the provided file",
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<VerificationResponse> handleFileProcessingException(
            FileProcessingException ex
    ) {
        logger.error("File processing failed", ex);
        
        VerificationResponse response = new VerificationResponse(
            false, false, false, false,
            List.of("File processing failed: " + ex.getMessage()),
            List.of(),
            "Failed to process the uploaded file",
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidConfigurationException.class)
    public ResponseEntity<VerificationResponse> handleInvalidConfigurationException(
            InvalidConfigurationException ex
    ) {
        logger.error("Invalid configuration", ex);
        
        VerificationResponse response = new VerificationResponse(
            false, false, false, false,
            List.of("Configuration error: " + ex.getMessage()),
            List.of(),
            "Service configuration is invalid",
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<VerificationResponse> handleMaxUploadSizeException(
            MaxUploadSizeExceededException ex
    ) {
        logger.warn("File size exceeded: {}", ex.getMessage());
        
        VerificationResponse response = new VerificationResponse(
            false, false, false, false,
            List.of("File size exceeds maximum allowed size (10MB)"),
            List.of(),
            "File too large",
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<VerificationResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        
        VerificationResponse response = new VerificationResponse(
            false, false, false, false,
            List.of("An unexpected error occurred"),
            List.of(),
            "Internal server error",
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

