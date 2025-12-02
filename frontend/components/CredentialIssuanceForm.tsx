'use client';

import { useState } from 'react';
import { DocumentTypePicker } from './DocumentTypePicker';
import FileUpload from './FileUpload';
import { CredentialIssuanceResult } from './CredentialIssuanceResult';
import { DocumentType, CredentialIssuanceResponse } from '@/lib/api/types';
import { issueCredentialFromDocument } from '@/lib/api/client';

export function CredentialIssuanceForm() {
  const [documentType, setDocumentType] = useState<DocumentType>(DocumentType.PASSPORT);
  const [walletDid, setWalletDid] = useState('');
  const [previewOnly, setPreviewOnly] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [result, setResult] = useState<CredentialIssuanceResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleFileUpload = async (file: File) => {
    setIsProcessing(true);
    setError(null);
    setResult(null);

    try {
      const response = await issueCredentialFromDocument(file, {
        documentType,
        walletDid,
        previewOnly,
      });

      setResult(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleReset = () => {
    setResult(null);
    setError(null);
    setWalletDid('');
    setPreviewOnly(false);
  };

  const isFormValid = walletDid.trim().length > 0;

  if (result) {
    return <CredentialIssuanceResult result={result} onReset={handleReset} />;
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-8">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-gray-900">Issue Verifiable Credential</h1>
        <p className="mt-2 text-gray-600">
          Upload a physical identity document to issue a verifiable digital credential to your mobile wallet
        </p>
      </div>

      <div className="bg-white shadow rounded-lg p-6 space-y-6">
        {/* Document Type Selection */}
        <DocumentTypePicker
          value={documentType}
          onChange={setDocumentType}
          disabled={isProcessing}
        />

        {/* Wallet DID Input */}
        <div>
          <label htmlFor="walletDid" className="block text-sm font-medium text-gray-700">
            Wallet DID *
          </label>
          <div className="mt-1">
            <input
              type="text"
              id="walletDid"
              value={walletDid}
              onChange={(e) => setWalletDid(e.target.value)}
              disabled={isProcessing}
              placeholder="did:example:1234567890abcdef"
              className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 disabled:opacity-50"
            />
          </div>
          <p className="mt-1 text-xs text-gray-500">
            The DID (Decentralized Identifier) of your Aries wallet where the credential will be sent
          </p>
        </div>

        {/* Preview Mode Toggle */}
        <div className="flex items-center">
          <input
            id="previewOnly"
            type="checkbox"
            checked={previewOnly}
            onChange={(e) => setPreviewOnly(e.target.checked)}
            disabled={isProcessing}
            className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded disabled:opacity-50"
          />
          <label htmlFor="previewOnly" className="ml-2 block text-sm text-gray-900">
            Preview mode (parse document without issuing credential)
          </label>
        </div>

        {/* File Upload */}
        <div>
          <FileUpload
            onFileSelect={handleFileUpload}
            isUploading={!isFormValid || isProcessing}
          />
        </div>

        {/* Form Validation Message */}
        {!isFormValid && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-md p-3">
            <p className="text-sm text-yellow-700">
              Please provide a wallet DID before uploading a document.
            </p>
          </div>
        )}

        {/* Processing Status */}
        {isProcessing && (
          <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
            <div className="flex items-center">
              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
              <span className="ml-3 text-sm text-blue-700">
                {previewOnly ? 'Parsing document...' : 'Processing document and issuing credential...'}
              </span>
            </div>
          </div>
        )}

        {/* Error Display */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-md p-4">
            <div className="flex">
              <div className="flex-shrink-0">
                <span className="h-5 w-5 text-red-400">⚠</span>
              </div>
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">
                  Processing Failed
                </h3>
                <p className="text-sm text-red-700 mt-1">
                  {error}
                </p>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Help Section */}
      <div className="bg-gray-50 rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">How it Works</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="text-center">
            <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
              <span className="text-blue-600 font-bold">1</span>
            </div>
            <h4 className="font-medium text-gray-900">Select Document Type</h4>
            <p className="text-sm text-gray-600 mt-1">
              Choose the type of physical document you want to digitize
            </p>
          </div>
          <div className="text-center">
            <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
              <span className="text-blue-600 font-bold">2</span>
            </div>
            <h4 className="font-medium text-gray-900">AI Extraction</h4>
            <p className="text-sm text-gray-600 mt-1">
              Our AI extracts key information from your document image
            </p>
          </div>
          <div className="text-center">
            <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
              <span className="text-blue-600 font-bold">3</span>
            </div>
            <h4 className="font-medium text-gray-900">Digital Credential</h4>
            <p className="text-sm text-gray-600 mt-1">
              Receive a verifiable credential in your mobile wallet
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}