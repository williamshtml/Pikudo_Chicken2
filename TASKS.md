# TASKS.md - Pikudo Chicken Backend

## Estado actual

- Fase 0 - Contexto y decisiones: completada.
- Fase 1 - Infraestructura local: completada.
- Fase 2 - Seguridad base: completada.
- Fase 3 - Integraciones base y storage: completada.
- Fase 4 - Catalogo real con Drive: completada.
- Fase 5 - Flujo operativo de pedidos, mesas y caja: activa.

`docs/context/07_CODEX_TASKS.md` queda como historico. Este archivo es el tablero vivo para ordenar las siguientes implementaciones.

## Reglas permanentes

- No convertir este backend en microservicios en esta etapa.
- No migrar a WebFlux todavia; mantener Spring MVC + JPA mientras se estabiliza el dominio.
- No crear ni expandir Angular.
- No editar migraciones Flyway ya aplicadas; cada cambio estructural debe ser una migracion nueva.
- No romper endpoints existentes sin mantener compatibilidad o documentar transicion.
- No confiar en precios enviados por frontend.
- No guardar GPS fuera de una entrega activa.
- No guardar secretos reales en Git.
- No acoplar nuevos modulos a storage local directo; usar abstraccion de storage.

## Decisiones post-Fase 2

- El sistema permite multiples administradores: cualquier usuario con rol `ADMINISTRADOR` tiene permisos administrativos.
- El seed de admin solo crea el usuario inicial de arranque si esta habilitado; no impone un admin unico.
- Google Drive sera el storage principal para imagenes y documentos, autenticado con OAuth refresh token.
- Storage local queda como fallback para desarrollo local y contingencia.
- Resend API sera el proveedor principal de email; SMTP queda como legado/no prioritario.
- SUNAT se implementara con Project OpenUBL XBuilder/XSender y certificado `.pfx` configurado por variables de entorno seguras.
- El tracking publico no expondria coordenadas exactas; admin y app Flutter si podran usar ubicacion precisa durante una entrega activa.

## Verificacion minima por cambio

- `.\mvnw.cmd -q test`
- `.\mvnw.cmd -q -DskipTests package`
- `docker compose --env-file .env.docker.example config --quiet`
- Smoke contra PostgreSQL/Docker cuando el cambio toque migraciones, datasource o arranque.
- Smoke de integraciones con provider deshabilitado/mock cuando el cambio toque Drive, Resend, SUNAT o tracking.

## Fase 2 - Seguridad base

- [x] Refresh tokens hasheados con rotacion y expiracion configurable.
- [x] Endpoints `POST /api/auth/refresh`, `POST /api/auth/logout` y `GET /api/auth/me`.
- [x] Permisos sembrados y ligados a roles sin romper los `ROLE_*` actuales.
- [x] Auditoria minima de login exitoso/fallido, refresh y logout.
- [x] Ajuste de `JwtFilter` para no saltarse todo `/api/auth/**`.
- [x] Limpieza documental menor: `HELP.md` y comentarios que todavia mencionan MySQL/Angular legacy.

## Fase 3 - Integraciones base y storage

- [x] Normalizar variables `.env` para Google Drive, Resend y SUNAT sin commitear secretos.
- [x] Crear abstraccion `StorageService` para archivos binarios.
- [x] Implementar provider local como fallback.
- [x] Implementar provider Google Drive con OAuth refresh token.
- [x] Registrar metadata de archivos subidos: provider, folder, file id, url, mime type, tamano, checksum y modulo propietario.
- [x] Migrar `ArchivoService` para usar la abstraccion de storage.
- [x] Preparar carpetas Drive para productos, avatares, evidencias y documentos SUNAT.
- [x] Agregar cliente Resend API deshabilitable por env.
- [x] Preparar configuracion SUNAT para `.pfx`, credenciales SOL, endpoints beta/prod y modo disabled/sandbox/prod.
- [x] Documentar smoke tests de Drive/Resend/SUNAT sin depender de secretos reales.

## Fase 4 - Catalogo real con Drive

- [x] Fase 4A: base de categorias reales con metadata, slug, orden y visibilidad.
- [x] Fase 4A: productos base, variantes iniciales y precios historicos iniciales.
- [x] Fase 4B: modificadores y grupos de modificadores.
- [x] Fase 4C: combos y componentes.
- [x] Fase 4A: imagenes de producto enlazadas a `storage_files` y subida mediante `StorageService`.
- [x] Fase 4A: endpoints nuevos paginados para evitar `findAll` operativo sin limite.

## Fase 5 - Flujo operativo de pedidos, mesas y caja

- [ ] Separar estado operativo de pedido y estado de pago.
- [ ] Crear historial de estados `order_status_history`.
- [ ] Definir transiciones: `UNREAD`, `READ`, `ACCEPTED`, `REJECTED`, `IN_PREPARATION`, `READY`, `ASSIGNED`, `ON_DELIVERY`, `NEAR_CUSTOMER`, `DELIVERED`, `CANCELLED`.
- [ ] Sesiones de mesa.
- [ ] Snapshots robustos de nombre/precio en detalle de pedido.
- [ ] Pagos y caja alineados al modelo objetivo.
- [ ] Indices por estado, fecha, mesa, repartidor y tracking code.

## Fase 6 - Comprobantes SUNAT

- [ ] Modelo de factura, boleta simple, boleta con documento, nota de credito y nota de debito.
- [ ] Project OpenUBL XBuilder para XML UBL firmado.
- [ ] Project OpenUBL XSender para envio SUNAT/OSE.
- [ ] Estado SUNAT con reintentos: `NO_ENVIADO`, `PENDIENTE`, `ACEPTADO`, `ACEPTADO_CON_OBSERVACION`, `RECHAZADO`.
- [ ] Guardar XML, CDR y PDF opcional en Drive.
- [ ] Estructura Drive: `FACTURAS`, `BOLETAS`, `NOTAS_DE_CREDITO`, `NOTAS_DE_DEBITO`, por fecha y cliente.
- [ ] Boleta simple en carpeta `PUBLICO_GENERAL` o `SIN_DOCUMENTO`.

## Fase 7 - Delivery, GPS y tracking real

- [ ] Entregas normalizadas.
- [ ] Validacion de delivery activo antes de recibir GPS.
- [ ] Ultima ubicacion en Redis con TTL.
- [ ] Historico en PostgreSQL con retencion definida.
- [ ] WebSocket separado para admin, cliente y repartidor.
- [ ] Vista publica con estado, avance, ETA/distancia aproximada y cercania, sin coordenadas exactas.
- [ ] Vista admin con mapa exacto y eventos de llegada a puntos de entrega.
- [ ] App Flutter envia GPS solo con entrega activa y muestra mapa/deep link para guiar al repartidor.

## Fase 8 - Promociones, descuentos y eventos

- [ ] Eventos comerciales.
- [ ] Promociones y targets.
- [ ] Cupones.
- [ ] Registro de descuentos aplicados.

## Fase 9 - Inventario y recetas

- [ ] Unidades de medida y almacenes.
- [ ] Recetas por variante.
- [ ] Movimientos de stock por venta.
- [ ] Alertas de stock bajo.

## Fase 10 - Backups, auditoria y reportes

- [ ] `backup_jobs` y modulo backend de coordinacion.
- [ ] Procedimiento de backup/restauracion documentado.
- [ ] Auditoria de acciones criticas.
- [ ] Reportes operativos con indices adecuados.
- [ ] Logs y metricas sin exponer JWT, refresh tokens, PFX, Drive tokens ni claves Resend.
