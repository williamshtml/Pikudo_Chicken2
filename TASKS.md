# TASKS.md - Pikudo Chicken Backend

## Estado actual

- Fase 0 - Contexto y decisiones: completada.
- Fase 1 - Infraestructura local: completada.
- Fase 2 - Seguridad base: activa.

`docs/context/07_CODEX_TASKS.md` queda como histórico. Este archivo es el tablero vivo para ordenar las siguientes implementaciones.

## Reglas permanentes

- No convertir este backend en microservicios en esta etapa.
- No migrar a WebFlux todavía; mantener Spring MVC + JPA mientras se estabiliza el dominio.
- No crear ni expandir Angular.
- No editar migraciones Flyway ya aplicadas; cada cambio estructural debe ser una migración nueva.
- No romper endpoints existentes sin mantener compatibilidad o documentar transición.
- No confiar en precios enviados por frontend.
- No guardar GPS fuera de una entrega activa.

## Verificación mínima por cambio

- `.\mvnw.cmd -q test`
- `.\mvnw.cmd -q -DskipTests package`
- `docker compose --env-file .env.docker.example config --quiet`
- Smoke contra PostgreSQL/Docker cuando el cambio toque migraciones, datasource o arranque.

## Fase 2 - Seguridad base

- [x] Refresh tokens hasheados con rotación y expiración configurable.
- [x] Endpoints `POST /api/auth/refresh`, `POST /api/auth/logout` y `GET /api/auth/me`.
- [x] Permisos sembrados y ligados a roles sin romper los `ROLE_*` actuales.
- [x] Auditoría mínima de login exitoso/fallido, refresh y logout.
- [x] Ajuste de `JwtFilter` para no saltarse todo `/api/auth/**`.
- [x] Limpieza documental menor: `HELP.md` y comentarios que todavía mencionan MySQL/Angular legacy.

## Fase 3 - Catálogo real

- [ ] Categorías reales de carta.
- [ ] Productos base, variantes y precios históricos.
- [ ] Modificadores y grupos de modificadores.
- [ ] Combos y componentes.
- [ ] Imágenes de producto en storage local.

## Fase 4 - Pedidos, mesas y caja

- [ ] Historial de estados de pedido.
- [ ] Sesiones de mesa.
- [ ] Snapshots robustos de nombre/precio en detalle de pedido.
- [ ] Pagos y caja alineados al modelo objetivo.

## Fase 5 - Promociones, descuentos y eventos

- [ ] Eventos comerciales.
- [ ] Promociones y targets.
- [ ] Cupones.
- [ ] Registro de descuentos aplicados.

## Fase 6 - Inventario y recetas

- [ ] Unidades de medida y almacenes.
- [ ] Recetas por variante.
- [ ] Movimientos de stock por venta.
- [ ] Alertas de stock bajo.

## Fase 7 - Delivery y GPS

- [ ] Entregas normalizadas.
- [ ] Validación de delivery activo antes de recibir GPS.
- [ ] Última ubicación en Redis.
- [ ] Histórico en PostgreSQL.
- [ ] Tracking por WebSocket.

## Fase 8 - Backups, auditoría y reportes

- [ ] `backup_jobs` y módulo backend de coordinación.
- [ ] Procedimiento de backup/restauración documentado.
- [ ] Auditoría de acciones críticas.
- [ ] Reportes operativos con índices adecuados.
