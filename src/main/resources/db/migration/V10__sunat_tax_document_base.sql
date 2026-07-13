ALTER TABLE comprobantes
    ADD COLUMN IF NOT EXISTS cliente_nombre_snapshot VARCHAR(180),
    ADD COLUMN IF NOT EXISTS document_folder_type VARCHAR(40),
    ADD COLUMN IF NOT EXISTS xml_storage_file_id UUID REFERENCES storage_files(id),
    ADD COLUMN IF NOT EXISTS cdr_storage_file_id UUID REFERENCES storage_files(id),
    ADD COLUMN IF NOT EXISTS pdf_storage_file_id UUID REFERENCES storage_files(id),
    ADD COLUMN IF NOT EXISTS sunat_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS sunat_next_retry_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS sunat_last_error TEXT,
    ADD COLUMN IF NOT EXISTS sunat_ticket VARCHAR(120);

UPDATE comprobantes
SET tipo_comprobante = CASE
        WHEN tipo_comprobante = 'BOLETA' THEN
            CASE
                WHEN numero_documento_cliente IS NULL OR numero_documento_cliente = '' THEN 'BOLETA_SIMPLE'
                ELSE 'BOLETA_CON_DOCUMENTO'
            END
        ELSE tipo_comprobante
    END,
    cliente_nombre_snapshot = COALESCE(cliente_nombre_snapshot, razon_social, 'PUBLICO_GENERAL'),
    document_folder_type = COALESCE(document_folder_type, CASE
        WHEN tipo_comprobante = 'FACTURA' THEN 'FACTURAS'
        ELSE 'BOLETAS'
    END);

CREATE TABLE IF NOT EXISTS document_sequences (
    id BIGSERIAL PRIMARY KEY,
    document_type VARCHAR(30) NOT NULL,
    serie VARCHAR(10) NOT NULL,
    next_number BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    CONSTRAINT ux_document_sequences_type_serie UNIQUE (document_type, serie),
    CONSTRAINT ck_document_sequences_next_positive CHECK (next_number > 0)
);

INSERT INTO document_sequences(document_type, serie, next_number)
SELECT 'FACTURA', 'F001', COALESCE(MAX(correlativo::BIGINT), 0) + 1
FROM comprobantes
WHERE tipo_comprobante = 'FACTURA' AND correlativo ~ '^[0-9]+$'
ON CONFLICT (document_type, serie) DO NOTHING;

INSERT INTO document_sequences(document_type, serie, next_number)
SELECT 'BOLETA_SIMPLE', 'B001', COALESCE(MAX(correlativo::BIGINT), 0) + 1
FROM comprobantes
WHERE tipo_comprobante IN ('BOLETA_SIMPLE', 'BOLETA_CON_DOCUMENTO') AND correlativo ~ '^[0-9]+$'
ON CONFLICT (document_type, serie) DO NOTHING;

INSERT INTO document_sequences(document_type, serie, next_number)
SELECT 'BOLETA_CON_DOCUMENTO', 'B001', next_number
FROM document_sequences
WHERE document_type = 'BOLETA_SIMPLE' AND serie = 'B001'
ON CONFLICT (document_type, serie) DO NOTHING;

INSERT INTO document_sequences(document_type, serie, next_number)
SELECT 'NOTA_CREDITO', 'NC01', COALESCE(MAX(correlativo::BIGINT), 0) + 1
FROM notas_credito
WHERE correlativo ~ '^[0-9]+$'
ON CONFLICT (document_type, serie) DO NOTHING;

INSERT INTO document_sequences(document_type, serie, next_number)
VALUES ('NOTA_DEBITO', 'ND01', 1)
ON CONFLICT (document_type, serie) DO NOTHING;

ALTER TABLE notas_credito
    ADD COLUMN IF NOT EXISTS xml_storage_file_id UUID REFERENCES storage_files(id),
    ADD COLUMN IF NOT EXISTS cdr_storage_file_id UUID REFERENCES storage_files(id),
    ADD COLUMN IF NOT EXISTS pdf_storage_file_id UUID REFERENCES storage_files(id),
    ADD COLUMN IF NOT EXISTS sunat_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS sunat_next_retry_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS sunat_last_error TEXT,
    ADD COLUMN IF NOT EXISTS sunat_ticket VARCHAR(120);

CREATE TABLE IF NOT EXISTS notas_debito (
    id BIGSERIAL PRIMARY KEY,
    comprobante_id BIGINT NOT NULL REFERENCES comprobantes(id),
    serie VARCHAR(10) NOT NULL,
    correlativo VARCHAR(20) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    monto_adicional NUMERIC(10, 2) NOT NULL,
    usuario_emisor_id BIGINT REFERENCES usuarios(id),
    fecha_emision TIMESTAMP NOT NULL DEFAULT NOW(),
    estado_sunat VARCHAR(30) DEFAULT 'NO_ENVIADO',
    hash_sunat VARCHAR(100),
    mensaje_sunat VARCHAR(500),
    xml_storage_file_id UUID REFERENCES storage_files(id),
    cdr_storage_file_id UUID REFERENCES storage_files(id),
    pdf_storage_file_id UUID REFERENCES storage_files(id),
    sunat_attempts INTEGER NOT NULL DEFAULT 0,
    sunat_next_retry_at TIMESTAMP,
    sunat_last_error TEXT,
    sunat_ticket VARCHAR(120),
    CONSTRAINT ux_nd_serie_correlativo UNIQUE (serie, correlativo)
);

CREATE TABLE IF NOT EXISTS sunat_submission_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comprobante_id BIGINT REFERENCES comprobantes(id),
    nota_credito_id BIGINT REFERENCES notas_credito(id),
    nota_debito_id BIGINT REFERENCES notas_debito(id),
    document_type VARCHAR(30) NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP,
    last_error TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    CONSTRAINT ck_sunat_submission_one_document CHECK (
        (CASE WHEN comprobante_id IS NULL THEN 0 ELSE 1 END) +
        (CASE WHEN nota_credito_id IS NULL THEN 0 ELSE 1 END) +
        (CASE WHEN nota_debito_id IS NULL THEN 0 ELSE 1 END) = 1
    ),
    CONSTRAINT ck_sunat_submission_status CHECK (status IN (
        'PENDING',
        'PROCESSING',
        'ACCEPTED',
        'ACCEPTED_WITH_OBSERVATION',
        'REJECTED',
        'FAILED_RETRYABLE',
        'FAILED_FINAL'
    ))
);

CREATE INDEX IF NOT EXISTS idx_comprobantes_estado_sunat ON comprobantes(estado_sunat);
CREATE INDEX IF NOT EXISTS idx_comprobantes_tipo_fecha ON comprobantes(tipo_comprobante, fecha_emision);
CREATE INDEX IF NOT EXISTS idx_sunat_jobs_status_retry ON sunat_submission_jobs(status, next_retry_at);
CREATE INDEX IF NOT EXISTS idx_sunat_jobs_comprobante ON sunat_submission_jobs(comprobante_id);
CREATE INDEX IF NOT EXISTS idx_sunat_jobs_nc ON sunat_submission_jobs(nota_credito_id);
CREATE INDEX IF NOT EXISTS idx_sunat_jobs_nd ON sunat_submission_jobs(nota_debito_id);
