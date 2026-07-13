CREATE TABLE IF NOT EXISTS deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id BIGINT NOT NULL UNIQUE REFERENCES pedidos(id),
    driver_id BIGINT REFERENCES usuarios(id),
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    tracking_code VARCHAR(80) NOT NULL UNIQUE,
    destination_address VARCHAR(255),
    destination_reference VARCHAR(255),
    customer_phone VARCHAR(20),
    current_latitude NUMERIC(10, 7),
    current_longitude NUMERIC(10, 7),
    last_location_at TIMESTAMP,
    assigned_at TIMESTAMP,
    accepted_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    eta_minutes INTEGER,
    distance_meters INTEGER,
    created_by_usuario_id BIGINT REFERENCES usuarios(id),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    CONSTRAINT ck_deliveries_status CHECK (status IN (
        'CREATED',
        'ASSIGNED',
        'ACCEPTED',
        'REJECTED',
        'PICKED_UP',
        'ON_DELIVERY',
        'NEAR_CUSTOMER',
        'DELIVERED',
        'CANCELLED'
    ))
);

CREATE TABLE IF NOT EXISTS delivery_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id),
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by_usuario_id BIGINT REFERENCES usuarios(id),
    reason VARCHAR(255),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS delivery_location_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id),
    driver_id BIGINT NOT NULL REFERENCES usuarios(id),
    latitude NUMERIC(10, 7) NOT NULL,
    longitude NUMERIC(10, 7) NOT NULL,
    accuracy_meters NUMERIC(8, 2),
    speed_meters_per_second NUMERIC(8, 2),
    heading_degrees NUMERIC(6, 2),
    recorded_at TIMESTAMP NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT NOW(),
    source VARCHAR(30) NOT NULL DEFAULT 'MOBILE'
);

CREATE TABLE IF NOT EXISTS delivery_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id),
    storage_file_id UUID NOT NULL REFERENCES storage_files(id),
    evidence_type VARCHAR(40) NOT NULL,
    uploaded_by_usuario_id BIGINT REFERENCES usuarios(id),
    notes VARCHAR(255),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status);
CREATE INDEX IF NOT EXISTS idx_deliveries_driver_status ON deliveries(driver_id, status);
CREATE INDEX IF NOT EXISTS idx_deliveries_tracking_code ON deliveries(tracking_code);
CREATE INDEX IF NOT EXISTS idx_delivery_history_delivery_time ON delivery_status_history(delivery_id, fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_delivery_location_delivery_time ON delivery_location_events(delivery_id, recorded_at);
CREATE INDEX IF NOT EXISTS idx_delivery_location_driver_time ON delivery_location_events(driver_id, recorded_at);
