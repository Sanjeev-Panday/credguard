'use client';

import { Credential } from '@/lib/api/types';
import { useState } from 'react';

interface CredentialViewerProps {
  credential: Credential;
}

export default function CredentialViewer({ credential }: CredentialViewerProps) {
  const [viewMode, setViewMode] = useState<'formatted' | 'json'>('formatted');

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm border border-gray-200 dark:border-gray-700">
      <div className="flex items-center justify-between mb-4">
        <h4 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
          Credential Details
        </h4>
        <div className="flex space-x-2">
          <button
            onClick={() => setViewMode('formatted')}
            className={`px-4 py-2 rounded text-sm font-medium transition-colors ${
              viewMode === 'formatted'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
            }`}
          >
            Formatted
          </button>
          <button
            onClick={() => setViewMode('json')}
            className={`px-4 py-2 rounded text-sm font-medium transition-colors ${
              viewMode === 'json'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
            }`}
          >
            JSON
          </button>
        </div>
      </div>

      {viewMode === 'formatted' ? (
        <FormattedView credential={credential} />
      ) : (
        <JSONView credential={credential} />
      )}
    </div>
  );
}

function FormattedView({ credential }: { credential: Credential }) {
  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <DetailField label="ID" value={credential.id} />
        <DetailField label="Type" value={credential.type} />
        <DetailField label="Subject" value={credential.subject} />
        <DetailField
          label="Issued At"
          value={new Date(credential.issuedAt).toLocaleString()}
        />
        <DetailField
          label="Expires At"
          value={
            credential.expiresAt
              ? new Date(credential.expiresAt).toLocaleString()
              : 'Never'
          }
        />
      </div>

      <div className="border-t border-gray-200 dark:border-gray-700 pt-4">
        <h5 className="font-semibold mb-3 text-gray-900 dark:text-gray-100">
          Issuer
        </h5>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <DetailField label="ID" value={credential.issuer.id} />
          <DetailField
            label="Display Name"
            value={credential.issuer.displayName}
          />
          <DetailField
            label="Trusted"
            value={credential.issuer.trusted ? 'Yes' : 'No'}
          />
        </div>
      </div>

      {Object.keys(credential.claims).length > 0 && (
        <div className="border-t border-gray-200 dark:border-gray-700 pt-4">
          <h5 className="font-semibold mb-3 text-gray-900 dark:text-gray-100">
            Claims
          </h5>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {Object.entries(credential.claims).map(([key, value]) => (
              <DetailField
                key={key}
                label={key}
                value={String(value)}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function JSONView({ credential }: { credential: Credential }) {
  return (
    <pre className="bg-gray-900 dark:bg-black text-green-400 p-4 rounded overflow-auto text-sm font-mono">
      {JSON.stringify(credential, null, 2)}
    </pre>
  );
}

function DetailField({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <div className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
        {label}
      </div>
      <div className="text-gray-900 dark:text-gray-100 break-words">{value}</div>
    </div>
  );
}

