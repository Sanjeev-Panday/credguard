'use client';

import { useState } from 'react';
import { DocumentType } from '@/lib/api/types';

interface DocumentTypePickerProps {
  value: DocumentType;
  onChange: (documentType: DocumentType) => void;
  disabled?: boolean;
}

const documentTypeOptions = [
  { value: DocumentType.PASSPORT, label: 'Passport', description: 'Government-issued travel document' },
  { value: DocumentType.DRIVERS_LICENSE, label: 'Driver\'s License', description: 'State-issued driving permit' },
  { value: DocumentType.DEGREE_CERTIFICATE, label: 'Degree Certificate', description: 'Educational qualification certificate' },
  { value: DocumentType.BIRTH_CERTIFICATE, label: 'Birth Certificate', description: 'Official birth registration document' },
  { value: DocumentType.OTHER, label: 'Other Document', description: 'Other identity document' },
];

export function DocumentTypePicker({ value, onChange, disabled = false }: DocumentTypePickerProps) {
  return (
    <div className="space-y-3">
      <label className="text-sm font-medium text-gray-700">
        Document Type
      </label>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
        {documentTypeOptions.map((option) => (
          <div
            key={option.value}
            className={`
              relative cursor-pointer rounded-lg border p-4 focus:outline-none transition-all
              ${value === option.value
                ? 'border-blue-500 bg-blue-50 ring-2 ring-blue-500'
                : 'border-gray-300 bg-white hover:bg-gray-50'
              }
              ${disabled ? 'opacity-50 cursor-not-allowed' : ''}
            `}
            onClick={() => !disabled && onChange(option.value)}
          >
            <div className="flex items-start">
              <div className="flex-shrink-0">
                <input
                  type="radio"
                  name="documentType"
                  value={option.value}
                  checked={value === option.value}
                  onChange={() => onChange(option.value)}
                  disabled={disabled}
                  className="mt-1 h-4 w-4 text-blue-600 border-gray-300 focus:ring-blue-500"
                />
              </div>
              <div className="ml-3 flex-1">
                <div className="font-medium text-gray-900">
                  {option.label}
                </div>
                <div className="text-sm text-gray-500 mt-1">
                  {option.description}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}