# ADR-002: Legacy URL-Based Document and Image Storage

## Status

Superseded by ADR-006

## Original decision date

2026-10-02

## Context

The portfolio needs CV documents and project images. The current data model
stores document and image URLs, while production object storage would require
additional infrastructure, access controls, upload validation, and lifecycle
management.

## Original Phase 6 decision

Phase 6 used externally hosted HTTP or HTTPS URLs for documents and project
images. That remains supported for compatibility with existing records.

## Superseding decision

ADR-006 specifies storing uploaded bytes in PostgreSQL. Existing hosted URLs
remain supported for compatibility.

## Rationale

- Preserve the original URL-based content fields for compatibility.

## Consequences

- See ADR-006 for upload storage, byte preservation, and binary data lifecycle.

## Alternatives Considered

- **Direct local filesystem storage:** superseded; uploaded media is stored in
  PostgreSQL per ADR-006.
