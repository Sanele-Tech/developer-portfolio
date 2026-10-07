CREATE TABLE media_assets (
    storage_key VARCHAR(40) PRIMARY KEY,
    mime_type VARCHAR(40) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0 AND size_bytes <= 10485760),
    content BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
