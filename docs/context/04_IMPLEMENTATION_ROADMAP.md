# 04 — Implementation Roadmap

## Objetivo de esta guía

Dar a Codex una ruta clara para avanzar sin destruir lo ya trabajado.

## Fase 0 — Contexto y limpieza controlada

Entregables:

- `AGENTS.md`.
- Documentación en `docs/context/`.
- Decisiones técnicas consolidadas.
- Auditoría del estado actual.

Criterio de aceptación:

- Cualquier agente o desarrollador puede entender qué se está construyendo antes de tocar código.

## Fase 1 — Infraestructura local

Entregables:

- Docker Compose con PostgreSQL, Redis, Kafka y backend.
- Variables de entorno.
- Perfiles de Spring.
- Health check.
- README de ejecución local.

Criterio de aceptación:

- El proyecto puede levantarse localmente sin depender de MySQL Workbench.

## Fase 2 — PostgreSQL + Flyway

Entregables:

- Dependencia PostgreSQL.
- Flyway configurado.
- Migraciones iniciales.
- Desactivación de `ddl-auto=update` para perfiles serios.
- Seed de roles y permisos.

Criterio de aceptación:

- La base se reconstruye desde cero solo con migraciones.

## Fase 3 — Seguridad y configuración

Entregables:

- JWT revisado.
- Refresh tokens.
- Roles y permisos normalizados.
- CORS configurable.
- Credenciales fuera del código.
- Auditoría de login.

Criterio de aceptación:

- Endpoints internos protegidos y configurables por ambiente.

## Fase 4 — Catálogo real

Entregables:

- Categorías reales de Pikudo.
- Productos.
- Variantes.
- Precios históricos.
- Modificadores.
- Combos.
- Imágenes.

Criterio de aceptación:

- La carta de Pikudo se puede cargar sin forzar un modelo plano de producto/precio.

## Fase 5 — Pedidos, mesas y caja

Entregables:

- Pedidos por salón, teléfono, WhatsApp, recojo y delivery.
- Mesas y sesiones de mesa.
- Estados de pedido.
- Historial de estados.
- Pagos.
- Cierre de caja básico.

Criterio de aceptación:

- Un pedido puede crearse, confirmarse, prepararse, pagarse y cerrarse.

## Fase 6 — Promociones, descuentos y eventos

Entregables:

- Eventos comerciales.
- Promociones.
- Cupones.
- Descuentos por producto, categoría, método de pago o total.
- Registro de descuento aplicado.

Criterio de aceptación:

- Las promociones aplican solo si están vigentes y cumplen reglas.

## Fase 7 — Inventario y recetas

Entregables:

- Insumos.
- Unidades.
- Almacenes.
- Recetas por variante.
- Movimientos de stock.
- Descuento por venta.
- Alertas de stock bajo.

Criterio de aceptación:

- La venta de productos puede generar consumo de insumos.

## Fase 8 — Delivery y GPS

Entregables:

- Motorizados.
- Vehículos.
- Zonas.
- Entregas.
- Última ubicación en Redis.
- Histórico en PostgreSQL.
- WebSocket para tracking.
- Deep link a Google Maps.

Criterio de aceptación:

- El repartidor reporta ubicación solo con entrega activa y el panel ve el avance.

## Fase 9 — Backups, auditoría y hardening

Entregables:

- Backup diario.
- Registro en `backup_jobs`.
- Checksum SHA-256.
- Retención.
- Prueba de restauración.
- Auditoría de acciones críticas.

Criterio de aceptación:

- Existe evidencia de backup y restauración documentada.

## Fase 10 — Reportes y analítica futura

Entregables:

- Ventas diarias.
- Ventas mensuales.
- Productos más vendidos.
- Métodos de pago.
- Delivery.
- Inventario.
- Tablas resumen.
- Ruta futura a BigQuery.

Criterio de aceptación:

- Los reportes no bloquean la operación diaria.
