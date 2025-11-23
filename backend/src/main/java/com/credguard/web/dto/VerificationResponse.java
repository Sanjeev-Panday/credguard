package com.credguard.web.dto;

import com.credguard.domain.Credential;
import com.credguard.domain.VerificationResult;

import java.util.List;
import java.util.Map;

public record VerificationResponse(
        boolean valid,
        boolean issuerTrusted,
        boolean signatureValid,
        boolean notExpired,
        List<String> errors,
        List<String> warnings,
        String explanation,
        CredentialDto credential
) {
    public static VerificationResponse from(VerificationResult result) {
        CredentialDto credentialDto = result.credential() != null
            ? CredentialDto.from(result.credential())
            : null;
        
        return new VerificationResponse(
            result.valid(),
            result.issuerTrusted(),
            result.signatureValid(),
            result.notExpired(),
            result.errors(),
            result.warnings(),
            result.explanation(),
            credentialDto
        );
    }
    
    public record CredentialDto(
            String id,
            String type,
            IssuerDto issuer,
            String subject,
            String issuedAt,
            String expiresAt,
            Map<String, Object> claims
    ) {
        public static CredentialDto from(Credential credential) {
            return new CredentialDto(
                credential.id(),
                credential.type(),
                new IssuerDto(
                    credential.issuer().id(),
                    credential.issuer().displayName(),
                    credential.issuer().trusted()
                ),
                credential.subject(),
                credential.issuedAt().toString(),
                credential.expiresAt() != null ? credential.expiresAt().toString() : null,
                credential.claims()
            );
        }
        
        public record IssuerDto(
                String id,
                String displayName,
                boolean trusted
        ) {}
    }
}

