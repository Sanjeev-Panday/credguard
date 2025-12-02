/**
 * API client for CredGuard backend
 */

import { 
  VerificationRequest, 
  VerificationResponse, 
  CredentialIssuanceRequest,
  CredentialIssuanceResponse,
  CredentialStatusResponse,
  DocumentType
} from './types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Verifies a credential from JSON
 */
export async function verifyCredential(
  request: VerificationRequest
): Promise<VerificationResponse> {
  const response = await fetch(`${API_BASE_URL}/api/credentials/verify`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Unknown error' }));
    throw new Error(error.message || `HTTP error! status: ${response.status}`);
  }

  return response.json();
}

/**
 * Uploads a file and verifies the extracted credential
 */
export async function uploadAndVerify(
  file: File
): Promise<VerificationResponse> {
  // Validate input early to avoid silent server-side errors
  if (!file) {
    throw new Error('No file provided to uploadAndVerify');
  }
  if (!(file instanceof File)) {
    // In some test environments the File constructor may differ; perform duck-typing as fallback
    const maybeFile = file as any;
    if (!maybeFile || typeof maybeFile.name !== 'string' || typeof maybeFile.size !== 'number') {
      throw new Error('Invalid file provided to uploadAndVerify');
    }
  }

  const formData = new FormData();
  // Provide the filename explicitly to ensure multipart/form-data part is recognized as a file
  formData.append('file', file, file.name);

  // Debugging info to help trace why backend may report missing 'file' part
  console.debug('uploadAndVerify: sending file', { name: file.name, size: file.size, type: file.type });

  const response = await fetch(`${API_BASE_URL}/api/credentials/upload`, {
    method: 'POST',
    body: formData,
    // Do NOT set Content-Type here; browser will add the correct multipart boundary.
    headers: {
      'Accept': 'application/json',
    },
  });

  console.debug('uploadAndVerify: response status', response.status, 'ok=', response.ok);

  if (response.ok) {
    return response.json();
  }

  // Try to parse a helpful error message from JSON body, otherwise fall back to text/status
  const contentType = response.headers.get('content-type') || '';
  let errorMessage = `HTTP error! status: ${response.status}`;

  if (contentType.includes('application/json')) {
    const errorBody = await response.json().catch(() => null);
    if (errorBody) {
      // If backend returns the VerificationResponse shape with an errors array, join them
      if (typeof errorBody === 'object') {
        if (Array.isArray((errorBody as any).errors) && (errorBody as any).errors.length > 0) {
          errorMessage = (errorBody as any).errors.join('; ');
        } else if ((errorBody as any).message) {
          errorMessage = (errorBody as any).message;
        } else {
          errorMessage = JSON.stringify(errorBody);
        }
      } else {
        errorMessage = String(errorBody);
      }
    }
  } else {
    const text = await response.text().catch(() => null);
    if (text) {
      errorMessage = text;
    }
  }

  throw new Error(errorMessage);
}

// New functions for credential issuance

/**
 * Issues a verifiable credential from an uploaded physical document
 */
export async function issueCredentialFromDocument(
  file: File,
  request: CredentialIssuanceRequest
): Promise<CredentialIssuanceResponse> {
  if (!file) {
    throw new Error('No file provided');
  }

  const formData = new FormData();
  formData.append('file', file, file.name);
  formData.append('documentType', request.documentType);
  formData.append('walletDid', request.walletDid);
  if (request.previewOnly !== undefined) {
    formData.append('previewOnly', String(request.previewOnly));
  }

  console.debug('issueCredentialFromDocument: sending file', { 
    name: file.name, 
    size: file.size, 
    type: file.type,
    documentType: request.documentType,
    walletDid: request.walletDid,
    previewOnly: request.previewOnly
  });

  const response = await fetch(`${API_BASE_URL}/api/credentials/issuance/issue-from-document`, {
    method: 'POST',
    body: formData,
    headers: {
      'Accept': 'application/json',
    },
  });

  if (response.ok) {
    return response.json();
  }

  const errorMessage = await extractErrorMessage(response);
  throw new Error(errorMessage);
}

/**
 * Issues a verifiable credential asynchronously
 */
export async function issueCredentialFromDocumentAsync(
  file: File,
  request: CredentialIssuanceRequest
): Promise<{ jobId: string }> {
  if (!file) {
    throw new Error('No file provided');
  }

  const formData = new FormData();
  formData.append('file', file, file.name);
  formData.append('documentType', request.documentType);
  formData.append('walletDid', request.walletDid);

  const response = await fetch(`${API_BASE_URL}/api/credentials/issuance/issue-from-document/async`, {
    method: 'POST',
    body: formData,
    headers: {
      'Accept': 'application/json',
    },
  });

  if (response.ok) {
    const text = await response.text();
    // Extract job ID from response text (format: "Credential issuance started. Job ID: job-123")
    const jobIdMatch = text.match(/Job ID: (.+)/);
    const jobId = jobIdMatch ? jobIdMatch[1] : text;
    return { jobId };
  }

  const errorMessage = await extractErrorMessage(response);
  throw new Error(errorMessage);
}

/**
 * Checks the status of a credential exchange
 */
export async function getCredentialStatus(
  credentialExchangeId: string
): Promise<CredentialStatusResponse> {
  const response = await fetch(`${API_BASE_URL}/api/credentials/issuance/status/${credentialExchangeId}`, {
    method: 'GET',
    headers: {
      'Accept': 'application/json',
    },
  });

  if (response.ok) {
    return response.json();
  }

  const errorMessage = await extractErrorMessage(response);
  throw new Error(errorMessage);
}

/**
 * Revokes a previously issued credential
 */
export async function revokeCredential(credentialId: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/credentials/issuance/revoke/${credentialId}`, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
    },
  });

  if (!response.ok) {
    const errorMessage = await extractErrorMessage(response);
    throw new Error(errorMessage);
  }
}

/**
 * Gets the connection status with a wallet
 */
export async function getConnectionStatus(connectionId: string): Promise<string> {
  const response = await fetch(`${API_BASE_URL}/api/credentials/issuance/connection/${connectionId}/status`, {
    method: 'GET',
    headers: {
      'Accept': 'application/json',
    },
  });

  if (response.ok) {
    return response.text();
  }

  const errorMessage = await extractErrorMessage(response);
  throw new Error(errorMessage);
}

/**
 * Health check endpoint
 */
export async function checkHealth(): Promise<{ status: string; service: string; version: string }> {
  const response = await fetch(`${API_BASE_URL}/health`);
  return response.json();
}

/**
 * Helper function to extract error messages from response
 */
async function extractErrorMessage(response: Response): Promise<string> {
  const contentType = response.headers.get('content-type') || '';
  let errorMessage = `HTTP error! status: ${response.status}`;

  if (contentType.includes('application/json')) {
    const errorBody = await response.json().catch(() => null);
    if (errorBody) {
      if (typeof errorBody === 'object') {
        if (Array.isArray((errorBody as any).errors) && (errorBody as any).errors.length > 0) {
          errorMessage = (errorBody as any).errors.join('; ');
        } else if ((errorBody as any).message) {
          errorMessage = (errorBody as any).message;
        } else {
          errorMessage = JSON.stringify(errorBody);
        }
      } else {
        errorMessage = String(errorBody);
      }
    }
  } else {
    const text = await response.text().catch(() => null);
    if (text) {
      errorMessage = text;
    }
  }

  return errorMessage;
}

