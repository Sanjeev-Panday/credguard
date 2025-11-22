'use client';

import { useState } from 'react';
import FileUpload from '@/components/FileUpload';
import VerificationResult from '@/components/VerificationResult';
import { uploadAndVerify } from '@/lib/api/client';
import { VerificationResponse } from '@/lib/api/types';

export default function Home() {
  const [result, setResult] = useState<VerificationResponse | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleFileSelect = async (file: File) => {
    setIsUploading(true);
    setError(null);
    setResult(null);

    try {
      const verificationResult = await uploadAndVerify(file);
      setResult(verificationResult);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <main className="min-h-screen py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2">
            CredGuard
          </h1>
          <p className="text-lg text-gray-600 dark:text-gray-400">
            AI-Powered Digital Credential Verification
          </p>
        </div>

        {/* File Upload Section */}
        <div className="mb-8">
          <FileUpload onFileSelect={handleFileSelect} isUploading={isUploading} />
        </div>

        {/* Error Display */}
        {error && (
          <div className="mb-8 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-6">
            <div className="flex items-center space-x-3">
              <svg
                className="w-6 h-6 text-red-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <div>
                <h3 className="text-lg font-semibold text-red-900 dark:text-red-200">
                  Error
                </h3>
                <p className="text-red-800 dark:text-red-300 mt-1">{error}</p>
              </div>
            </div>
          </div>
        )}

        {/* Verification Result */}
        {result && <VerificationResult result={result} />}

        {/* Footer Info */}
        {!result && !error && (
          <div className="mt-12 text-center text-sm text-gray-500 dark:text-gray-400">
            <p>Upload a credential file (PDF, image) to verify its authenticity.</p>
            <p className="mt-2">
              The system will extract credential information using AI and validate
              issuer trust, expiration, and signature.
            </p>
          </div>
        )}
      </div>
    </main>
  );
}
