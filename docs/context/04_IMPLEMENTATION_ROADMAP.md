# 04 - Implementation Roadmap

## Objetivo de esta guia

Dar a Codex una ruta clara para avanzar sin destruir lo ya trabajado. La numeracion canonica operativa queda alineada con `TASKS.md`: Fase 0 contexto, Fase 1 infraestructura, Fase 2 seguridad, Fase 3 integraciones base y storage.

## Fase 0 - Contexto y limpieza controlada

Entregables:

- `AGENTS.md`.
- Documentacion en `docs/context/`.
- Decisiones tecnicas consolidadas.
- Auditoria del estado actual.

Criterio de aceptacion:

- Cualquier agente o desarrollador puede entender que se esta construyendo antes de tocar codigo.

## Fase 1 - Infraestructura local

Entregables:

- Java 21.
- Docker Compose con API, Redis, Kafka y conexion a PostgreSQL externo.
- Red Docker externa `pikudo_postgres_net`.
- Variables de entorno por perfil.
- PostgreSQL + Flyway.
- Health check y documentacion de ejecucion local.

Criterio de aceptacion:

- El proyecto puede levantarse localmente o en Docker usando `pikudo_db` sin depender de MySQL Workbench.

## Fase 2 - Seguridad base

Entregables:

- JWT configurable por `.env`.
- Refresh tokens hasheados con rotacion.
- Roles, permisos y auditoria minima.
- Endpoints `login`, `refresh`, `logout` y `me`.
- Seed inicial de admin configurable.

Criterio de aceptacion:

- Endpoints internos protegidos y configurables por ambiente.
- El sistema permite multiples administradores mediante usuarios con rol `ADMINISTRADOR`.

## Fase 3 - Integraciones base y storage

Entregables:

- Abstraccion `StorageService` para que ningun modulo nuevo dependa directamente de disco local.
- Provider local para desarrollo/fallback.
- Provider Google Drive con OAuth refresh token.
- Metadata de archivos: provider, folder, file id, url, mime type, tamano, checksum y modulo propietario.
- Configuracion por `.env` para Drive, Resend y SUNAT.
- Cliente Resend API deshabilitable por entorno.
- Configuracion SUNAT preparada para `.pfx`, credenciales SOL, endpoints beta/prod y modo disabled/sandbox/prod.

Criterio de aceptacion:

- Imagenes, evidencias y documentos futuros pueden guardarse por una interfaz comun.
- La API arranca con integraciones deshabilitadas si faltan secretos.
- Los secretos reales no se versionan.

## Fase 4 - Catalogo real con Drive

Entregables:

- Categorias reales de Pikudo.
- Productos base.
- Variantes.
- Precios historicos.
- Modificadores.
- Combos.
- Imagenes de producto en Google Drive mediante `StorageService`.
- Endpoints con paginacion/filtros para evitar cargas completas innecesarias.

Criterio de aceptacion:

- La carta de Pikudo se puede cargar sin forzar un modelo plano de producto/precio.
- Las imagenes no quedan acopladas al filesystem local.

## Fase 5 - Flujo operativo de pedidos, mesas y caja

Entregables:

- Separacion entre estado operativo de pedido y estado de pago.
- Historial `order_status_history`.
- Estados operativos: `UNREAD`, `READ`, `ACCEPTED`, `REJECTED`, `IN_PREPARATION`, `READY`, `ASSIGNED`, `ON_DELIVERY`, `NEAR_CUSTOMER`, `DELIVERED`, `CANCELLED`.
- Pedidos por salon, telefono, WhatsApp, recojo, web y delivery.
- Mesas y sesiones de mesa.
- Snapshots de producto, nombre, precio, impuestos y modificadores en detalle de pedido.
- Pagos y cierre de caja basico.

Criterio de aceptacion:

- Un pedido puede recibirse, leerse, aceptarse o rechazarse, prepararse, quedar listo, pagarse y cerrarse.
- Las transiciones invalidas se rechazan y quedan auditadas.

## Fase 6 - Comprobantes SUNAT

Entregables:

- Factura.
- Boleta simple sin documento.
- Boleta con DNI/RUC u otro documento aceptado.
- Nota de credito.
- Nota de debito.
- Generacion XML UBL firmado con Project OpenUBL XBuilder.
- Envio SUNAT/OSE con Project OpenUBL XSender.
- Estados SUNAT y reintentos.
- Archivos XML, CDR y PDF opcional en Google Drive.

Criterio de aceptacion:

- La venta queda registrada aunque SUNAT este temporalmente caido.
- La emision puede reintentarse y los archivos tributarios quedan trazables.

## Fase 7 - Delivery, GPS y tracking real

Entregables:

- Entregas normalizadas.
- Motorizados, vehiculos y zonas.
- Validacion de entrega activa antes de recibir GPS.
- Ultima ubicacion en Redis con TTL.
- Historico en PostgreSQL.
- WebSocket separado para panel admin, tracking publico y app Flutter.
- Cliente web con estado/ETA/distancia aproximada, sin mapa exacto.
- Panel admin con mapa exacto y eventos de llegada.
- Flutter con envio GPS activo y mapa/deep link para guiar al repartidor.

Criterio de aceptacion:

- El repartidor reporta ubicacion solo con entrega activa.
- El cliente ve avance sin exponer coordenadas exactas.
- El panel admin ve ubicacion precisa para operar la entrega.

## Fase 8 - Promociones, descuentos y eventos

Entregables:

- Eventos comerciales.
- Promociones.
- Cupones.
- Descuentos por producto, categoria, metodo de pago o total.
- Registro de descuento aplicado.

Criterio de aceptacion:

- Las promociones aplican solo si estan vigentes y cumplen reglas.

## Fase 9 - Inventario y recetas

Entregables:

- Insumos.
- Unidades.
- Almacenes.
- Recetas por variante.
- Movimientos de stock.
- Descuento por venta.
- Alertas de stock bajo.

Criterio de aceptacion:

- La venta de productos puede generar consumo de insumos.

## Fase 10 - Backups, auditoria, reportes y hardening

Entregables:

- Backup diario.
- Registro en `backup_jobs`.
- Checksum SHA-256.
- Retencion.
- Prueba de restauracion.
- Auditoria de acciones criticas.
- Reportes operativos con indices.
- Ruta futura a BigQuery.

Criterio de aceptacion:

- Existe evidencia de backup y restauracion documentada.
- Los reportes no bloquean la operacion diaria.
