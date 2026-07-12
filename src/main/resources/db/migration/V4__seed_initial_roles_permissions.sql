INSERT INTO roles (nombre)
VALUES ('ADMINISTRADOR'), ('CAJERO'), ('MOZO'), ('MOTORIZADO')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO permissions (code, description)
VALUES
    ('AUTH_ME_READ', 'Consultar la sesion autenticada'),
    ('USERS_MANAGE', 'Gestionar usuarios internos'),
    ('ROLES_READ', 'Consultar roles internos'),
    ('CATALOG_READ', 'Consultar catalogo'),
    ('CATALOG_MANAGE', 'Gestionar catalogo'),
    ('ORDERS_READ', 'Consultar pedidos'),
    ('ORDERS_MANAGE', 'Gestionar pedidos'),
    ('PAYMENTS_MANAGE', 'Gestionar pagos y caja'),
    ('DELIVERY_READ', 'Consultar delivery'),
    ('DELIVERY_MANAGE', 'Gestionar delivery'),
    ('TRACKING_UPDATE', 'Actualizar tracking propio'),
    ('INVENTORY_READ', 'Consultar inventario'),
    ('INVENTORY_MANAGE', 'Gestionar inventario'),
    ('REPORTS_READ', 'Consultar reportes'),
    ('BACKUPS_MANAGE', 'Gestionar backups'),
    ('AUDIT_READ', 'Consultar auditoria')
ON CONFLICT (code) DO UPDATE
SET description = EXCLUDED.description,
    enabled = TRUE;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.nombre = 'ADMINISTRADOR'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'AUTH_ME_READ',
    'CATALOG_READ',
    'ORDERS_READ',
    'ORDERS_MANAGE',
    'PAYMENTS_MANAGE',
    'DELIVERY_READ',
    'DELIVERY_MANAGE',
    'REPORTS_READ'
)
WHERE r.nombre = 'CAJERO'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'AUTH_ME_READ',
    'CATALOG_READ',
    'ORDERS_READ',
    'ORDERS_MANAGE'
)
WHERE r.nombre = 'MOZO'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'AUTH_ME_READ',
    'DELIVERY_READ',
    'TRACKING_UPDATE'
)
WHERE r.nombre = 'MOTORIZADO'
ON CONFLICT DO NOTHING;
