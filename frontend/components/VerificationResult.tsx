'use client';

import { VerificationResponse } from '@/lib/api/types';
import CredentialViewer from './CredentialViewer';

interface VerificationResultProps {
  result: VerificationResponse;
}

export default function VerificationResult({ result }: VerificationResultProps) {
  const isValid = result.valid;
  const statusColor = isValid
    ? 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200'
    : 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200';

  return (
    <div className="space-y-6">
      {/* Status Card */}
      <div className={`rounded-lg p-6 ${statusColor}`}>
        <div className="flex items-center space-x-3">
          {isValid ? (
            <svg
              className="w-8 h-8"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          ) : (
            <svg
              className="w-8 h-8"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          )}
          <div>
            <h3 className="text-xl font-bold">
              {isValid ? 'Credential Verified' : 'Verification Failed'}
            </h3>
            <p className="text-sm mt-1">{result.explanation}</p>
          </div>
        </div>
      </div>

      {/* Validation Details */}
      <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm border border-gray-200 dark:border-gray-700">
        <h4 className="text-lg font-semibold mb-4 text-gray-900 dark:text-gray-100">
          Validation Details
        </h4>
        <div className="grid grid-cols-2 gap-4">
          <ValidationCheck
            label="Issuer Trusted"
            passed={result.issuerTrusted}
          />
          <ValidationCheck
            label="Signature Valid"
            passed={result.signatureValid}
          />
          <ValidationCheck
            label="Not Expired"
            passed={result.notExpired}
          />
          <ValidationCheck
            label="Overall Status"
            passed={result.valid}
          />
        </div>
      </div>

      {/* Errors */}
      {result.errors.length > 0 && (
        <div className="bg-red-50 dark:bg-red-900/20 rounded-lg p-6 border border-red-200 dark:border-red-800">
          <h4 className="text-lg font-semibold mb-3 text-red-900 dark:text-red-200">
            Errors
          </h4>
          <ul className="list-disc list-inside space-y-1 text-red-800 dark:text-red-300">
            {result.errors.map((error, index) => (
              <li key={index}>{error}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Warnings */}
      {result.warnings.length > 0 && (
        <div className="bg-yellow-50 dark:bg-yellow-900/20 rounded-lg p-6 border border-yellow-200 dark:border-yellow-800">
          <h4 className="text-lg font-semibold mb-3 text-yellow-900 dark:text-yellow-200">
            Warnings
          </h4>
          <ul className="list-disc list-inside space-y-1 text-yellow-800 dark:text-yellow-300">
            {result.warnings.map((warning, index) => (
              <li key={index}>{warning}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Credential Details */}
      {result.credential && (
        <CredentialViewer credential={result.credential} />
      )}
    </div>
  );
}

function ValidationCheck({ label, passed }: { label: string; passed: boolean }) {
  return (
    <div className="flex items-center space-x-2">
      {passed ? (
        <svg
          className="w-5 h-5 text-green-500"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M5 13l4 4L19 7"
          />
        </svg>
      ) : (
        <svg
          className="w-5 h-5 text-red-500"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M6 18L18 6M6 6l12 12"
          />
        </svg>
      )}
      <span className="text-gray-700 dark:text-gray-300">{label}</span>
    </div>
  );
}

