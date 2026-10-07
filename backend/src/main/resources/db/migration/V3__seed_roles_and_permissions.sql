INSERT INTO permissions (name) VALUES
    ('CONTENT_EDIT'),
    ('PROJECT_EDIT'),
    ('DOCUMENTS_EDIT'),
    ('MESSAGES_READ'),
    ('ANALYTICS_VIEW'),
    ('USERS_MANAGE'),
    ('AUDIT_VIEW')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name) VALUES
    ('ADMIN'),
    ('EDITOR'),
    ('ANALYST')
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, permissions.id
FROM roles CROSS JOIN permissions
WHERE roles.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, permissions.id
FROM roles CROSS JOIN permissions
WHERE roles.name = 'EDITOR'
  AND permissions.name IN ('CONTENT_EDIT', 'PROJECT_EDIT', 'DOCUMENTS_EDIT')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, permissions.id
FROM roles CROSS JOIN permissions
WHERE roles.name = 'ANALYST'
  AND permissions.name IN ('ANALYTICS_VIEW', 'MESSAGES_READ')
ON CONFLICT DO NOTHING;