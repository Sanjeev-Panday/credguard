package com.credguard.infra.crypto;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;

/**
 * Service for verifying JWT/JWS signatures using Nimbus JOSE.
 * <p>
 * This service handles signature verification for verifiable credentials
 * in JWT format. It supports RSA-based signatures and can fetch
 * public keys from JWK sets.
 */
@Service
public class SignatureVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(SignatureVerificationService.class);

    /**
     * Verifies the signature of a JWT credential.
     * <p>
     * This method attempts to verify the signature of a JWT. If the credential
     * is not in JWT format or signature verification is not applicable,
     * it returns true (graceful degradation).
     *
     * @param jwtString the JWT string to verify
     * @param issuerPublicKeyUrl optional URL to fetch issuer's public key (JWK set)
     * @return true if signature is valid or verification is not applicable
     */
    public boolean verifySignature(String jwtString, String issuerPublicKeyUrl) {
        if (jwtString == null || jwtString.isBlank()) {
            logger.warn("JWT string is null or blank, skipping signature verification");
            return true; // Graceful degradation
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(jwtString);
            JWSHeader header = signedJWT.getHeader();
            
            // Check if it's a signed JWT
            if (header.getAlgorithm() == null) {
                logger.warn("JWT has no algorithm specified, skipping signature verification");
                return true;
            }

            // For now, we'll do a basic structure check
            // Full verification requires issuer's public key
            if (issuerPublicKeyUrl != null && !issuerPublicKeyUrl.isBlank()) {
                return verifyWithPublicKey(signedJWT, issuerPublicKeyUrl);
            }

            // If no public key URL provided, check JWT structure validity
            // In production, you should always verify signatures
            logger.warn("No issuer public key URL provided, performing structure check only");
            return verifyStructure(signedJWT);

        } catch (ParseException e) {
            logger.error("Failed to parse JWT: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error during signature verification: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifies signature using issuer's public key from JWK set.
     */
    private boolean verifyWithPublicKey(SignedJWT signedJWT, String jwkSetUrl) {
        try {
            JWKSet jwkSet = JWKSet.load(URI.create(jwkSetUrl).toURL());
            JWSHeader header = signedJWT.getHeader();
            
            // Find matching key
            JWK jwk = jwkSet.getKeyByKeyId(header.getKeyID());
            if (jwk == null && jwkSet.getKeys().size() == 1) {
                // If only one key and no key ID, use that key
                jwk = jwkSet.getKeys().iterator().next();
            }

            if (jwk == null) {
                logger.error("No matching key found in JWK set");
                return false;
            }

            if (!(jwk instanceof RSAKey)) {
                logger.error("Only RSA keys are currently supported");
                return false;
            }

            RSAKey rsaKey = (RSAKey) jwk;
            JWSVerifier verifier = new RSASSAVerifier(rsaKey);
            
            return signedJWT.verify(verifier);

        } catch (Exception e) {
            logger.error("Failed to verify signature with public key: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifies JWT structure without signature verification.
     * <p>
     * This is a fallback when public keys are not available.
     * In production, signatures should always be verified.
     */
    private boolean verifyStructure(SignedJWT signedJWT) {
        try {
            // Check that JWT can be parsed and has valid structure
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (claims == null) {
                return false;
            }

            // Basic structure validation
            return signedJWT.getHeader() != null && 
                   signedJWT.getSignature() != null &&
                   claims.getIssuer() != null;

        } catch (ParseException e) {
            logger.error("Failed to parse JWT claims: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies signature for a credential that may contain JWT data in claims.
     * <p>
     * This method looks for JWT data in the credential's claims map.
     *
     * @param credentialClaims the credential's claims map
     * @param issuerPublicKeyUrl optional URL to fetch issuer's public key
     * @return true if signature is valid or verification is not applicable
     */
    public boolean verifyCredentialSignature(
            Map<String, Object> credentialClaims,
            String issuerPublicKeyUrl
    ) {
        // Look for JWT in claims
        Object jwtObject = credentialClaims.get("jwt");
        if (jwtObject instanceof String) {
            return verifySignature((String) jwtObject, issuerPublicKeyUrl);
        }

        // If no JWT found, assume signature verification is not applicable
        // (e.g., credential is in a different format)
        logger.debug("No JWT found in credential claims, skipping signature verification");
        return true;
    }
}

