# 05 - GPS, Delivery and Tracking Plan

## Objetivo

Permitir que Pikudo Chicken controle entregas, visualice motorizados y ofrezca seguimiento al cliente sin comprometer privacidad ni sobrecargar el servidor local.

## Regla principal

El sistema solo debe recibir ubicacion GPS cuando el motorizado tenga una entrega activa.

No guardar tracking permanente del trabajador fuera de una entrega.

## Flujo operativo del restaurante

```text
Pedido recibido
        |
Estado UNREAD: cocina/caja/panel aun no lo leyo
        |
Estado READ: personal lo abrio/reviso
        |
Aceptado o rechazado
        |
Si acepta: IN_PREPARATION
        |
READY: preparacion terminada
        |
Si es delivery: ASSIGNED a motorizado
        |
Motorizado acepta entrega
        |
ON_DELIVERY: app Flutter inicia tracking
        |
NEAR_CUSTOMER: calculado por distancia o marcado por app/admin
        |
DELIVERED: se detiene tracking
```

El estado de pago y el estado SUNAT no deben mezclarse con el estado operativo del pedido.

## Vistas por actor

### Cliente web/publico

Debe ver:

- Estado del pedido.
- Avance del proceso.
- ETA o rango aproximado cuando exista.
- Distancia/cercania aproximada.
- Mensajes como "tu pedido esta en preparacion", "el repartidor esta en camino" o "esta cerca".

No debe ver:

- Coordenadas exactas.
- Mapa exacto del repartidor.
- Datos personales internos del motorizado.
- Historial completo de ubicaciones.

### Panel administrativo

Debe ver:

- Mapa exacto de motorizados con entrega activa.
- Pedido asignado.
- Ultima ubicacion.
- Eventos de llegada a punto de entrega.
- Estado de conectividad de la app Flutter.
- Alertas si el repartidor deja de reportar.

### App Flutter del repartidor

Debe:

- Compartir ubicacion solo con entrega activa.
- Detener tracking al entregar/cancelar.
- Mostrar mapa o deep link al destino.
- Permitir aceptar/rechazar entrega, marcar recojo, marcar cerca y completar.
- Reintentar envio cuando vuelva internet sin mandar ubicaciones de entregas ya cerradas.

## Flujo tecnico base

```text
Pedido delivery aceptado
        |
Backend crea delivery
        |
Admin/caja asigna motorizado
        |
Flutter recibe asignacion
        |
Motorizado acepta
        |
Backend valida entrega activa
        |
Flutter envia GPS cada intervalo permitido
        |
Backend guarda ultima ubicacion en Redis
        |
Backend persiste evento en PostgreSQL
        |
Backend emite eventos WebSocket/Kafka
        |
Admin ve mapa exacto, cliente ve avance aproximado
```

## Frecuencia recomendada

| Estado | Frecuencia |
|---|---:|
| Sin entrega activa | No enviar |
| Asignado sin aceptar | No enviar GPS |
| Aceptado sin recoger | 15-30 s |
| En camino al cliente | 5-10 s |
| App en segundo plano | 15-30 s segun permisos Android |
| Pedido entregado/cancelado | Detener |

## Datos minimos de ubicacion

```json
{
  "deliveryId": "uuid",
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
POST /api/v1/deliveries/{deliveryId}/near-customer
POST /api/v1/deliveries/{deliveryId}/complete
POST /api/v1/deliveries/{deliveryId}/locations
GET  /api/v1/deliveries/{deliveryId}/last-location
GET  /api/v1/orders/tracking/{trackingCode}
WS   /ws/tracking/orders/{trackingCode}
WS   /ws/admin/deliveries/{deliveryId}
WS   /ws/driver/deliveries/{deliveryId}
```

## Redis

Guardar ultima ubicacion:

```text
delivery:last-location:{deliveryId}
driver:active-delivery:{driverId}
order:tracking:{trackingCode}
```

TTL sugerido:

- Durante entrega activa: refrescar en cada actualizacion.
- Al entregar: conservar 30 a 60 minutos y luego expirar.
- Si la app se desconecta: conservar estado corto para mostrar "ultima ubicacion conocida".

## PostgreSQL

Guardar historico en `delivery_location_events`:

- `id`
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

Guardar cambios de estado en historial separado:

- `order_status_history`
- `delivery_status_history` si se separa entrega de pedido.

## Kafka

Emitir hechos de dominio, no reemplazar la transaccion:

- `DeliveryAssigned`
- `DeliveryAccepted`
- `DeliveryRejected`
- `DeliveryPickedUp`
- `DriverLocationUpdated`
- `DeliveryNearCustomer`
- `DeliveryCompleted`
- `OrderStatusChanged`

## Google Maps / Routes

Fase inicial:

- Abrir destino en Google Maps desde Flutter usando deep link.
- Guardar latitud/longitud y direccion.
- Calcular cercania por distancia simple cuando sea suficiente.

Fase posterior:

- Calcular ETA con proveedor externo.
- Recalcular solo si hay cambio significativo para controlar costos.
- Guardar distancia y duracion estimada.

## Fases GPS

### Fase GPS 1 - Tracking interno basico

- Flutter envia ubicacion.
- Backend valida entrega activa.
- Redis guarda ultima ubicacion.
- PostgreSQL guarda historico.
- Panel ve ubicacion exacta.

### Fase GPS 2 - Tracking para cliente

- Generar `tracking_code`.
- Exponer enlace publico.
- Mostrar estados del pedido.
- Mostrar ubicacion aproximada, ETA o cercania sin coordenadas exactas.

### Fase GPS 3 - ETA y rutas

- Integrar Google Routes API u otro proveedor.
- Calcular distancia y tiempo estimado.
- Evitar llamadas excesivas.

### Fase GPS 4 - Multiples paradas

- Activar `delivery_stops`.
- Orden manual inicial.
- Optimizacion automatica solo en fase avanzada.

## Seguridad y privacidad

- El tracking publico debe usar `tracking_code`, no ID interno.
- Aplicar rate limit en endpoint publico.
- No guardar ubicacion sin delivery activo.
- Detener tracking al completar/cancelar entrega.
- No exponer coordenadas exactas al cliente publico.
- Auditar cambios manuales de estado hechos por admin/caja.
