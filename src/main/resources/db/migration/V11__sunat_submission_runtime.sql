ALTER TABLE comprobantes
    ADD COLUMN IF NOT EXISTS sunat_response_code VARCHAR(40),
    ADD COLUMN IF NOT EXISTS sunat_response_description TEXT,
    ADD COLUMN IF NOT EXISTS sunat_sent_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS sunat_accepted_at TIMESTAMP;

ALTER TABLE notas_credito
    ADD COLUMN IF NOT EXISTS sunat_response_code VARCHAR(40),
    ADD COLUMN IF NOT EXISTS sunat_response_description TEXT,
    ADD COLUMN IF NOT EXISTS sunat_sent_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS sunat_accepted_at TIMESTAMP;

ALTER TABLE notas_debito
    ADD COLUMN IF NOT EXISTS sunat_response_code VARCHAR(40),
    ADD COLUMN IF NOT EXISTS sunat_response_description TEXT,
    ADD COLUMN IF NOT EXISTS sunat_sent_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS sunat_accepted_at TIMESTAMP;

ALTER TABLE sunat_submission_jobs
    ADD COLUMN IF NOT EXISTS sunat_response_code VARCHAR(40),
    ADD COLUMN IF NOT EXISTS sunat_response_description TEXT,
    ADD COLUMN IF NOT EXISTS sunat_sent_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS sunat_accepted_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_comprobantes_sunat_retry ON comprobantes(estado_sunat, sunat_next_retry_at);
CREATE INDEX IF NOT EXISTS idx_comprobantes_sunat_response ON comprobantes(sunat_response_code);
CREATE INDEX IF NOT EXISTS idx_nc_sunat_retry ON notas_credito(estado_sunat, sunat_next_retry_at);
CREATE INDEX IF NOT EXISTS idx_nd_sunat_retry ON notas_debito(estado_sunat, sunat_next_retry_at);
CREATE INDEX IF NOT EXISTS idx_sunat_jobs_response ON sunat_submission_jobs(sunat_response_code);
