package com.credguard.web;

import com.credguard.application.ai.CredentialExtractionService;
import com.credguard.web.dto.VerificationResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers.
 * <p>
 * This handler provides consistent error responses across all endpoints
 * and handles validation errors, extraction failures, and other exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param ex the validation exception
     * @return error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<VerificationResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        List<String> errorMessages = errors.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.toList());
        
        VerificationResponse response = new VerificationResponse(
            false,
            false,
            false,
            false,
            errorMessages,
            List.of(),
            "Validation failed",
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles credential extraction failures.
     *
     * @param ex the extraction exception
     * @return error response
     */
    @ExceptionHandler(CredentialExtractionService.CredentialExtractionException.class)
    public ResponseEntity<VerificationResponse> handleExtractionException(
            CredentialExtractionService.CredentialExtractionException ex
    ) {
        VerificationResponse response = new VerificationResponse(
            false,
            false,
            false,
            false,
            List.of("Credential extraction failed: " + ex.getMessage()),
            List.of(),
            "Failed to extract credential information from the provided file",
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles file upload size limit exceeded errors.
     *
     * @param ex the upload size exception
     * @return error response
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<VerificationResponse> handleMaxUploadSizeException(
            MaxUploadSizeExceededException ex
    ) {
        VerificationResponse response = new VerificationResponse(
            false,
            false,
            false,
            false,
            List.of("File size exceeds maximum allowed size (10MB)"),
            List.of(),
            "File too large",
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param ex the exception
     * @return error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<VerificationResponse> handleGenericException(Exception ex) {
        VerificationResponse response = new VerificationResponse(
            false,
            false,
            false,
            false,
            List.of("An unexpected error occurred: " + ex.getMessage()),
            List.of(),
            "Internal server error",
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

