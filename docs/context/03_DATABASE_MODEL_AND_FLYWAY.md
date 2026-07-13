# 03 - Database Model and Flyway

## Objetivo

Migrar el backend hacia PostgreSQL con modelo normalizado, migraciones versionadas y control de cambios real.

## Principios

1. PostgreSQL sera la fuente principal de verdad.
2. Flyway debe controlar toda estructura de base de datos.
3. `spring.jpa.hibernate.ddl-auto=update` no debe usarse en produccion.
4. Mantener auditoria en tablas principales.
5. Mantener historial de estados para pedidos, delivery, comprobantes y acciones criticas.
6. Guardar snapshots de producto/precio/impuestos en detalle de pedido.
7. Separar operacion diaria de analitica futura.
8. Disenar pensando en anos de historico.
9. No guardar binarios grandes en PostgreSQL; guardar metadata y punteros a storage.

## Estado actual de migraciones

La rama operativa ya usa:

```text
V1__init_extensions.sql
V2__baseline_existing_schema.sql
V3__create_security_refresh_audit_tables.sql
V4__seed_initial_roles_permissions.sql
```

No editar esas migraciones si ya fueron aplicadas. Toda evolucion debe entrar como `V5`, `V6`, etc.

## Normalizacion progresiva

Agregar tablas nuevas sin destruir funcionalidad:

- `storage_files`
- `product_variants`
- `product_prices`
- `product_images`
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
- `electronic_documents`
- `electronic_document_files`
- `debit_notes`
- `backup_jobs`
- `outbox_events`

## Tablas objetivo principales

### Seguridad

- `usuarios`
- `roles`
- `permissions`
- `role_permissions`
- `refresh_tokens`
- `audit_logs`

El sistema permite multiples administradores porque muchos usuarios pueden referenciar el mismo rol `ADMINISTRADOR`.

### Storage

`storage_files` debe guardar metadata comun:

- `id uuid`
- `provider varchar(30)` (`local`, `google_drive`)
- `bucket_or_drive varchar(120)`
- `folder_id varchar(160)`
- `folder_path varchar(500)`
- `external_file_id varchar(200)`
- `public_url text`
- `download_url text`
- `filename varchar(255)`
- `mime_type varchar(120)`
- `size_bytes bigint`
- `checksum_sha256 varchar(64)`
- `owner_module varchar(60)`
- `owner_id varchar(80)`
- `created_by_usuario_id bigint`
- `created_at timestamptz`

### Catalogo

- `product_categories`
- `products`
- `product_variants`
- `product_prices`
- `product_images`
- `modifier_groups`
- `modifiers`
- `product_modifier_groups`
- `combo_components`

`product_images` debe apuntar a `storage_files`, no a rutas locales directas.

### Pedidos

- `orders` o evolucion controlada de `pedidos`
- `order_items` o evolucion controlada de `detalles_pedido`
- `order_item_modifiers`
- `order_status_history`

Separar:

- Estado operativo del pedido.
- Estado de pago/cobro.
- Estado de comprobante/SUNAT.

### Caja y pagos

- `cash_registers`
- `cash_sessions`
- `payment_methods`
- `payments`
- `payment_transactions`

### Comprobantes SUNAT

- `electronic_documents`
- `electronic_document_files`
- `credit_notes`
- `debit_notes`

Tipos objetivo:

- `FACTURA`
- `BOLETA_SIMPLE`
- `BOLETA_CON_DOCUMENTO`
- `NOTA_CREDITO`
- `NOTA_DEBITO`

Archivos objetivo:

- `XML_SIGNED`
- `CDR`
- `PDF`

Los archivos deben apuntar a `storage_files`.

### Delivery y GPS

- `drivers`
- `vehicles`
- `delivery_zones`
- `deliveries`
- `delivery_stops`
- `delivery_location_events`
- `delivery_evidence`

`delivery_location_events` debe conservar historico solo cuando exista entrega activa. Redis conserva la ultima ubicacion caliente con TTL.

### Notificaciones, auditoria y backups

- `notification_devices`
- `notifications`
- `outbox_events`
- `backup_jobs`

## Convencion Flyway futura

La numeracion exacta depende de lo ya aplicado en la rama. Propuesta desde el estado actual:

```text
V5__create_storage_files.sql
V6__create_catalog_normalized_tables.sql
V7__create_order_lifecycle_tables.sql
V8__create_electronic_documents_tables.sql
V9__create_delivery_tracking_tables.sql
V10__create_notifications_outbox_backup_tables.sql
```

## Reglas Flyway

- No editar migraciones ya aplicadas.
- Cada cambio estructural debe ser una nueva migracion.
- Separar estructura de datos seed cuando sea posible.
- Todo cambio debe poder reconstruir la DB desde cero.
- Mantener scripts de rollback manual para cambios riesgosos.
- Antes de crear una migracion, revisar si el baseline actual ya tiene la tabla antigua equivalente.

## Indices minimos

- `pedidos(estado)`
- `pedidos(fecha_creacion)`
- `pedidos(repartidor_id, estado)`
- `order_status_history(order_id, created_at)`
- `products(category_id)`
- `product_prices(variant_id, valid_from, valid_to)`
- `storage_files(owner_module, owner_id)`
- `storage_files(provider, external_file_id)`
- `electronic_documents(document_type, serie, correlativo)` unico.
- `electronic_documents(sunat_status, issued_at)`
- `electronic_document_files(document_id, file_type)`
- `payments(order_id)`
- `deliveries(order_id)`
- `deliveries(driver_id, status)`
- `delivery_location_events(delivery_id, recorded_at)`
- `delivery_location_events(driver_id, recorded_at)`
- `stock_movements(inventory_item_id, created_at)`
- `outbox_events(status, created_at)`
- `backup_jobs(started_at, status)`

## Datos geograficos

Para GPS se recomienda PostGIS:

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

Usar `geography(Point, 4326)` para puntos y `geography(Polygon, 4326)` para zonas cuando se implemente tracking completo.

## Regla sobre UUID vs Long

El plan objetivo prefiere UUID para tablas nuevas de integraciones, storage, tracking, outbox, backups y eventos. El proyecto actual usa Long en entidades principales. No migrar todos los IDs sin analisis; priorizar estabilidad.
