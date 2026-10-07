# ADR-006: Media Storage

## Status

Accepted

## Context

The platform accepts a CV PDF, project images, and a profile image. Uploaded
media must survive container restarts and redeployments, preserve the exact
original bytes, and remain addressable through stable public URLs without
introducing a separate storage service in this release.

## Decision

Store uploaded media in PostgreSQL in the `media_assets` table as binary data.
The CMS stores the returned public URL in the existing document, project, and
profile URL fields. Existing externally hosted URLs remain supported.

Uploads are limited to 10 MB and validated from their file signatures. Supported
types are PDF documents and JPEG, PNG, WebP, and GIF images. The application
does not transform or recompress uploaded bytes.

## Rationale

- PostgreSQL backups include the original media bytes.
- No filesystem persistence or object-storage provisioning is required.
- Public APIs continue to use URL values, keeping the content contract simple.
- Signature validation prevents trusting a submitted filename or MIME type.

## Consequences

- Database size and backup/restore duration increase with media volume.
- Public media retrieval streams the bytes stored in PostgreSQL.
- Unreferenced media is not automatically deleted.
- Media requests are publicly readable by design; uploads remain restricted to
  authenticated administrators and editors.

## Alternatives Considered

- **Local filesystem:** rejected because files can be lost on container
  replacement and are not shared automatically across instances.
- **Object storage:** deferred for this release to avoid a new provider and
  infrastructure dependency.
