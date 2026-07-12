CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(160) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_roles FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permissions FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_token_hash VARCHAR(64),
    created_by_ip VARCHAR(64),
    user_agent VARCHAR(255),
    CONSTRAINT fk_refresh_tokens_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id BIGINT,
    username VARCHAR(60),
    action VARCHAR(80) NOT NULL,
    outcome VARCHAR(30) NOT NULL,
    ip_address VARCHAR(64),
    user_agent VARCHAR(255),
    detail VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_audit_logs_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_usuario ON refresh_tokens (usuario_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs (action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_usuario ON audit_logs (usuario_id);
