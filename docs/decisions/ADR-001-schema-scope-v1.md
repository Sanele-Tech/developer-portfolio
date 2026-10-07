# ADR-001: Schema Scope for the Initial Backend

## Status

Accepted

## Decision

Phase 3 is implemented against the tables currently defined by Flyway V1 and V2. The database migrations are the source of truth for entity, repository, service, and API contracts.

The following SRS wishlist items are deferred until a feature requires them:

- `published` or visibility columns
- `display_order` columns
- `navigation_items`
- `document_types`
- standalone Java entities for composite join tables

Until visibility is modeled by a later migration, public read services treat stored rows as public. New schema capabilities will be introduced through a new Flyway migration rather than editing an applied migration.

## Consequences

The initial public API reflects the current schema and does not offer draft/publish filtering. The normalized many-to-many relationships are managed through JPA collections, while the existing legacy project technology text remains available for backward compatibility.