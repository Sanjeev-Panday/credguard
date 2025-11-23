package com.credguard.infra.ai;

import com.credguard.config.AIConfiguration;
import com.credguard.domain.Credential;
import com.credguard.domain.Issuer;
import com.credguard.exception.CredentialExtractionException;
import com.credguard.exception.InvalidConfigurationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Vision API client for extracting credentials from files.
 */
@Component
public class OpenAIVisionClient implements AIVisionClient {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIVisionClient.class);
    private static final String EXTRACTION_PROMPT = """
            Analyze this document and extract verifiable credential information. 
            Return a JSON object with the following structure:
            {
              "id": "credential-id",
              "type": "VerifiableCredential",
              "issuer": {
                "id": "issuer-id",
                "displayName": "Issuer Name",
                "trusted": true
              },
              "subject": "subject-identifier",
              "issuedAt": "ISO-8601 timestamp",
              "expiresAt": "ISO-8601 timestamp or null",
              "claims": {
                "key": "value"
              }
            }
            Extract all relevant information from the document. If a field is not present, use reasonable defaults.
            """;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AIConfiguration aiConfiguration;

    public OpenAIVisionClient(AIConfiguration aiConfiguration) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.aiConfiguration = aiConfiguration;
    }

    @Override
    public Credential extractCredential(byte[] fileBytes, String fileName) {
        logger.debug("Extracting credential from file: {}, size: {} bytes", fileName, fileBytes.length);
        
        if (aiConfiguration.isMockMode()) {
            logger.info("Mock mode enabled, returning mock credential");
            return createMockCredential();
        }
        
        String apiKey = aiConfiguration.getOpenai().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new InvalidConfigurationException(
                "OpenAI API key is not configured. Set OPENAI_API_KEY environment variable or enable mock mode."
            );
        }
        
        try {
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);
            String mimeType = detectMimeType(fileName);
            logger.debug("Detected MIME type: {} for file: {}", mimeType, fileName);
            
            Map<String, Object> requestBody = buildRequestBody(base64Image, mimeType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            logger.info("Sending request to OpenAI API for file: {}", fileName);
            ResponseEntity<String> response = restTemplate.exchange(
                aiConfiguration.getOpenai().getApiUrl(),
                HttpMethod.POST,
                request,
                String.class
            );
            
            logger.debug("Received response from OpenAI API: {}", response.getStatusCode());
            return parseResponse(response.getBody());
            
        } catch (InvalidConfigurationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to extract credential from file: {}", fileName, e);
            throw new CredentialExtractionException(
                "Failed to extract credential from file: " + e.getMessage(),
                e
            );
        }
    }
    
    private Map<String, Object> buildRequestBody(String base64Image, String mimeType) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        
        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", "data:" + mimeType + ";base64," + base64Image);
        
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", EXTRACTION_PROMPT);
        
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", imageUrl);
        
        message.put("content", List.of(textContent, imageContent));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfiguration.getOpenai().getModel());
        requestBody.put("messages", List.of(message));
        requestBody.put("max_tokens", aiConfiguration.getOpenai().getMaxTokens());
        requestBody.put("response_format", Map.of("type", "json_object"));
        
        return requestBody;
    }
    
    private String detectMimeType(String fileName) {
        if (fileName == null) {
            return "image/png";
        }
        
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        
        return "image/png";
    }
    
    private Credential parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                throw new CredentialExtractionException("Invalid response from OpenAI API: no choices");
            }
            
            JsonNode message = choices.get(0).get("message");
            if (message == null) {
                throw new CredentialExtractionException("Invalid response from OpenAI API: no message");
            }
            
            JsonNode content = message.get("content");
            if (content == null) {
                throw new CredentialExtractionException("Invalid response from OpenAI API: no content");
            }
            
            String jsonContent = content.asText();
            JsonNode credentialJson = objectMapper.readTree(jsonContent);
            
            return mapToCredential(credentialJson);
        } catch (CredentialExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new CredentialExtractionException("Failed to parse OpenAI response", e);
        }
    }
    
    private Credential mapToCredential(JsonNode json) {
        try {
            String id = json.get("id").asText();
            String type = json.get("type").asText();
            
            JsonNode issuerNode = json.get("issuer");
            Issuer issuer = new Issuer(
                issuerNode.get("id").asText(),
                issuerNode.get("displayName").asText(),
                issuerNode.has("trusted") && issuerNode.get("trusted").asBoolean()
            );
            
            String subject = json.get("subject").asText();
            Instant issuedAt = parseDateOrInstant(json.get("issuedAt").asText());
            
            Instant expiresAt = null;
            if (json.has("expiresAt") && !json.get("expiresAt").isNull()) {
                expiresAt = parseDateOrInstant(json.get("expiresAt").asText());
            }
            
            Map<String, Object> claims = new HashMap<>();
            if (json.has("claims") && json.get("claims").isObject()) {
                JsonNode claimsNode = json.get("claims");
                claimsNode.fields().forEachRemaining(entry -> 
                    claims.put(entry.getKey(), entry.getValue().asText())
                );
            }
            
            return new Credential(id, type, issuer, subject, issuedAt, expiresAt, claims);
        } catch (Exception e) {
            throw new CredentialExtractionException("Failed to map JSON to Credential", e);
        }
    }
    
    private Credential createMockCredential() {
        return new Credential(
            "mock-credential-123",
            "VerifiableCredential",
            new Issuer("did:example:issuer", "Mock Issuer", true),
            "did:example:subject",
            Instant.now(),
            Instant.now().plusSeconds(86400 * 365),
            Map.of("degree", "Bachelor of Science", "university", "Example University")
        );
    }

    private Instant parseDateOrInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            try {
                LocalDate date = LocalDate.parse(value);
                return date.atStartOfDay().toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid date/time format: " + value);
            }
        }
    }
}

