# 07 — Codex Tasks for Next Implementation Session

## Objetivo inmediato

Preparar el backend para iniciar la implementación real del plan técnico sin romper el avance del compañero.

## Tarea 1 — Revisar estructura actual

Analizar:

- `pom.xml`
- `src/main/resources/application.properties`
- `src/main/java/com/pikudo/restaurant/PikudoChicken2Application.java`
- `src/main/java/com/pikudo/restaurant/config/SecurityConfig.java`
- `src/main/java/com/pikudo/restaurant/entity/*`
- `src/main/java/com/pikudo/restaurant/controller/*`
- `src/main/java/com/pikudo/restaurant/service/*`
- `src/main/java/com/pikudo/restaurant/repository/*`
- `fronted/package.json`

Salida esperada:

- Lista de módulos actuales.
- Dependencias actuales.
- Riesgos de migración.
- Plan de cambios sin reescribir todo.

## Tarea 2 — Crear infraestructura local

Agregar:

- `docker-compose.yml`
- `.env.example`
- `docs/run-local.md`
- Servicio PostgreSQL.
- Servicio Redis.
- Servicio Kafka.

No conectar todo aún si rompe el backend. Preparar base reproducible.

## Tarea 3 — Migrar configuración a perfiles

Crear:

```text
application.properties
application-local.properties
application-postgres.properties
application-prod.properties
```

Mover credenciales a variables de entorno.

## Tarea 4 — Agregar PostgreSQL y Flyway

Modificar `pom.xml`:

- Agregar PostgreSQL driver.
- Agregar Flyway.
- Evaluar si mantener MySQL temporalmente solo para transición.

Crear:

```text
src/main/resources/db/migration/V1__init_extensions.sql
src/main/resources/db/migration/V2__baseline_existing_schema.sql
```

## Tarea 5 — Congelar Angular como legacy

No borrar `fronted/` todavía.

Agregar nota en documentación:

- Angular queda como referencia/legacy.
- El frontend objetivo será React + Vite + Tauri y Next.js.
- No construir nuevas features en Angular.

## Tarea 6 — Diseñar entidades nuevas sin romper actuales

Priorizar nuevas tablas:

- `order_status_history`
- `deliveries`
- `delivery_location_events`
- `backup_jobs`
- `outbox_events`
- `audit_logs`

Estas tablas aportan valor sin obligar a rediseñar toda la carta de inmediato.

## Tarea 7 — Backups

Crear módulo:

```text
com.pikudo.restaurant.backups
```

Componentes:

- `BackupJob` entity.
- `BackupJobRepository`.
- `BackupService`.
- `BackupScheduler`.
- `BackupController` opcional para admin.

Regla:

- El backend puede auditar y coordinar, pero el proceso real puede ejecutarse por servicio externo/cron para mayor confiabilidad.

## Tarea 8 — Tracking GPS básico

Crear o adaptar:

- Endpoint para registrar ubicación.
- Validación de delivery activo.
- Guardar última ubicación en Redis.
- Guardar histórico en PostgreSQL.
- Evento `DriverLocationUpdated`.

No guardar GPS fuera de entrega activa.

## Tarea 9 — OpenAPI

Asegurar que Swagger documente:

- Auth.
- Productos.
- Pedidos.
- Pagos.
- Delivery.
- Tracking.
- Backups.

## Tarea 10 — No hacer todavía

No hacer aún:

- Reescritura total a WebFlux.
- Microservicios.
- Borrado del frontend Angular.
- Cambio masivo de paquetes.
- Migración global de Long a UUID.
- Integración real con BigQuery.
- Facturación SUNAT completa.

## Prompt recomendado para Codex

```text
Lee AGENTS.md y todos los archivos en docs/context. Analiza el estado actual del backend Spring Boot de Pikudo Chicken y prepara la Fase 1: infraestructura local con PostgreSQL, Redis, Kafka, perfiles de configuración, Flyway inicial y documentación de ejecución. No reescribas módulos funcionales todavía. No expandas Angular. No conviertas el proyecto en microservicios. Mantén la aplicación como modular monolith multicapas.
```
