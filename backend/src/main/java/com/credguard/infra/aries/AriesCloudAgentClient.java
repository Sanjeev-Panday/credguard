package com.credguard.infra.aries;

import com.credguard.domain.VerifiableCredential;
import com.credguard.domain.CredentialIssuanceResult;

/**
 * Interface for Aries Cloud Agent client operations.
 * Defines the contract for issuing verifiable credentials to mobile wallets via Aries protocol.
 */
public interface AriesCloudAgentClient {
    
    /**
     * Creates a new connection invitation for a mobile wallet.
     * 
     * @param walletDid The DID of the wallet to connect to
     * @return Connection ID for tracking the connection
     */
    String createConnectionInvitation(String walletDid);
    
    /**
     * Issues a verifiable credential to a connected wallet.
     * 
     * @param credential The verifiable credential to issue
     * @return Result of the credential issuance operation
     */
    CredentialIssuanceResult issueCredential(VerifiableCredential credential);
    
    /**
     * Sends a credential offer to the wallet.
     * 
     * @param credential The credential to offer
     * @return Credential exchange ID for tracking
     */
    String sendCredentialOffer(VerifiableCredential credential);
    
    /**
     * Checks the status of a credential exchange.
     * 
     * @param credentialExchangeId The exchange ID to check
     * @return Current status of the exchange
     */
    String getCredentialExchangeStatus(String credentialExchangeId);
    
    /**
     * Revokes a previously issued credential.
     * 
     * @param credentialId The ID of the credential to revoke
     * @return Success status of the revocation
     */
    boolean revokeCredential(String credentialId);
    
    /**
     * Gets the connection status with a wallet.
     * 
     * @param connectionId The connection ID to check
     * @return Connection status
     */
    String getConnectionStatus(String connectionId);
}