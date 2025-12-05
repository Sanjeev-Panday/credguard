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
import java.text.ParseException;
import java.util.Map;

/**
 * Service for verifying JWT/JWS signatures using Nimbus JOSE.
 */
@Service
public class SignatureVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(SignatureVerificationService.class);

    public boolean verifySignature(String jwtString, String issuerPublicKeyUrl) {
        if (jwtString == null || jwtString.isBlank()) {
            logger.debug("JWT string is null or blank, skipping signature verification");
            return true;
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(jwtString);
            JWSHeader header = signedJWT.getHeader();

            if (header.getAlgorithm() == null) {
                logger.debug("JWT has no algorithm specified, skipping signature verification");
                return true;
            }

            if (issuerPublicKeyUrl != null && !issuerPublicKeyUrl.isBlank()) {
                logger.debug("Verifying JWT signature with public key from: {}", issuerPublicKeyUrl);
                return verifyWithPublicKey(signedJWT, issuerPublicKeyUrl);
            }

            logger.debug("No issuer public key URL provided, performing structure check only");
            return verifyStructure(signedJWT);

        } catch (ParseException e) {
            logger.error("Failed to parse JWT: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error during signature verification", e);
            return false;
        }
    }

    private boolean verifyWithPublicKey(SignedJWT signedJWT, String jwkSetUrl) {
        try {
            JWKSet jwkSet = JWKSet.load(URI.create(jwkSetUrl).toURL());
            JWSHeader header = signedJWT.getHeader();

            JWK jwk = jwkSet.getKeyByKeyId(header.getKeyID());
            if (jwk == null && jwkSet.getKeys().size() == 1) {
                jwk = jwkSet.getKeys().iterator().next();
            }

            if (jwk == null) {
                logger.error("No matching key found in JWK set");
                return false;
            }

            // Java 21: Pattern matching for instanceof - combines type check and cast
            if (!(jwk instanceof RSAKey rsaKey)) {
                logger.error("Only RSA keys are currently supported");
                return false;
            }

            JWSVerifier verifier = new RSASSAVerifier(rsaKey);

            boolean verified = signedJWT.verify(verifier);
            logger.debug("JWT signature verification result: {}", verified);
            return verified;

        } catch (Exception e) {
            logger.error("Failed to verify signature with public key from: {}", jwkSetUrl, e);
            return false;
        }
    }

    private boolean verifyStructure(SignedJWT signedJWT) {
        try {
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (claims == null) {
                return false;
            }

            return signedJWT.getHeader() != null &&
                    signedJWT.getSignature() != null &&
                    claims.getIssuer() != null;

        } catch (ParseException e) {
            logger.error("Failed to parse JWT claims: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyCredentialSignature(
            Map<String, Object> credentialClaims,
            String issuerPublicKeyUrl) {
        Object jwtObject = credentialClaims.get("jwt");
        // Java 21: Pattern matching for instanceof - more concise and readable
        if (jwtObject instanceof String jwt) {
            return verifySignature(jwt, issuerPublicKeyUrl);
        }

        logger.debug("No JWT found in credential claims, skipping signature verification");
        return true;
    }
}
