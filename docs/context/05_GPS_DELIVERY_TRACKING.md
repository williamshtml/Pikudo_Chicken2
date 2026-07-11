# 05 — GPS, Delivery and Tracking Plan

## Objetivo

Permitir que Pikudo Chicken controle entregas, visualice motorizados y ofrezca seguimiento al cliente sin comprometer privacidad ni sobrecargar el servidor local.

## Regla principal

El sistema solo debe recibir ubicación GPS cuando el motorizado tenga una entrega activa.

No guardar tracking permanente del trabajador fuera de una entrega.

## Flujo base

```text
Pedido delivery confirmado
        |
Caja asigna motorizado
        |
Backend crea delivery
        |
Flutter recibe notificación
        |
Motorizado acepta pedido
        |
App inicia tracking activo
        |
Cada 5-10 segundos envía ubicación
        |
Backend guarda última ubicación en Redis
        |
Backend guarda histórico en PostgreSQL
        |
Backend emite evento DriverLocationUpdated
        |
Panel y cliente reciben actualización por WebSocket
```

## Frecuencia recomendada

| Estado | Frecuencia |
|---|---:|
| Sin pedido activo | No enviar |
| Pedido asignado sin recoger | 15-30 s |
| En camino al cliente | 5-10 s |
| App en segundo plano | 15-30 s según permisos Android |
| Pedido entregado | Detener |

## Datos mínimos de ubicación

```json
{
  "latitude": -12.046374,
  "longitude": -77.042793,
  "accuracyMeters": 8.5,
  "speedMps": 4.2,
  "batteryLevel": 76,
  "recordedAt": "2026-07-09T18:30:00-05:00"
}
```

## Endpoints sugeridos

```text
POST /api/v1/deliveries/{deliveryId}/accept
POST /api/v1/deliveries/{deliveryId}/reject
POST /api/v1/deliveries/{deliveryId}/pickup
POST /api/v1/deliveries/{deliveryId}/complete
POST /api/v1/deliveries/{deliveryId}/locations
GET  /api/v1/deliveries/{deliveryId}/last-location
GET  /api/v1/orders/tracking/{trackingCode}
WS   /ws/tracking/orders/{trackingCode}
WS   /ws/admin/deliveries/{deliveryId}
```

## Redis

Guardar última ubicación:

```text
delivery:last-location:{deliveryId}
```

TTL sugerido:

- Durante entrega activa: refrescar en cada actualización.
- Al entregar: conservar 30 a 60 minutos y luego expirar.

## PostgreSQL

Guardar histórico en `delivery_location_events`:

- `delivery_id`
- `driver_id`
- `latitude`
- `longitude`
- `geo_point`
- `accuracy_meters`
- `speed_mps`
- `battery_level`
- `recorded_at`
- `created_at`

## Kafka

Emitir:

- `DriverLocationUpdated`
- `DeliveryAssigned`
- `DeliveryAccepted`
- `DeliveryCompleted`

## Google Maps / Routes

Fase inicial:

- Abrir destino en Google Maps desde Flutter usando deep link.
- Guardar latitud/longitud y dirección.

Fase posterior:

- Calcular ETA con proveedor externo.
- Recalcular solo si hay cambio significativo para controlar costos.
- Guardar distancia y duración estimada.

## Fases GPS

### Fase 1 — Tracking interno básico

- Flutter envía ubicación.
- Backend valida entrega activa.
- Redis guarda última ubicación.
- PostgreSQL guarda histórico.
- Panel ve ubicación.

### Fase 2 — Tracking para cliente

- Generar `tracking_code`.
- Exponer enlace público.
- Mostrar estados del pedido.
- Mostrar ubicación aproximada si está en camino.

### Fase 3 — ETA y rutas

- Integrar Google Routes API u otro proveedor.
- Calcular distancia y tiempo estimado.
- Evitar llamadas excesivas.

### Fase 4 — Múltiples paradas

- Activar `delivery_stops`.
- Orden manual inicial.
- Optimización automática solo en fase avanzada.

## Seguridad y privacidad

- El cliente no debe ver teléfono, datos personales ni historial completo del motorizado.
- El tracking público debe usar `tracking_code`, no ID interno.
- Aplicar rate limit en endpoint público.
- No guardar ubicación sin delivery activo.
- Detener tracking al completar/cancelar entrega.
