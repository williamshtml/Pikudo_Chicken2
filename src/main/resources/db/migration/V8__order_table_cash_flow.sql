ALTER TABLE pedidos
    ADD COLUMN IF NOT EXISTS estado_operativo VARCHAR(30),
    ADD COLUMN IF NOT EXISTS estado_pago VARCHAR(30),
    ADD COLUMN IF NOT EXISTS order_code VARCHAR(40),
    ADD COLUMN IF NOT EXISTS tracking_code VARCHAR(80),
    ADD COLUMN IF NOT EXISTS source VARCHAR(30),
    ADD COLUMN IF NOT EXISTS service_type VARCHAR(30),
    ADD COLUMN IF NOT EXISTS subtotal NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS discount_total NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS tax_total NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS delivery_fee NUMERIC(10, 2);

UPDATE pedidos
SET estado_operativo = CASE estado
        WHEN 'PENDING' THEN 'UNREAD'
        WHEN 'ON_DELIVERY' THEN 'ON_DELIVERY'
        WHEN 'DELIVERED' THEN 'DELIVERED'
        WHEN 'CANCELLED' THEN 'CANCELLED'
        WHEN 'PAID' THEN 'DELIVERED'
        ELSE 'UNREAD'
    END,
    estado_pago = CASE estado
        WHEN 'PAID' THEN 'PAID'
        WHEN 'CANCELLED' THEN 'VOIDED'
        ELSE 'UNPAID'
    END,
    order_code = COALESCE(order_code, 'ORD-' || LPAD(id::TEXT, 8, '0')),
    tracking_code = COALESCE(tracking_code, 'TRK-' || LPAD(id::TEXT, 8, '0')),
    source = COALESCE(source, CASE WHEN tipo_pedido = 'DELIVERY' THEN 'WEB' ELSE 'DINE_IN' END),
    service_type = COALESCE(service_type, CASE WHEN tipo_pedido = 'DELIVERY' THEN 'DELIVERY' ELSE 'DINE_IN' END),
    subtotal = COALESCE(subtotal, total),
    discount_total = COALESCE(discount_total, 0),
    tax_total = COALESCE(tax_total, 0),
    delivery_fee = COALESCE(delivery_fee, 0);

ALTER TABLE pedidos
    ALTER COLUMN estado_operativo SET NOT NULL,
    ALTER COLUMN estado_pago SET NOT NULL,
    ALTER COLUMN order_code SET NOT NULL,
    ALTER COLUMN source SET NOT NULL,
    ALTER COLUMN service_type SET NOT NULL,
    ALTER COLUMN subtotal SET NOT NULL,
    ALTER COLUMN discount_total SET NOT NULL,
    ALTER COLUMN tax_total SET NOT NULL,
    ALTER COLUMN delivery_fee SET NOT NULL;

ALTER TABLE detalles_pedido
    ADD COLUMN IF NOT EXISTS variante_id BIGINT REFERENCES producto_variantes(id),
    ADD COLUMN IF NOT EXISTS producto_nombre_snapshot VARCHAR(160),
    ADD COLUMN IF NOT EXISTS variante_nombre_snapshot VARCHAR(120),
    ADD COLUMN IF NOT EXISTS precio_unitario_snapshot NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS line_total NUMERIC(10, 2);

UPDATE detalles_pedido d
SET producto_nombre_snapshot = COALESCE(d.producto_nombre_snapshot, p.nombre),
    variante_nombre_snapshot = COALESCE(d.variante_nombre_snapshot, pv.nombre, p.nombre),
    precio_unitario_snapshot = COALESCE(d.precio_unitario_snapshot, d.precio_unitario),
    discount_amount = COALESCE(d.discount_amount, 0),
    tax_amount = COALESCE(d.tax_amount, 0),
    line_total = COALESCE(d.line_total, d.subtotal),
    variante_id = COALESCE(d.variante_id, pv.id)
FROM productos p
LEFT JOIN LATERAL (
    SELECT id, nombre
    FROM producto_variantes
    WHERE producto_id = p.id
    ORDER BY orden ASC, id ASC
    LIMIT 1
) pv ON true
WHERE d.producto_id = p.id;

ALTER TABLE detalles_pedido
    ALTER COLUMN producto_nombre_snapshot SET NOT NULL,
    ALTER COLUMN precio_unitario_snapshot SET NOT NULL,
    ALTER COLUMN discount_amount SET NOT NULL,
    ALTER COLUMN tax_amount SET NOT NULL,
    ALTER COLUMN line_total SET NOT NULL;

CREATE TABLE IF NOT EXISTS table_sessions (
    id BIGSERIAL PRIMARY KEY,
    mesa_id BIGINT NOT NULL REFERENCES mesas(id),
    opened_by_usuario_id BIGINT REFERENCES usuarios(id),
    closed_by_usuario_id BIGINT REFERENCES usuarios(id),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    guest_count INTEGER,
    notes VARCHAR(300),
    opened_at TIMESTAMP NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP
);

ALTER TABLE pedidos
    ADD COLUMN IF NOT EXISTS table_session_id BIGINT REFERENCES table_sessions(id);

CREATE TABLE IF NOT EXISTS order_item_modifiers (
    id BIGSERIAL PRIMARY KEY,
    detalle_pedido_id BIGINT NOT NULL REFERENCES detalles_pedido(id) ON DELETE CASCADE,
    modifier_id BIGINT REFERENCES modifiers(id),
    modifier_group_id BIGINT REFERENCES modifier_groups(id),
    modifier_group_name_snapshot VARCHAR(120),
    modifier_name_snapshot VARCHAR(120) NOT NULL,
    extra_price_snapshot NUMERIC(10, 2) NOT NULL DEFAULT 0,
    quantity INTEGER NOT NULL DEFAULT 1,
    total_extra NUMERIC(10, 2) NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    changed_by_usuario_id BIGINT REFERENCES usuarios(id),
    reason VARCHAR(300),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_pedidos_order_code ON pedidos(order_code);
CREATE INDEX IF NOT EXISTS idx_pedidos_estado_operativo ON pedidos(estado_operativo);
CREATE INDEX IF NOT EXISTS idx_pedidos_estado_pago ON pedidos(estado_pago);
CREATE INDEX IF NOT EXISTS idx_pedidos_fecha_creacion ON pedidos(fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_pedidos_mesa_estado_operativo ON pedidos(mesa_id, estado_operativo);
CREATE INDEX IF NOT EXISTS idx_pedidos_tracking_code ON pedidos(tracking_code);
CREATE INDEX IF NOT EXISTS idx_pedidos_table_session ON pedidos(table_session_id);
CREATE INDEX IF NOT EXISTS idx_detalles_pedido_variante ON detalles_pedido(variante_id);
CREATE INDEX IF NOT EXISTS idx_order_item_modifiers_detalle ON order_item_modifiers(detalle_pedido_id);
CREATE INDEX IF NOT EXISTS idx_order_status_history_pedido_fecha ON order_status_history(pedido_id, fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_table_sessions_mesa_status ON table_sessions(mesa_id, status);
