'use client';

import { useState } from 'react';
import FileUpload from '@/components/FileUpload';
import VerificationResult from '@/components/VerificationResult';
import { CredentialIssuanceForm } from '@/components/CredentialIssuanceForm';
import { uploadAndVerify } from '@/lib/api/client';
import { VerificationResponse } from '@/lib/api/types';

type TabType = 'verify' | 'issue';

export default function Home() {
  const [activeTab, setActiveTab] = useState<TabType>('verify');
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
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2">
            CredGuard
          </h1>
          <p className="text-lg text-gray-600 dark:text-gray-400">
            AI-Powered Digital Credential Verification & Issuance
          </p>
        </div>

        {/* Tab Navigation */}
        <div className="mb-8">
          <div className="border-b border-gray-200 dark:border-gray-700">
            <nav className="-mb-px flex space-x-8" aria-label="Tabs">
              <button
                onClick={() => setActiveTab('verify')}
                className={`
                  py-2 px-1 border-b-2 font-medium text-sm
                  ${activeTab === 'verify'
                    ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300'
                  }
                `}
              >
                Verify Credentials
              </button>
              <button
                onClick={() => setActiveTab('issue')}
                className={`
                  py-2 px-1 border-b-2 font-medium text-sm
                  ${activeTab === 'issue'
                    ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300'
                  }
                `}
              >
                Issue Credentials
              </button>
            </nav>
          </div>
        </div>

        {/* Tab Content */}
        {activeTab === 'verify' ? (
          <div>
            {/* File Upload Section */}
            <div className="mb-8">
              <div className="text-center mb-6">
                <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-2">
                  Verify Digital Credentials
                </h2>
                <p className="text-gray-600 dark:text-gray-400">
                  Upload a credential file to verify its authenticity and validate issuer trust
                </p>
              </div>
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
        ) : (
          <div>
            {/* Credential Issuance Form */}
            <CredentialIssuanceForm />
          </div>
        )}
      </div>
    </main>
  );
}
