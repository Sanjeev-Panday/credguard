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
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${API_BASE_URL}/api/credentials/upload`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Unknown error' }));
    throw new Error(error.message || `HTTP error! status: ${response.status}`);
  }

  return response.json();
}

/**
 * Health check endpoint
 */
export async function checkHealth(): Promise<{ status: string; service: string; version: string }> {
  const response = await fetch(`${API_BASE_URL}/health`);
  return response.json();
}

