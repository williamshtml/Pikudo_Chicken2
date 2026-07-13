CREATE TABLE IF NOT EXISTS order_payments (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    caja_turno_id BIGINT NOT NULL REFERENCES cajas_turnos(id),
    metodo_pago_id BIGINT NOT NULL REFERENCES metodos_pago(id),
    monto NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    external_reference VARCHAR(120),
    notes VARCHAR(300),
    received_by_usuario_id BIGINT REFERENCES usuarios(id),
    voided_by_usuario_id BIGINT REFERENCES usuarios(id),
    void_reason VARCHAR(300),
    voided_at TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    CONSTRAINT ck_order_payments_monto_positive CHECK (monto > 0),
    CONSTRAINT ck_order_payments_status CHECK (status IN ('CONFIRMED', 'VOIDED', 'REFUNDED'))
);

CREATE TABLE IF NOT EXISTS order_discounts (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    detalle_pedido_id BIGINT REFERENCES detalles_pedido(id) ON DELETE CASCADE,
    discount_type VARCHAR(30) NOT NULL,
    requested_value NUMERIC(10, 2) NOT NULL,
    calculated_amount NUMERIC(10, 2) NOT NULL,
    reason VARCHAR(300) NOT NULL,
    authorized_by_usuario_id BIGINT REFERENCES usuarios(id),
    status VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    CONSTRAINT ck_order_discounts_type CHECK (discount_type IN ('MANUAL_AMOUNT', 'MANUAL_PERCENT')),
    CONSTRAINT ck_order_discounts_status CHECK (status IN ('APPLIED', 'VOIDED')),
    CONSTRAINT ck_order_discounts_requested_non_negative CHECK (requested_value >= 0),
    CONSTRAINT ck_order_discounts_calculated_non_negative CHECK (calculated_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_order_payments_pedido_status ON order_payments(pedido_id, status);
CREATE INDEX IF NOT EXISTS idx_order_payments_caja_status_fecha ON order_payments(caja_turno_id, status, fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_order_payments_metodo ON order_payments(metodo_pago_id);
CREATE INDEX IF NOT EXISTS idx_order_discounts_pedido_status ON order_discounts(pedido_id, status);
CREATE INDEX IF NOT EXISTS idx_order_discounts_detalle ON order_discounts(detalle_pedido_id);
