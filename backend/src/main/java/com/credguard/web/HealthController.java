package com.credguard.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public Map<String, String> getHealth() {
        return Map.of("status", "OK",
            "service", "credguard-backend",
            "version", "1.0.0"
            );
        }            
}
