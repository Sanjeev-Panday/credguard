# CredGuard - Cursor Project Instructions

---------------------------------------------------------------------

## Project Summary

CredGuard is an AI-powered digital credential verification system
designed to demonstrate:

- Digital identity and verifiable credentials
- AI extraction and reasoning
- Java backend architecture
- Modern frontend development
- Blockchain anchoring (future)
- AWS/cloud deployment (future)

The system processes digital credentials using:

1. AI extraction (PDF/image to structured JSON)
2. Verification pipeline (issuer trust, expiry, signature checks)
3. Human-readable explanations
4. A frontend UI (Next.js + Tailwind)

Cursor should follow clean architecture, domain-driven design, and
generate extensible code.

---------------------------------------------------------------------

## Architecture Overview

### Backend (Java 21 + Spring Boot)

Backend folder: backend/

Package structure:

com.credguard
  domain         - pure domain models (Java records)
  application    - use cases and business logic
  web            - REST controllers and DTOs
  infra          - integrations (AI, storage, blockchain, AWS)

Layer rules:

- domain/: 
  - No Spring imports
  - No framework dependencies
  - Java records preferred
  - Immutable and side-effect free

- application/:
  - Services and business logic only
  - No HTTP, JSON, or Spring MVC logic

- web/:
  - Controllers and DTO mappers
  - No business logic
  - Controllers must call application services

- infra/:
  - All integrations (AI, blockchain, AWS, storage)
  - Keep infrastructure out of domain and application logic

---------------------------------------------------------------------

## Current Implementation Status

Already implemented:

- Spring Boot backend skeleton
- /api/health endpoint
- Domain models:
  - Credential
  - Issuer
  - VerificationResult
- Package structure
- Initial README

Cursor should preserve this foundation.

---------------------------------------------------------------------

## Features Cursor Should Build Next

### 1. Verification Pipeline (priority)

Implement:

- Issuer trust validation
- Expiry validation
- Signature validation (stub first, full Nimbus JOSE later)
- Combine validation results into VerificationResult

Expected helper methods:

- validateIssuer()
- validateExpiry()
- validateSignature()

### 2. AI Extraction Service

Folder: backend/src/main/java/com/credguard/application/ai/

Responsibilities:

- Accept raw file bytes
- Call AI Vision API
- Extract credential fields
- Map extracted fields to Credential
- Handle invalid inputs gracefully
- Use OPENAI_API_KEY from environment
- Provide mock mode for tests

### 3. REST Endpoints

In web layer:

- POST /api/credentials/verify
- POST /api/credentials/upload (multipart)

Use DTOs for requests and responses.
Controllers call application services only.

### 4. Frontend (future phase)

Cursor will generate:

- Next.js App Router pages (frontend/app/)
- File upload UI
- Verification result UI
- Tailwind components
- API client under frontend/lib/api/

---------------------------------------------------------------------

## AI Integration Guidelines

- Place AI client code in infra/ai/
- No hardcoded API keys
- Wrap LLM calls in helper classes
- Provide typed mapping from AI output to domain models
- Include robust error handling

---------------------------------------------------------------------

## Digital Identity Rules

- Issuer.trusted determines basic trust
- Expiration checks must use Instant.now()
- Signature verification must not be implemented in domain layer
- Domain layer cannot reference Spring, Jackson, Nimbus, or HTTP classes

---------------------------------------------------------------------

## Code Style Guidelines

Cursor should generate code that follows:

- Java 21 records when possible
- Constructor-based dependency injection
- Clear naming conventions
- Javadoc for all public classes
- No business logic in controllers
- Small, composable methods
- Clean and predictable error handling
- Organized imports

---------------------------------------------------------------------

## Testing Guidelines

Tests located in:

backend/src/test/java/com/credguard/

Tools:

- JUnit 5
- Mockito or Spring Boot test slices
- MockMvc for controller tests

Cursor should generate tests for:

- Verification logic
- AI extraction mapping
- DTO mapping
- REST endpoints

---------------------------------------------------------------------

## Frontend Development Guidelines (Future Phase)

Cursor should generate:

- Next.js App Router pages (frontend/app/)
- TypeScript components
- TailwindCSS styling
- Drag-and-drop upload area
- Credential result display components
- JSON viewer
- API client under frontend/lib/api/

UI should be:

- Minimalistic
- Clean
- Developer-tool styled
- Dark-mode friendly

---------------------------------------------------------------------

## AWS / Infra Guidelines (Future Phase)

Place infrastructure code in /infra.

Expected tools:

- Terraform or AWS CDK
- S3 for file uploads
- CloudFront for frontend
- ECS or Lambda for backend
- Secrets Manager or SSM Parameter Store for configuration

IAM should follow least privilege.

---------------------------------------------------------------------

## Final Directive to Cursor

Always follow the established architecture:

- Pure domain layer
- Application layer handles business logic
- Web layer handles HTTP and DTOs
- Infra layer handles external systems

All generated code should be production-grade, clean, and consistent with
a system designed by a senior architect.

Treat this file as the authoritative project guide.
