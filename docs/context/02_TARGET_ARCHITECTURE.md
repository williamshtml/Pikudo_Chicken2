# 02 — Target Backend Architecture

## Decisión central

El backend debe construirse como **modular monolith multicapas**.

No iniciar con microservicios porque el sistema será self-hosted para un restaurante específico. Los microservicios aumentarían despliegue, monitoreo, logs, seguridad, recuperación ante fallos y soporte técnico sin una ganancia real para esta etapa.

## Vista lógica

```text
[React + Vite + Tauri Desktop]
              |
[Next.js Landing Pública]
              |
[Flutter Delivery App]
              |
        [Nginx / Reverse Proxy]
              |
[Spring Boot API modular multicapas]
              |
  --------------------------------------
  |            |          |             |
PostgreSQL   Redis      Kafka     Local Storage
  |                                   |
Backups diarios                  Evidencias / imágenes
  |
Export futuro a BigQuery
```

## Capas

### API Layer

Responsable de exponer endpoints REST y WebSocket.

Debe contener:

- Controllers.
- Request DTOs.
- Response DTOs.
- Validación de entrada.
- OpenAPI annotations.

No debe contener reglas de negocio complejas.

### Application Layer

Responsable de casos de uso.

Ejemplos:

- Crear pedido.
- Confirmar pedido.
- Cambiar estado.
- Registrar pago.
- Asignar delivery.
- Registrar ubicación.
- Ejecutar backup.

### Domain Layer

Responsable de reglas de negocio.

Ejemplos:

- Un pedido entregado no vuelve a preparación.
- Una promoción vencida no aplica.
- Un mozo no cobra pedidos.
- Un delivery solo reporta GPS con entrega activa.
- Un producto sin insumo crítico puede marcarse no disponible.

### Infrastructure Layer

Responsable de adaptadores técnicos.

Incluye:

- Persistencia PostgreSQL.
- Redis.
- Kafka.
- Google Maps / Routes.
- Firebase Cloud Messaging.
- Culqi.
- SUNAT.
- Storage local.
- Backup runner.

## Convención de paquetes objetivo

```text
com.studiostkoh.pikudo
  config
  shared
    api
    application
    domain
    infrastructure
    security
    events
  identity
    api
    application
    domain
    infrastructure
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
```

## Compatibilidad con el repo actual

El repo actual usa `com.pikudo`. No renombrar todo de golpe. Se recomienda:

1. Mantener `com.pikudo` mientras se ordena infraestructura.
2. Crear subpaquetes modulares dentro de `com.pikudo`.
3. Evitar cambios masivos de paquete que dificulten revisión.
4. Evaluar cambio a `com.studiostkoh.pikudo` solo cuando el backend esté estable.

## Uso de WebFlux

El proyecto actual usa Spring MVC. El objetivo menciona WebFlux. Para evitar roturas:

1. No mezclar WebMVC y WebFlux sin decisión clara.
2. Si se migra a WebFlux, planificarlo como fase técnica dedicada.
3. Para MVP self-hosted se puede mantener MVC inicialmente y usar WebSocket para tiempo real.
4. WebFlux se justifica más en tracking, notificaciones, streaming y endpoints I/O intensivos.

## Kafka

Kafka debe usarse para eventos de dominio importantes:

- `OrderCreated`
- `OrderStatusChanged`
- `PaymentRegistered`
- `DeliveryAssigned`
- `DriverLocationUpdated`
- `StockMovementCreated`
- `BackupCompleted`
- `PromotionApplied`

No usar Kafka para todo. PostgreSQL sigue siendo la fuente de verdad.

## Redis

Redis debe usarse para:

- Caché de carta.
- Estado actual de mesas.
- Última ubicación de delivery.
- Rate limiting.
- Tracking público.
- Locks operativos.

## Storage local

Guardar localmente:

- Imágenes de producto.
- Evidencias de entrega.
- Backups.
- Logs rotativos.

No guardar binarios grandes dentro de PostgreSQL salvo necesidad concreta.
