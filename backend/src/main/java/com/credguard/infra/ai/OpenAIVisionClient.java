package com.credguard.infra.ai;

import com.credguard.domain.Credential;
import com.credguard.domain.Issuer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenAI Vision API client implementation for extracting credentials from files.
 * <p>
 * This implementation uses OpenAI's Vision API to analyze images/PDFs and extract
 * structured credential information. It maps the AI response to the Credential domain model.
 */
@Component
public class OpenAIVisionClient implements AIVisionClient {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final boolean mockMode;

    /**
     * Constructor for dependency injection.
     *
     * @param apiKey the OpenAI API key from environment
     * @param mockMode whether to use mock mode (returns sample credential)
     */
    public OpenAIVisionClient(
            @Value("${ai.openai.api-key:}") String apiKey,
            @Value("${ai.mock-mode:false}") boolean mockMode
    ) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.mockMode = mockMode;
    }

    @Override
    public Credential extractCredential(byte[] fileBytes, String fileName) throws CredentialExtractionException {
        if (mockMode) {
            return createMockCredential();
        }
        
        if (apiKey == null || apiKey.isBlank()) {
            throw new CredentialExtractionException(
                "OpenAI API key is not configured. Set OPENAI_API_KEY environment variable or ai.openai.api-key property."
            );
        }
        
        try {
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);
            String mimeType = detectMimeType(fileName);
            
            String prompt = buildExtractionPrompt();
            Map<String, Object> requestBody = buildRequestBody(base64Image, mimeType, prompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            return parseResponse(response.getBody());
            
        } catch (Exception e) {
            throw new CredentialExtractionException(
                "Failed to extract credential from file: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Builds the extraction prompt for the AI.
     */
    private String buildExtractionPrompt() {
        return """
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
    }
    
    /**
     * Builds the request body for OpenAI API.
     */
    private Map<String, Object> buildRequestBody(String base64Image, String mimeType, String prompt) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        
        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", "data:" + mimeType + ";base64," + base64Image);
        
        Map<String, Object> content = new HashMap<>();
        content.put("type", "text");
        content.put("text", prompt);
        
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", imageUrl);
        
        message.put("content", java.util.List.of(content, imageContent));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("messages", java.util.List.of(message));
        requestBody.put("max_tokens", 2000);
        requestBody.put("response_format", Map.of("type", "json_object"));
        
        return requestBody;
    }
    
    /**
     * Detects MIME type from file name.
     */
    private String detectMimeType(String fileName) {
        if (fileName == null) {
            return "image/png";
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/png"; // default
    }
    
    /**
     * Parses the OpenAI API response and converts it to a Credential.
     */
    private Credential parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.get("choices");
        if (choices == null || !choices.isArray() || choices.size() == 0) {
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
    }
    
    /**
     * Maps JSON response to Credential domain object.
     */
    private Credential mapToCredential(JsonNode json) throws Exception {
        String id = json.get("id").asText();
        String type = json.get("type").asText();
        
        JsonNode issuerNode = json.get("issuer");
        Issuer issuer = new Issuer(
            issuerNode.get("id").asText(),
            issuerNode.get("displayName").asText(),
            issuerNode.has("trusted") && issuerNode.get("trusted").asBoolean()
        );
        
        String subject = json.get("subject").asText();
        Instant issuedAt = Instant.parse(json.get("issuedAt").asText());
        
        Instant expiresAt = null;
        if (json.has("expiresAt") && !json.get("expiresAt").isNull()) {
            expiresAt = Instant.parse(json.get("expiresAt").asText());
        }
        
        Map<String, Object> claims = new HashMap<>();
        if (json.has("claims") && json.get("claims").isObject()) {
            JsonNode claimsNode = json.get("claims");
            claimsNode.fields().forEachRemaining(entry -> {
                claims.put(entry.getKey(), entry.getValue().asText());
            });
        }
        
        return new Credential(id, type, issuer, subject, issuedAt, expiresAt, claims);
    }
    
    /**
     * Creates a mock credential for testing when mock mode is enabled.
     */
    private Credential createMockCredential() {
        return new Credential(
            "mock-credential-123",
            "VerifiableCredential",
            new Issuer("did:example:issuer", "Mock Issuer", true),
            "did:example:subject",
            Instant.now(),
            Instant.now().plusSeconds(86400 * 365), // 1 year from now
            Map.of("degree", "Bachelor of Science", "university", "Example University")
        );
    }
}

