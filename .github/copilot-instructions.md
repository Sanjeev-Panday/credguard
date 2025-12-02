# Copilot Instructions for CredGuard

## Project Overview
CredGuard is an AI-powered digital credential verification system with a Java backend (clean architecture) and a Next.js/TypeScript frontend. It integrates AI for credential extraction and uses robust validation pipelines.

## Architecture & Key Patterns
- **Backend (Java, Spring Boot):**
  - Follows clean architecture: `domain/` (models), `application/` (business logic), `web/` (REST controllers), `infra/` (external integrations).
  - AI extraction via `infra/ai/` (OpenAI Vision API, mock mode supported).
  - Signature validation via `infra/crypto/` (Nimbus JOSE for JWT/JWS).
  - Error handling centralized in `web/GlobalExceptionHandler.java`.
  - DTOs in `web/dto/` for API payloads.
  - Core verification logic in `application/VerificationService.java` and validators in `application/validation/`.
- **Frontend (Next.js, TypeScript):**
  - UI in `frontend/components/` (drag-and-drop upload, result viewer).
  - API client in `frontend/lib/api/client.ts`.
  - Configurable backend URL via `NEXT_PUBLIC_API_URL` in `.env.local`.

## Developer Workflows
- **Build & Run Backend:**
  - `cd backend && mvn spring-boot:run` (API at `http://localhost:8080`)
- **Run Frontend:**
  - `cd frontend && npm install && npm run dev` (UI at `http://localhost:3000`)
- **Testing Backend:**
  - `cd backend && mvn test` (unit tests in `src/test/java/com/credguard/`)
- **AI Extraction (Mock Mode):**
  - Enable with `AI_MOCK_MODE=true` env or `ai.mock-mode=true` in `application.properties` for local/dev testing.

## Integration Points
- **AI Extraction:**
  - `infra/ai/OpenAIVisionClient.java` (real) and mock mode for tests.
- **Signature Verification:**
  - `infra/crypto/SignatureVerificationService.java` (uses Nimbus JOSE).
- **REST API:**
  - `web/CredentialController.java` exposes `/api/credentials/verify` and `/api/credentials/upload`.
- **Frontend ↔ Backend:**
  - API requests via `lib/api/client.ts`.

## Conventions & Patterns
- **DTOs:** All API payloads use DTOs in `web/dto/`.
- **Validation:** Each aspect (issuer trust, expiry, signature) is a separate validator in `application/validation/`.
- **Error Handling:** Use custom exceptions in `exception/` and handle globally.
- **Testing:** Focus on `VerificationServiceTest.java` for core logic.
- **Environment Config:** Prefer `.env.local` for frontend, `application.properties` for backend.

## Example: Verification Flow
1. Frontend uploads credential (JSON or file) via `/api/credentials/upload`.
2. Backend extracts data (AI or mock), validates (issuer, expiry, signature), returns result.
3. Frontend displays result in `VerificationResult.tsx`.

---
For new features, follow existing layer separation and validation patterns. See `README.md` for more details and example API usage.
