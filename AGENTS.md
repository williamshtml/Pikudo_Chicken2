# AGENTS.md — Contexto operativo para Codex

## Propósito del repositorio

Este repositorio corresponde al backend actual de **Pikudo Chicken**, una solución a medida para un restaurante concreto. No debe tratarse como SaaS multi-tenant de Studios TKOH. El objetivo actual es convertir el avance existente en una API profesional, self-hosted, preparada para ejecutarse en un servidor o PC del restaurante.

La solución debe cubrir administración, caja, catálogo, productos, promociones, descuentos, eventos, pedidos, mesas, pagos, delivery, tracking GPS, inventario, reportes, auditoría y backups diarios.

## Estado actual detectado

- El proyecto backend usa Maven y Spring Boot.
- Actualmente está orientado a Spring MVC + JPA + MySQL.
- Ya existen módulos y clases para autenticación, usuarios, roles, productos, categorías, pedidos, caja, mesas, inventario, reportes, pagos, delivery, tracking, notificaciones, Culqi, Google Maps y SUNAT.
- Existe un frontend Angular dentro de `fronted/`, pero **no debe crecer ni ser tomado como stack objetivo**.
- El objetivo técnico del nuevo plan es PostgreSQL + Flyway + arquitectura multicapas modular.

## Decisión técnica principal

No iniciar con microservicios. Para Pikudo Chicken corresponde un **modular monolith multicapas**, porque el sistema será instalado y operado para un restaurante específico. La separación debe ser interna por módulos, no por despliegues independientes.

Microservicios solo serán considerados cuando existan varios locales, alta carga de tracking, reportes pesados, BI independiente o necesidad real de despliegues separados.

## Stack objetivo backend

- Java 21 LTS como objetivo de modernización.
- Spring Boot 3.x.
- Spring WebFlux como objetivo progresivo para flujos reactivos, tracking y WebSocket.
- PostgreSQL como base de datos principal.
- Flyway para migraciones versionadas.
- Redis para caché, estados temporales y última ubicación de delivery.
- Kafka para eventos internos importantes.
- Spring Security + JWT + refresh tokens.
- OpenAPI / Swagger para contrato de endpoints.
- Testcontainers para pruebas de integración.
- Docker Compose para entorno local y despliegue self-hosted.

## Stack frontend objetivo

- Panel administrativo desktop: React + TypeScript + Vite + Tauri.
- Landing pública: Next.js + React + TypeScript.
- App delivery: Flutter.
- Angular no debe seguir creciendo. Si se mantiene algo temporal, debe considerarse legacy y no base del producto final.

## Regla sobre Angular

No crear nuevas pantallas ni arquitectura nueva en Angular. El frontend Angular existente puede revisarse solo como referencia funcional, pero el camino objetivo es React + TypeScript + Vite + Tauri para desktop y Next.js para landing.

## Arquitectura interna obligatoria

Usar arquitectura multicapas:

```text
api/controller
application/service/usecase
domain/model/rules
infrastructure/persistence/integration/events
```

Separar por módulos de negocio:

```text
identity
restaurant
catalog
pricing
orders
payments
tables
delivery
tracking
inventory
notifications
reports
backups
audit
shared
```

## Reglas de implementación

1. No hacer rewrites masivos sin necesidad.
2. Priorizar migración incremental desde el estado actual.
3. No mantener `ddl-auto=update` como estrategia final de base de datos.
4. Introducir Flyway antes de cambios fuertes de modelo.
5. Migrar MySQL a PostgreSQL como cambio estructural temprano.
6. Mantener consistencia transaccional en pedidos, pagos e inventario.
7. Usar snapshots de nombre/precio en detalles de pedido.
8. Nunca confiar en precios enviados por el frontend.
9. Registrar historial de estados de pedido y delivery.
10. El GPS solo debe activarse con delivery activo.
11. Los backups deben ser restaurables, no solo generados.
12. Los eventos Kafka deben representar hechos de dominio, no reemplazar la base transaccional.

## Prioridad de trabajo para Codex

Primero ordenar el contexto y la base del proyecto. Luego implementar infraestructura. Después migrar módulos por fases.

Orden recomendado:

1. Documentación de contexto y decisiones técnicas.
2. Docker Compose base.
3. PostgreSQL + Flyway.
4. Refactor de configuración y perfiles.
5. Seguridad JWT y roles.
6. Catálogo normalizado.
7. Pedidos, mesas y caja.
8. Promociones, descuentos y eventos.
9. Delivery y tracking.
10. Backups, auditoría y reportes.

## Archivos de contexto relevantes

Leer antes de modificar código:

- `docs/context/MASTER_BACKEND_PLAN.md`
- `docs/context/00_PROJECT_BRIEF.md`
- `docs/context/01_CURRENT_STATE_AUDIT.md`
- `docs/context/02_TARGET_ARCHITECTURE.md`
- `docs/context/03_DATABASE_MODEL_AND_FLYWAY.md`
- `docs/context/04_IMPLEMENTATION_ROADMAP.md`
- `docs/context/05_GPS_DELIVERY_TRACKING.md`
- `docs/context/06_DEVOPS_BACKUPS_OBSERVABILITY.md`
- `docs/context/07_CODEX_TASKS.md`

## Prohibiciones fuertes

- No convertir este proyecto en SaaS multi-tenant en esta fase.
- No seguir expandiendo Angular.
- No meter microservicios desde el inicio.
- No usar `ddl-auto=update` como solución final.
- No guardar contraseñas sin hashing fuerte.
- No almacenar ubicaciones de repartidor fuera de entregas activas.
- No romper endpoints existentes sin documentar migración.
- No crear lógica de negocio en controladores.
- No mezclar DTOs, entidades y reglas de negocio sin separación.

## Criterio de calidad

Un cambio es aceptable si mejora la mantenibilidad, respeta la arquitectura multicapas, no rompe el flujo operativo del restaurante y acerca el proyecto al plan técnico de Pikudo Chicken.
