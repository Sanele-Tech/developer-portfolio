CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(120) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE pages (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(180) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    excerpt TEXT,
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    author_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE page_versions (
    id BIGSERIAL PRIMARY KEY,
    page_id BIGINT NOT NULL REFERENCES pages (id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (page_id, version_number)
);

CREATE TABLE technologies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(140) NOT NULL UNIQUE
);

CREATE TABLE project_technologies (
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    technology_id BIGINT NOT NULL REFERENCES technologies (id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, technology_id)
);

CREATE TABLE project_links (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    label VARCHAR(80) NOT NULL,
    url VARCHAR(500) NOT NULL,
    link_type VARCHAR(40) NOT NULL DEFAULT 'OTHER'
);

CREATE TABLE project_images (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE experience (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile (id) ON DELETE CASCADE,
    job_title VARCHAR(160) NOT NULL,
    company VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    is_current_role BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE education (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile (id) ON DELETE CASCADE,
    institution VARCHAR(200) NOT NULL,
    qualification VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile (id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(120),
    proficiency SMALLINT CHECK (proficiency IS NULL OR proficiency BETWEEN 0 AND 100),
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile (id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    issuer VARCHAR(160),
    description TEXT,
    achieved_on DATE,
    url VARCHAR(500),
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_name VARCHAR(120) NOT NULL,
    sender_email VARCHAR(320) NOT NULL,
    subject VARCHAR(200),
    body TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    assigned_to BIGINT REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE message_replies (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages (id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE email_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analytics_sessions (
    id UUID PRIMARY KEY,
    visitor_id VARCHAR(120),
    user_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMPTZ
);

CREATE TABLE analytics_events (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES analytics_sessions (id) ON DELETE CASCADE,
    event_name VARCHAR(120) NOT NULL,
    path VARCHAR(500),
    metadata JSONB,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    document_type VARCHAR(80) NOT NULL,
    storage_url VARCHAR(500) NOT NULL,
    mime_type VARCHAR(120),
    size_bytes BIGINT CHECK (size_bytes IS NULL OR size_bytes >= 0),
    uploaded_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE site_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(160) NOT NULL UNIQUE,
    setting_value TEXT,
    updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feature_flags (
    id BIGSERIAL PRIMARY KEY,
    flag_key VARCHAR(160) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500),
    updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(120),
    entity_id BIGINT,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seo_metadata (
    id BIGSERIAL PRIMARY KEY,
    page_id BIGINT UNIQUE REFERENCES pages (id) ON DELETE CASCADE,
    meta_title VARCHAR(200),
    meta_description VARCHAR(500),
    canonical_url VARCHAR(500),
    og_image_url VARCHAR(500),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pages_status ON pages (status);
CREATE INDEX idx_page_versions_page_id ON page_versions (page_id);
CREATE INDEX idx_project_links_project_id ON project_links (project_id);
CREATE INDEX idx_project_images_project_order ON project_images (project_id, sort_order);
CREATE INDEX idx_experience_profile_order ON experience (profile_id, sort_order);
CREATE INDEX idx_education_profile_order ON education (profile_id, sort_order);
CREATE INDEX idx_skills_profile_order ON skills (profile_id, sort_order);
CREATE INDEX idx_achievements_profile_order ON achievements (profile_id, sort_order);
CREATE INDEX idx_messages_status_created ON messages (status, created_at DESC);
CREATE INDEX idx_message_replies_message_id ON message_replies (message_id);
CREATE INDEX idx_analytics_events_session_time ON analytics_events (session_id, occurred_at);
CREATE INDEX idx_audit_logs_user_created ON audit_logs (user_id, created_at DESC);