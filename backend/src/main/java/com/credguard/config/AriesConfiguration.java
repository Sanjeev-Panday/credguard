package com.credguard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Aries Cloud Agent integration.
 */
@Configuration
@ConfigurationProperties(prefix = "aries")
public class AriesConfiguration {
    
    private boolean mockMode = false;
    private String agentUrl = "http://localhost:8040";
    private String apiKey;
    private String walletId = "credguard-wallet";
    private String walletKey = "credguard-key-123";
    private String credentialDefinitionId;
    private String schemaId;
    private Connection connection = new Connection();
    
    // Getters and setters
    
    public boolean isMockMode() {
        return mockMode;
    }
    
    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }
    
    public String getAgentUrl() {
        return agentUrl;
    }
    
    public void setAgentUrl(String agentUrl) {
        this.agentUrl = agentUrl;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getWalletId() {
        return walletId;
    }
    
    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }
    
    public String getWalletKey() {
        return walletKey;
    }
    
    public void setWalletKey(String walletKey) {
        this.walletKey = walletKey;
    }
    
    public String getCredentialDefinitionId() {
        return credentialDefinitionId;
    }
    
    public void setCredentialDefinitionId(String credentialDefinitionId) {
        this.credentialDefinitionId = credentialDefinitionId;
    }
    
    public String getSchemaId() {
        return schemaId;
    }
    
    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Connection configuration for Aries agent.
     */
    public static class Connection {
        private int timeout = 30000; // 30 seconds
        private int retries = 3;
        private boolean autoAccept = true;
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
        
        public int getRetries() {
            return retries;
        }
        
        public void setRetries(int retries) {
            this.retries = retries;
        }
        
        public boolean isAutoAccept() {
            return autoAccept;
        }
        
        public void setAutoAccept(boolean autoAccept) {
            this.autoAccept = autoAccept;
        }
    }
}