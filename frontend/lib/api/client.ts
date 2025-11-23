/**
 * API client for CredGuard backend
 */

import { VerificationRequest, VerificationResponse } from './types';

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

/**
 * Health check endpoint
 */
export async function checkHealth(): Promise<{ status: string; service: string; version: string }> {
  const response = await fetch(`${API_BASE_URL}/health`);
  return response.json();
}

