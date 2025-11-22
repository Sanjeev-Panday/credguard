'use client';

import { useCallback, useState } from 'react';

interface FileUploadProps {
  onFileSelect: (file: File) => void;
  isUploading?: boolean;
}

export default function FileUpload({ onFileSelect, isUploading = false }: FileUploadProps) {
  const [isDragging, setIsDragging] = useState(false);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setIsDragging(false);

      const file = e.dataTransfer.files[0];
      if (file) {
        onFileSelect(file);
      }
    },
    [onFileSelect]
  );

  const handleFileInput = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (file) {
        onFileSelect(file);
      }
    },
    [onFileSelect]
  );

  return (
    <div
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
      className={`
        relative border-2 border-dashed rounded-lg p-12 text-center transition-colors
        ${
          isDragging
            ? 'border-blue-500 bg-blue-50 dark:bg-blue-950'
            : 'border-gray-300 dark:border-gray-700 bg-gray-50 dark:bg-gray-900'
        }
        ${isUploading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:border-gray-400 dark:hover:border-gray-600'}
      `}
    >
      <input
        type="file"
        id="file-upload"
        className="hidden"
        onChange={handleFileInput}
        accept=".pdf,.png,.jpg,.jpeg,.gif,.webp"
        disabled={isUploading}
      />
      <label
        htmlFor="file-upload"
        className="cursor-pointer flex flex-col items-center space-y-4"
      >
        <svg
          className="w-16 h-16 text-gray-400 dark:text-gray-600"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
          />
        </svg>
        <div className="text-gray-700 dark:text-gray-300">
          <span className="font-semibold">Click to upload</span> or drag and drop
        </div>
        <div className="text-sm text-gray-500 dark:text-gray-400">
          PDF, PNG, JPG, GIF or WEBP (MAX. 10MB)
        </div>
      </label>
      {isUploading && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50 rounded-lg">
          <div className="text-white font-semibold">Uploading...</div>
        </div>
      )}
    </div>
  );
}

