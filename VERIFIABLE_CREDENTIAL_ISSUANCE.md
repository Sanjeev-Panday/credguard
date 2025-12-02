# Verifiable Credential Issuance Feature

## Overview

This feature enables CredGuard to issue verifiable digital credentials from scanned physical identity documents. Users can upload photos or scans of documents like passports, driver's licenses, or degree certificates, and receive a W3C-compliant verifiable credential in their Aries-compatible mobile wallet.

## Architecture

### Backend Components

#### Domain Layer (`domain/`)
- **`PhysicalDocument`**: Represents uploaded physical documents with extraction status
- **`VerifiableCredential`**: W3C-compliant verifiable credential model
- **`CredentialIssuanceResult`**: Result of the credential issuance process

#### Application Layer (`application/`)
- **`CredentialIssuanceService`**: Main orchestration service for the credential issuance workflow
- **`PhysicalDocumentParsingService`**: AI-powered document attribute extraction service

#### Infrastructure Layer (`infra/`)
- **`AriesCloudAgentClient`**: Interface for Aries agent integration
- **`AriesCloudAgentClientImpl`**: HTTP client implementation for Aries agent communication

#### Web Layer (`web/`)
- **`CredentialIssuanceController`**: REST API endpoints for credential issuance
- **DTOs**: Request/response objects for API communication

### Frontend Components

- **`CredentialIssuanceForm`**: Main form for document upload and credential issuance
- **`DocumentTypePicker`**: UI component for selecting document types
- **`CredentialIssuanceResult`**: Display component for issuance results
- Updated **`page.tsx`**: Added tabs for verification and issuance workflows

## Supported Document Types

1. **Passport** - International travel document
2. **Driver's License** - State-issued driving permit
3. **Degree Certificate** - Educational qualification certificate
4. **Birth Certificate** - Official birth registration document
5. **Other** - General identity document type

## API Endpoints

### Document Upload & Credential Issuance
```
POST /api/credentials/issuance/issue-from-document
```
- **Parameters**: 
  - `file`: Multipart file (image/PDF)
  - `documentType`: Document type enum
  - `walletDid`: Target wallet DID
  - `previewOnly`: Boolean (optional, for document parsing preview)

### Async Credential Issuance
```
POST /api/credentials/issuance/issue-from-document/async
```
- Returns job ID for tracking async processing

### Status Checking
```
GET /api/credentials/issuance/status/{credentialExchangeId}
```
- Returns current status of credential exchange

### Credential Revocation
```
POST /api/credentials/issuance/revoke/{credentialId}
```
- Revokes a previously issued credential

### Connection Status
```
GET /api/credentials/issuance/connection/{connectionId}/status
```
- Checks wallet connection status

## Workflow

1. **Document Upload**: User selects document type and uploads a scanned image/PDF
2. **AI Extraction**: OpenAI Vision API extracts structured attributes from the document
3. **Credential Creation**: System creates a W3C-compliant verifiable credential
4. **Aries Integration**: Establishes connection with user's mobile wallet
5. **Credential Issuance**: Issues the credential via Aries Cloud Agent
6. **Mobile Delivery**: User receives the credential in their Aries wallet app

## Configuration

### Application Properties
```properties
# Aries Cloud Agent Configuration
aries.agent-url=${ARIES_AGENT_URL:http://localhost:8040}
aries.api-key=${ARIES_API_KEY:}
aries.wallet-id=${ARIES_WALLET_ID:credguard-wallet}
aries.credential-definition-id=${ARIES_CRED_DEF_ID:}
aries.mock-mode=${ARIES_MOCK_MODE:true}
```

### Environment Variables
- `ARIES_AGENT_URL`: URL of your Aries Cloud Agent
- `ARIES_API_KEY`: API key for agent authentication
- `ARIES_MOCK_MODE`: Enable mock mode for development (default: true)
- `OPENAI_API_KEY`: Required for AI document parsing

## Mock Mode

For development and testing, the system supports mock mode:
- Set `ARIES_MOCK_MODE=true` to simulate credential issuance
- AI extraction can also use mock mode via `AI_MOCK_MODE=true`
- Perfect for development without requiring actual Aries agent setup

## Frontend Features

- **Tabbed Interface**: Switch between credential verification and issuance
- **Document Type Selection**: Visual picker for different document types
- **Drag & Drop Upload**: Easy file upload with progress indication
- **Preview Mode**: Parse documents without issuing credentials
- **Real-time Status**: Live updates on processing status
- **QR Code Support**: Generate QR codes for mobile wallet connection

## Security & Privacy

- Documents are processed server-side and not stored permanently
- AI extraction happens via secure API calls to OpenAI
- Credentials use cryptographic proofs for authenticity
- Wallet connections use DID-based authentication

## Testing

Comprehensive test suite includes:
- Unit tests for all service classes
- Integration tests for API endpoints
- Mock implementations for external dependencies
- Frontend component testing capabilities

## Future Enhancements

1. **Additional Document Types**: ID cards, vaccination certificates, etc.
2. **Batch Processing**: Upload and process multiple documents
3. **Advanced AI Features**: Better OCR, multilingual support
4. **Credential Templates**: Customizable credential schemas
5. **Audit Logging**: Comprehensive issuance audit trails
6. **Revocation Registry**: Advanced credential revocation management

## Getting Started

1. **Setup Aries Agent**: Deploy an Aries Cloud Agent instance
2. **Configure Environment**: Set required environment variables
3. **Enable AI**: Configure OpenAI API access
4. **Run Application**: Start both backend and frontend
5. **Test Workflow**: Use mock mode for initial testing
6. **Mobile Wallet**: Install Aries-compatible wallet app for testing

This feature transforms CredGuard from a verification-only system into a complete digital identity platform, enabling users to digitize their physical credentials securely and conveniently.