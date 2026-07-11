# 03 — Database Model and Flyway

## Objetivo

Migrar el backend hacia PostgreSQL con modelo normalizado, migraciones versionadas y control de cambios real.

## Principios

1. PostgreSQL será la fuente principal de verdad.
2. Flyway debe controlar toda estructura de base de datos.
3. `spring.jpa.hibernate.ddl-auto=update` no debe usarse en producción.
4. Mantener auditoría en tablas principales.
5. Mantener historial de estados para pedidos y delivery.
6. Guardar snapshots de producto/precio en detalle de pedido.
7. Separar operación diaria de analítica futura.
8. Diseñar pensando en 5 años de histórico.

## Migración desde estado actual

El repo actual usa MySQL + JPA. La migración recomendada es incremental:

### Paso 1: Infraestructura

- Agregar dependencia PostgreSQL.
- Agregar Flyway.
- Crear perfil `local-postgres`.
- Agregar Docker Compose con PostgreSQL.
- Desactivar `ddl-auto=update` en perfiles serios.

### Paso 2: Migración base

Crear migraciones iniciales que repliquen el modelo existente:

```text
V1__init_extensions.sql
V2__create_security_tables.sql
V3__create_catalog_tables.sql
V4__create_orders_tables.sql
V5__create_payments_tables.sql
V6__create_delivery_tables.sql
V7__seed_roles_permissions.sql
```

### Paso 3: Normalización progresiva

Agregar tablas nuevas sin destruir funcionalidad:

- `product_variants`
- `product_prices`
- `modifier_groups`
- `modifiers`
- `combo_components`
- `commercial_events`
- `promotions`
- `promotion_targets`
- `applied_discounts`
- `order_status_history`
- `deliveries`
- `delivery_location_events`
- `backup_jobs`
- `outbox_events`
- `audit_logs`

## Tablas objetivo principales

### Núcleo

- `restaurants`
- `branches`
- `business_settings`

Aunque el sistema no sea SaaS, mantener `restaurants` y `branches` permite que Pikudo crezca a una segunda sede sin rehacer toda la base.

### Seguridad

- `users`
- `roles`
- `permissions`
- `user_roles`
- `role_permissions`
- `refresh_tokens`

### Catálogo

- `product_categories`
- `products`
- `product_variants`
- `product_prices`
- `product_images`
- `modifier_groups`
- `modifiers`
- `product_modifier_groups`
- `combo_components`

### Promociones y eventos

- `commercial_events`
- `promotions`
- `promotion_targets`
- `coupons`
- `applied_discounts`

### Pedidos

- `orders`
- `order_items`
- `order_item_modifiers`
- `order_status_history`

### Caja y pagos

- `cash_registers`
- `cash_sessions`
- `payment_methods`
- `payments`
- `payment_transactions`

### Comprobantes

- `receipts`
- `credit_notes`

### Inventario

- `measurement_units`
- `warehouses`
- `inventory_items`
- `stock_movements`
- `recipes`
- `recipe_items`

### Delivery y GPS

- `drivers`
- `vehicles`
- `delivery_zones`
- `deliveries`
- `delivery_stops`
- `delivery_location_events`
- `delivery_evidence`

### Notificaciones, auditoría y backups

- `notification_devices`
- `notifications`
- `audit_logs`
- `outbox_events`
- `backup_jobs`

## Convención Flyway

```text
src/main/resources/db/migration/
  V1__init_extensions.sql
  V2__create_core_restaurant_tables.sql
  V3__create_security_tables.sql
  V4__create_catalog_tables.sql
  V5__create_promotions_tables.sql
  V6__create_tables_and_orders.sql
  V7__create_payments_and_receipts.sql
  V8__create_inventory_tables.sql
  V9__create_delivery_tracking_tables.sql
  V10__create_notifications_outbox_audit_backup.sql
  V11__seed_initial_roles_permissions.sql
  V12__seed_pikudo_initial_categories.sql
```

## Reglas Flyway

- No editar migraciones ya aplicadas.
- Cada cambio estructural debe ser una nueva migración.
- Separar estructura de datos seed cuando sea posible.
- Todo cambio debe poder reconstruir la DB desde cero.
- Mantener scripts de rollback manual para cambios riesgosos.

## Índices mínimos

- `orders(branch_id, created_at)`
- `orders(status)`
- `orders(tracking_code)` único.
- `order_status_history(order_id, created_at)`
- `products(branch_id, category_id)`
- `products(product_type, is_available)`
- `product_prices(variant_id, valid_from, valid_to)`
- `payments(order_id)`
- `deliveries(order_id)`
- `delivery_location_events(delivery_id, recorded_at)`
- `delivery_location_events(driver_id, recorded_at)`
- `stock_movements(inventory_item_id, created_at)`
- `outbox_events(status, created_at)`
- `backup_jobs(started_at, status)`

## Datos geográficos

Para GPS se recomienda PostGIS:

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

Usar `geography(Point, 4326)` para puntos y `geography(Polygon, 4326)` para zonas.

## Regla sobre UUID vs Long

El plan objetivo prefiere UUID. El proyecto actual usa Long. No migrar todos los IDs sin análisis. Priorizar estabilidad. Para nuevas tablas de tracking, outbox, backups y eventos se puede usar UUID desde el inicio si no rompe relaciones existentes.
