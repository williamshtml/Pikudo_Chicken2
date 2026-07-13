# 02 - Target Backend Architecture

## Decision central

El backend debe construirse como modular monolith multicapas. No iniciar con microservicios porque el sistema sera self-hosted para un restaurante especifico. Los microservicios aumentarian despliegue, monitoreo, logs, seguridad, recuperacion ante fallos y soporte tecnico sin una ganancia real para esta etapa.

## Vista logica

```text
[React + Vite + Tauri Desktop]
              |
[Next.js Landing Publica]
              |
[Flutter Delivery App]
              |
        [Nginx / Reverse Proxy]
              |
[Spring Boot API modular multicapas]
              |
  -------------------------------------------------------
  |            |          |             |               |
PostgreSQL   Redis      Kafka     StorageService   Integrations
  |            |          |             |               |
Backups    Tracking   Eventos     Local/Drive     Resend/SUNAT/Maps
```

PostgreSQL sigue siendo la fuente de verdad. Redis se usa para estado caliente y TTL. Kafka se reserva para hechos de dominio importantes. Google Drive se usa como storage externo de archivos operativos, con local storage como fallback.

## Capas

### API Layer

Responsable de exponer REST y WebSocket:

- Controllers.
- Request DTOs.
- Response DTOs.
- Validacion de entrada.
- OpenAPI annotations.

No debe contener reglas de negocio complejas.

### Application Layer

Responsable de casos de uso:

- Crear pedido.
- Cambiar estado operativo.
- Registrar pago.
- Asignar delivery.
- Registrar ubicacion.
- Subir archivo.
- Enviar email.
- Emitir comprobante.
- Ejecutar backup.

### Domain Layer

Responsable de reglas de negocio:

- Un pedido entregado no vuelve a preparacion.
- Un pedido rechazado no puede cobrarse.
- Una promocion vencida no aplica.
- Un mozo no cobra pedidos.
- Un delivery solo reporta GPS con entrega activa.
- El cliente publico no ve coordenadas exactas del repartidor.
- Un comprobante SUNAT rechazado debe quedar trazable y reintentable segun regla definida.

### Infrastructure Layer

Responsable de adaptadores tecnicos:

- Persistencia PostgreSQL.
- Redis.
- Kafka.
- Google Drive.
- Resend API.
- Google Maps / Routes.
- Firebase Cloud Messaging.
- Culqi.
- SUNAT con Project OpenUBL XBuilder/XSender.
- Storage local fallback.
- Backup runner.

## Convencion de paquetes objetivo

```text
com.pikudo.restaurant
  config
  shared
    api
    application
    domain
    infrastructure
    security
    events
    storage
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
  integrations
```

La normalizacion de raiz a `com.pikudo.restaurant` ya quedo aplicada. La modularizacion profunda debe hacerse incrementalmente creando subpaquetes internos por dominio sin cambiar contratos publicos de golpe.

## Admin y seguridad

- No existe admin unico.
- Varios usuarios pueden compartir el rol `ADMINISTRADOR`.
- El seed de admin es solo bootstrap inicial y debe poder deshabilitarse por entorno.
- Cambios de usuarios, roles, permisos y credenciales deben auditarse.

## Storage

`StorageService` debe ser el unico punto de entrada para guardar binarios nuevos.

Backends:

- `local`: desarrollo, fallback y pruebas.
- `google-drive`: backend principal para produccion self-hosted.

Usos previstos:

- Imagenes de producto.
- Avatares de usuarios/repartidores.
- Evidencias de entrega.
- XML, CDR y PDF de comprobantes SUNAT.
- Backups exportados si se define una politica de copia externa.

No guardar binarios grandes dentro de PostgreSQL salvo necesidad concreta. La base debe guardar metadata, ids externos, checksum y trazabilidad.

## Email

Resend API sera el proveedor principal. SMTP queda como compatibilidad secundaria/no prioritaria.

Casos iniciales:

- Alertas operativas.
- Recuperacion o invitacion de usuarios si se implementa.
- Notificaciones de comprobantes si se habilita envio al cliente.
- Alertas de backup o fallos de integracion.

## SUNAT

La integracion SUNAT se hara con Project OpenUBL:

- XBuilder para crear y firmar XML UBL.
- XSender para enviar comprobantes a SUNAT/OSE.
- `.pfx` y password por variables de entorno seguras.
- Modo `disabled`, `sandbox` y `prod`.

Documentos objetivo:

- Factura.
- Boleta simple sin documento.
- Boleta con documento.
- Nota de credito.
- Nota de debito.

## Pedidos y tracking

Separar estado operativo de pedido y estado de pago.

Estado operativo objetivo:

```text
UNREAD -> READ -> ACCEPTED -> IN_PREPARATION -> READY -> ASSIGNED -> ON_DELIVERY -> NEAR_CUSTOMER -> DELIVERED
                       |              |              |             |             |
                    REJECTED       CANCELLED      CANCELLED     CANCELLED    CANCELLED
```

El cliente publico ve estado, avance, ETA/distancia aproximada y cercania. El panel administrativo y la app Flutter del repartidor usan ubicacion exacta durante entrega activa.

## Uso de WebFlux

El proyecto actual usa Spring MVC. Para evitar roturas:

- No mezclar WebMVC y WebFlux sin fase tecnica dedicada.
- Mantener MVC inicialmente y usar WebSocket para tiempo real.
- Evaluar WebFlux despues de estabilizar pedidos, delivery y storage.

## Kafka

Kafka debe usarse para eventos de dominio importantes:

- `OrderCreated`
- `OrderStatusChanged`
- `PaymentRegistered`
- `DeliveryAssigned`
- `DriverLocationUpdated`
- `ReceiptIssued`
- `ReceiptSunatStatusChanged`
- `StockMovementCreated`
- `BackupCompleted`
- `PromotionApplied`

No usar Kafka para todo. PostgreSQL sigue siendo la fuente de verdad.

## Redis

Redis debe usarse para:

- Cache de carta.
- Estado actual de mesas.
- Ultima ubicacion de delivery.
- Rate limiting.
- Tracking publico.
- Locks operativos.
- TTL de estados temporales.
