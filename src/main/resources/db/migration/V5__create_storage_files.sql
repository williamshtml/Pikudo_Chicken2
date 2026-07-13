CREATE TABLE storage_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(30) NOT NULL,
    bucket_or_drive VARCHAR(160),
    folder_id VARCHAR(160),
    folder_path VARCHAR(500),
    external_file_id VARCHAR(240),
    public_url TEXT,
    download_url TEXT,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255),
    mime_type VARCHAR(120) NOT NULL,
    extension VARCHAR(20),
    size_bytes BIGINT NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL,
    owner_module VARCHAR(60),
    owner_id VARCHAR(80),
    created_by_usuario_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_storage_files_created_by_usuario
        FOREIGN KEY (created_by_usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_storage_files_owner
    ON storage_files(owner_module, owner_id);

CREATE INDEX idx_storage_files_provider_external
    ON storage_files(provider, external_file_id);

CREATE INDEX idx_storage_files_checksum
    ON storage_files(checksum_sha256);
