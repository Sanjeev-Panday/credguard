'use client';

import { CredentialIssuanceResponse } from '@/lib/api/types';

interface CredentialIssuanceResultProps {
  result: CredentialIssuanceResponse;
  onReset: () => void;
}

export function CredentialIssuanceResult({ result, onReset }: CredentialIssuanceResultProps) {
  const { success, message, document, credential, issuance } = result;

  return (
    <div className="space-y-6">
      {/* Status Banner */}
      <div
        className={`
          rounded-md p-4 border
          ${success
            ? 'bg-green-50 border-green-200'
            : 'bg-red-50 border-red-200'
          }
        `}
      >
        <div className="flex items-center">
          <div
            className={`
              flex-shrink-0 h-5 w-5 rounded-full flex items-center justify-center text-white text-xs font-bold
              ${success ? 'bg-green-500' : 'bg-red-500'}
            `}
          >
            {success ? '✓' : '✗'}
          </div>
          <div className="ml-3">
            <h3
              className={`
                text-sm font-medium
                ${success ? 'text-green-800' : 'text-red-800'}
              `}
            >
              {success ? 'Credential Processed Successfully' : 'Processing Failed'}
            </h3>
            <p
              className={`
                text-sm mt-1
                ${success ? 'text-green-700' : 'text-red-700'}
              `}
            >
              {message}
            </p>
          </div>
        </div>
      </div>

      {/* Document Information */}
      {document && (
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Document Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Document Type</label>
              <p className="text-sm text-gray-900 mt-1">{document.documentType}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">File Name</label>
              <p className="text-sm text-gray-900 mt-1">{document.fileName}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Processing Status</label>
              <span
                className={`
                  inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium mt-1
                  ${document.status === 'Credential Issued'
                    ? 'bg-green-100 text-green-800'
                    : document.status === 'Attributes Extracted'
                    ? 'bg-blue-100 text-blue-800'
                    : 'bg-gray-100 text-gray-800'
                  }
                `}
              >
                {document.status}
              </span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Uploaded At</label>
              <p className="text-sm text-gray-900 mt-1">
                {new Date(document.uploadedAt).toLocaleString()}
              </p>
            </div>
          </div>

          {/* Extracted Attributes */}
          {document.extractedAttributes && Object.keys(document.extractedAttributes).length > 0 && (
            <div className="mt-6">
              <h4 className="text-sm font-medium text-gray-700 mb-3">Extracted Attributes</h4>
              <div className="bg-gray-50 rounded-md p-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {Object.entries(document.extractedAttributes)
                    .filter(([key]) => key !== 'documentType' && key !== 'extractedAt')
                    .map(([key, value]) => (
                      <div key={key}>
                        <label className="block text-xs font-medium text-gray-600 uppercase tracking-wide">
                          {key.replace(/([A-Z])/g, ' $1').trim()}
                        </label>
                        <p className="text-sm text-gray-900 mt-1">
                          {value ? String(value) : 'Not detected'}
                        </p>
                      </div>
                    ))}
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Credential Information */}
      {credential && (
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Verifiable Credential</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Credential ID</label>
              <p className="text-sm text-gray-900 mt-1 font-mono break-all">{credential.id}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Credential Type</label>
              <p className="text-sm text-gray-900 mt-1">{credential.type.join(', ')}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Issuer</label>
              <p className="text-sm text-gray-900 mt-1">{credential.issuer}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Status</label>
              <span
                className={`
                  inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium mt-1
                  ${credential.status === 'Issued'
                    ? 'bg-green-100 text-green-800'
                    : credential.status === 'Created'
                    ? 'bg-blue-100 text-blue-800'
                    : 'bg-gray-100 text-gray-800'
                  }
                `}
              >
                {credential.status}
              </span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Issuance Date</label>
              <p className="text-sm text-gray-900 mt-1">
                {new Date(credential.issuanceDate).toLocaleString()}
              </p>
            </div>
            {credential.expirationDate && (
              <div>
                <label className="block text-sm font-medium text-gray-700">Expiration Date</label>
                <p className="text-sm text-gray-900 mt-1">
                  {new Date(credential.expirationDate).toLocaleString()}
                </p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Issuance Information (only shown when actually issued) */}
      {issuance && (
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Issuance Details</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Exchange ID</label>
              <p className="text-sm text-gray-900 mt-1 font-mono break-all">{issuance.credentialExchangeId}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Connection ID</label>
              <p className="text-sm text-gray-900 mt-1 font-mono break-all">{issuance.connectionId}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Wallet DID</label>
              <p className="text-sm text-gray-900 mt-1 font-mono break-all">{issuance.walletDid}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Processing Time</label>
              <p className="text-sm text-gray-900 mt-1">{issuance.processingTimeMs} ms</p>
            </div>
            {issuance.offerUrl && (
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700">Offer URL</label>
                <a
                  href={issuance.offerUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-sm text-blue-600 hover:text-blue-800 mt-1 break-all"
                >
                  {issuance.offerUrl}
                </a>
              </div>
            )}
          </div>

          {/* QR Code placeholder - in a real app, you'd generate a QR code for the offer URL */}
          {issuance.offerUrl && (
            <div className="mt-6 text-center">
              <div className="inline-block p-4 bg-gray-100 rounded-lg">
                <div className="w-32 h-32 bg-white border-2 border-dashed border-gray-300 rounded flex items-center justify-center">
                  <span className="text-xs text-gray-500">QR Code<br />for Mobile Wallet</span>
                </div>
              </div>
              <p className="text-xs text-gray-600 mt-2">
                Scan with your Aries wallet app to receive the credential
              </p>
            </div>
          )}
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex justify-between items-center pt-6 border-t">
        <button
          onClick={onReset}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          Process Another Document
        </button>
        
        {issuance?.offerUrl && (
          <a
            href={issuance.offerUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Open in Wallet
          </a>
        )}
      </div>
    </div>
  );
}