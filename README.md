# CredGuard

AI-Powered Digital Credential Verification System

## Overview

CredGuard is a digital credential verification system that demonstrates:
- Digital identity and verifiable credentials
- AI extraction and reasoning
- Java backend architecture with clean architecture principles
- Modern REST API design

## Architecture

The backend follows clean architecture with clear layer separation:

- **domain/** - Pure domain models (Java records), no framework dependencies
- **application/** - Business logic and use cases
- **web/** - REST controllers and DTOs
- **infra/** - External integrations (AI, storage, etc.)

## Features

### ✅ Implemented

1. **Verification Pipeline**
   - Issuer trust validation
   - Expiry validation
   - Signature validation (stub implementation)
   - Combined verification results

2. **REST API Endpoints**
   - `POST /api/credentials/verify` - Verify a credential from JSON
   - `POST /api/credentials/upload` - Upload file and verify extracted credential
   - `GET /health` - Health check endpoint

3. **AI Extraction Service**
   - OpenAI Vision API integration
   - Extracts credential information from PDFs and images
   - Mock mode for testing without API key
   - Maps AI output to domain models

4. **Error Handling**
   - Global exception handler
   - Validation error handling
   - File upload error handling

5. **Testing**
   - Unit tests for VerificationService
   - Comprehensive test coverage

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.6+

### Configuration

Set environment variables:

```bash
# Required for AI extraction (or use mock mode)
export OPENAI_API_KEY=your-api-key-here

# Optional: Enable mock mode for testing without API key
export AI_MOCK_MODE=true
```

Or configure in `application.properties`:

```properties
ai.openai.api-key=your-api-key
ai.mock-mode=false
```

### Running the Application

```bash
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### Testing

```bash
cd backend
mvn test
```

## API Usage

### Verify Credential (JSON)

```bash
curl -X POST http://localhost:8080/api/credentials/verify \
  -H "Content-Type: application/json" \
  -d '{
    "id": "cred-123",
    "type": "VerifiableCredential",
    "issuer": {
      "id": "did:example:issuer",
      "displayName": "Example University",
      "trusted": true
    },
    "subject": "did:example:subject",
    "issuedAt": "2024-01-01T00:00:00Z",
    "expiresAt": "2025-01-01T00:00:00Z",
    "claims": {
      "degree": "Bachelor of Science"
    }
  }'
```

### Upload and Verify File

```bash
curl -X POST http://localhost:8080/api/credentials/upload \
  -F "file=@credential.pdf"
```

## Project Structure

```
backend/
├── src/main/java/com/credguard/
│   ├── domain/              # Domain models
│   ├── application/         # Business logic
│   │   └── ai/              # AI extraction service
│   ├── web/                 # REST controllers
│   │   └── dto/             # Data transfer objects
│   └── infra/               # Infrastructure
│       └── ai/              # AI client implementations
└── src/test/                # Tests
```

## Next Steps

- [ ] Implement full signature verification with Nimbus JOSE
- [ ] Add frontend (Next.js + Tailwind)
- [ ] Add blockchain anchoring
- [ ] AWS/cloud deployment
- [ ] Enhanced AI extraction with better prompts
- [ ] Support for multiple AI providers

## License

MIT
