/**
 * Type definitions for CredGuard API
 */

export interface Issuer {
  id: string;
  displayName: string;
  trusted: boolean;
}

export interface Credential {
  id: string;
  type: string;
  issuer: Issuer;
  subject: string;
  issuedAt: string;
  expiresAt: string | null;
  claims: Record<string, unknown>;
}

export interface VerificationResponse {
  valid: boolean;
  issuerTrusted: boolean;
  signatureValid: boolean;
  notExpired: boolean;
  errors: string[];
  warnings: string[];
  explanation: string;
  credential: Credential | null;
}

export interface VerificationRequest {
  id: string;
  type: string;
  issuer: {
    id: string;
    displayName: string;
    trusted: boolean;
  };
  subject: string;
  issuedAt: string;
  expiresAt: string | null;
  claims: Record<string, unknown>;
}

