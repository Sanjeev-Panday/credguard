package com.credguard.config;

import com.credguard.exception.InvalidConfigurationException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Validated
public class AIConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(AIConfiguration.class);
    
    private OpenAI openai = new OpenAI();
    private boolean mockMode = false;
    
    @PostConstruct
    public void validateConfiguration() {
        if (!mockMode && (openai.getApiKey() == null || openai.getApiKey().isBlank())) {
            logger.warn("OpenAI API key is not configured. Set OPENAI_API_KEY environment variable " +
                       "or ai.openai.api-key property, or enable mock mode with ai.mock-mode=true");
        }
        
        logger.info("AI Configuration initialized - Mock Mode: {}", mockMode);
    }
    
    public OpenAI getOpenai() {
        return openai;
    }
    
    public void setOpenai(OpenAI openai) {
        this.openai = openai;
    }
    
    public boolean isMockMode() {
        return mockMode;
    }
    
    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }
    
    public static class OpenAI {
        private String apiKey;
        private String model = "gpt-4o";
        private String apiUrl = "https://api.openai.com/v1/chat/completions";
        private int maxTokens = 2000;
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
        
        public String getApiUrl() {
            return apiUrl;
        }
        
        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
        
        public int getMaxTokens() {
            return maxTokens;
        }
        
        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
    }
}
