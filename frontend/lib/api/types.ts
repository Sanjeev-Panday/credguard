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

// New types for credential issuance

export enum DocumentType {
  PASSPORT = 'PASSPORT',
  DRIVERS_LICENSE = 'DRIVERS_LICENSE',
  DEGREE_CERTIFICATE = 'DEGREE_CERTIFICATE',
  BIRTH_CERTIFICATE = 'BIRTH_CERTIFICATE',
  OTHER = 'OTHER'
}

export interface DocumentInfo {
  id: string;
  documentType: string;
  fileName: string;
  status: string;
  extractedAttributes: Record<string, unknown>;
  uploadedAt: string;
}

export interface CredentialInfo {
  id: string;
  context: string[];
  type: string[];
  issuer: string;
  credentialSubject: Record<string, unknown>;
  issuanceDate: string;
  expirationDate: string | null;
  status: string;
}

export interface IssuanceInfo {
  credentialExchangeId: string;
  offerUrl: string | null;
  connectionId: string;
  walletDid: string;
  processingTimeMs: number;
  processedAt: string;
}

export interface CredentialIssuanceResponse {
  success: boolean;
  message: string;
  document?: DocumentInfo;
  credential?: CredentialInfo;
  issuance?: IssuanceInfo;
}

export interface CredentialIssuanceRequest {
  documentType: DocumentType;
  walletDid: string;
  previewOnly?: boolean;
}

export interface CredentialStatusResponse {
  credentialId: string | null;
  exchangeId: string;
  status: string;
  message: string;
  active: boolean;
}

