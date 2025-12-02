package com.credguard.infra.aries;

import com.credguard.config.AriesConfiguration;
import com.credguard.domain.VerifiableCredential;
import com.credguard.domain.CredentialIssuanceResult;
import com.credguard.exception.CredentialIssuanceException;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of Aries Cloud Agent client for issuing verifiable credentials.
 * Integrates with Aries Cloud Agent API to manage connections and credential issuance.
 */
@Component
public class AriesCloudAgentClientImpl implements AriesCloudAgentClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AriesCloudAgentClientImpl.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AriesConfiguration ariesConfiguration;

    public AriesCloudAgentClientImpl(AriesConfiguration ariesConfiguration) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.ariesConfiguration = ariesConfiguration;
    }

    @Override
    public String createConnectionInvitation(String walletDid) {
        logger.info("Creating connection invitation for wallet DID: {}", walletDid);
        
        if (ariesConfiguration.isMockMode()) {
            return createMockConnectionId();
        }
        
        try {
            String endpoint = ariesConfiguration.getAgentUrl() + "/connections/create-invitation";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("my_label", "CredGuard Identity Issuer");
            requestBody.put("accept", "auto");
            
            HttpEntity<Map<String, Object>> request = createAuthorizedRequest(requestBody);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                request,
                String.class
            );
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String connectionId = responseJson.get("connection_id").asText();
            
            logger.info("Created connection invitation with ID: {}", connectionId);
            return connectionId;
            
        } catch (Exception e) {
            logger.error("Failed to create connection invitation", e);
            throw new CredentialIssuanceException("Failed to create connection invitation: " + e.getMessage(), e);
        }
    }

    @Override
    public CredentialIssuanceResult issueCredential(VerifiableCredential credential) {
        logger.info("Issuing credential: {} to wallet: {}", credential.id(), credential.walletDid());
        
        long startTime = System.currentTimeMillis();
        
        try {
            if (ariesConfiguration.isMockMode()) {
                return createMockIssuanceResult(credential, System.currentTimeMillis() - startTime);
            }
            
            // Step 1: Send credential offer
            String credentialExchangeId = sendCredentialOffer(credential);
            
            // Step 2: Wait for offer acceptance (in real implementation, this would be async)
            // For now, we'll simulate immediate acceptance
            
            // Step 3: Issue the credential
            String endpoint = ariesConfiguration.getAgentUrl() + 
                "/issue-credential-2.0/records/" + credentialExchangeId + "/send-credential";
            
            Map<String, Object> requestBody = buildCredentialPayload(credential);
            HttpEntity<Map<String, Object>> request = createAuthorizedRequest(requestBody);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                request,
                String.class
            );
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String offerUrl = responseJson.has("offer_url") ? responseJson.get("offer_url").asText() : null;
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            logger.info("Successfully issued credential: {} in {}ms", credential.id(), processingTime);
            
            return CredentialIssuanceResult.success(
                credential.withStatus(VerifiableCredential.IssuanceStatus.ISSUED, "Credential successfully issued"),
                credentialExchangeId,
                offerUrl,
                processingTime
            );
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to issue credential: {}", credential.id(), e);
            
            return CredentialIssuanceResult.failure(
                credential.withStatus(VerifiableCredential.IssuanceStatus.FAILED, e.getMessage()),
                e.getMessage(),
                processingTime
            );
        }
    }

    @Override
    public String sendCredentialOffer(VerifiableCredential credential) {
        logger.info("Sending credential offer for: {}", credential.id());
        
        if (ariesConfiguration.isMockMode()) {
            return "mock-exchange-" + UUID.randomUUID().toString();
        }
        
        try {
            String endpoint = ariesConfiguration.getAgentUrl() + "/issue-credential-2.0/send-offer";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("connection_id", credential.connectionId());
            requestBody.put("auto_issue", true);
            requestBody.put("auto_remove", false);
            requestBody.put("credential_definition_id", ariesConfiguration.getCredentialDefinitionId());
            requestBody.put("credential_proposal", buildCredentialProposal(credential));
            
            HttpEntity<Map<String, Object>> request = createAuthorizedRequest(requestBody);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                request,
                String.class
            );
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String exchangeId = responseJson.get("credential_exchange_id").asText();
            
            logger.info("Sent credential offer with exchange ID: {}", exchangeId);
            return exchangeId;
            
        } catch (Exception e) {
            logger.error("Failed to send credential offer", e);
            throw new CredentialIssuanceException("Failed to send credential offer: " + e.getMessage(), e);
        }
    }

    @Override
    public String getCredentialExchangeStatus(String credentialExchangeId) {
        logger.debug("Checking status for credential exchange: {}", credentialExchangeId);
        
        if (ariesConfiguration.isMockMode()) {
            return "credential_acked"; // Mock successful status
        }
        
        try {
            String endpoint = ariesConfiguration.getAgentUrl() + 
                "/issue-credential-2.0/records/" + credentialExchangeId;
            
            HttpEntity<?> request = createAuthorizedRequest(null);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                request,
                String.class
            );
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.get("state").asText();
            
        } catch (Exception e) {
            logger.error("Failed to get credential exchange status", e);
            return "error";
        }
    }

    @Override
    public boolean revokeCredential(String credentialId) {
        logger.info("Revoking credential: {}", credentialId);
        
        if (ariesConfiguration.isMockMode()) {
            logger.info("Mock mode: credential {} marked as revoked", credentialId);
            return true;
        }
        
        try {
            String endpoint = ariesConfiguration.getAgentUrl() + "/revocation/revoke";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("credential_id", credentialId);
            requestBody.put("publish", true);
            
            HttpEntity<Map<String, Object>> request = createAuthorizedRequest(requestBody);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                request,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            logger.info("Credential {} revocation {}", credentialId, success ? "successful" : "failed");
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to revoke credential: {}", credentialId, e);
            return false;
        }
    }

    @Override
    public String getConnectionStatus(String connectionId) {
        logger.debug("Checking connection status: {}", connectionId);
        
        if (ariesConfiguration.isMockMode()) {
            return "active"; // Mock active connection
        }
        
        try {
            String endpoint = ariesConfiguration.getAgentUrl() + "/connections/" + connectionId;
            
            HttpEntity<?> request = createAuthorizedRequest(null);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                request,
                String.class
            );
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.get("state").asText();
            
        } catch (Exception e) {
            logger.error("Failed to get connection status", e);
            return "error";
        }
    }
    
    private HttpEntity<Map<String, Object>> createAuthorizedRequest(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (ariesConfiguration.getApiKey() != null && !ariesConfiguration.getApiKey().isBlank()) {
            headers.set("X-API-Key", ariesConfiguration.getApiKey());
        }
        
        return new HttpEntity<>(body, headers);
    }
    
    private Map<String, Object> buildCredentialPayload(VerifiableCredential credential) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("credential", Map.of(
            "@context", credential.context(),
            "type", credential.type(),
            "issuer", credential.issuer(),
            "credentialSubject", credential.credentialSubject(),
            "issuanceDate", credential.issuanceDate().toString(),
            "expirationDate", credential.expirationDate() != null ? credential.expirationDate().toString() : null
        ));
        return payload;
    }
    
    private Map<String, Object> buildCredentialProposal(VerifiableCredential credential) {
        Map<String, Object> proposal = new HashMap<>();
        proposal.put("@type", "issue-credential/2.0/credential-preview");
        proposal.put("attributes", credential.credentialSubject());
        return proposal;
    }
    
    private String createMockConnectionId() {
        return "mock-conn-" + UUID.randomUUID().toString();
    }
    
    private CredentialIssuanceResult createMockIssuanceResult(VerifiableCredential credential, long processingTime) {
        String mockExchangeId = "mock-exchange-" + UUID.randomUUID().toString();
        String mockOfferUrl = "https://mock-agent.example.com/offer/" + mockExchangeId;
        
        return CredentialIssuanceResult.success(
            credential.withStatus(VerifiableCredential.IssuanceStatus.ISSUED, "Mock issuance successful"),
            mockExchangeId,
            mockOfferUrl,
            processingTime
        );
    }
}