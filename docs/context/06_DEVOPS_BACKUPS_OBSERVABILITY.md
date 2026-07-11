# 06 — DevOps, Backups and Observability

## Objetivo

Preparar el backend para ejecución self-hosted con seguridad mínima, recuperación ante fallos y diagnóstico operativo.

## Docker Compose objetivo

Servicios sugeridos:

```text
pikudo-api
postgres
redis
kafka
nginx
backup-runner
```

## Estructura de despliegue

```text
pikudo-deploy/
  docker-compose.yml
  .env
  nginx/
    nginx.conf
  backups/
  storage/
    product-images/
    delivery-evidence/
  logs/
  secrets/
```

## Variables de entorno mínimas

```text
POSTGRES_DB=pikudo_db
POSTGRES_USER=pikudo_app
POSTGRES_PASSWORD=********
SPRING_PROFILES_ACTIVE=local
JWT_SECRET=********
REDIS_HOST=redis
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
BACKUP_DIR=/backups
GOOGLE_MAPS_API_KEY=********
FIREBASE_CREDENTIALS_PATH=/secrets/firebase.json
```

## Backups

La base PostgreSQL debe tener backup diario.

Política recomendada:

| Elemento | Valor |
|---|---|
| Frecuencia | Diario |
| Hora | 02:00 a.m. |
| Formato | `.dump` comprimido |
| Herramienta | `pg_dump` |
| Checksum | SHA-256 |
| Retención diaria | 30 días |
| Retención mensual | 12 meses |
| Prueba de restauración | Mensual |

## Comando base

```bash
pg_dump \
  --host=localhost \
  --port=5432 \
  --username=pikudo_app \
  --format=custom \
  --file=/backups/pikudo_$(date +%Y%m%d_%H%M%S).dump \
  pikudo_db
```

## Flujo de backup

```text
Scheduler inicia proceso
        |
Inserta registro backup_jobs STARTED
        |
Ejecuta pg_dump
        |
Comprime archivo
        |
Calcula SHA-256
        |
Marca backup_jobs SUCCESS
        |
Elimina backups vencidos
        |
Emite evento BackupCompleted
```

## Regla crítica

Un backup no probado no es confiable. Debe existir procedimiento de restauración documentado.

## Auditoría mínima

Registrar:

- Login exitoso/fallido.
- Creación/cancelación de pedidos.
- Cambios de estado.
- Pagos registrados.
- Asignación de delivery.
- Fallos de notificación.
- Fallos de backup.
- Cambios de precio.
- Descuentos aplicados.

## Observabilidad

Agregar progresivamente:

- Spring Boot Actuator.
- Health checks.
- Logs rotativos.
- Métricas de endpoints.
- Métricas de eventos Kafka pendientes.
- Métricas de Redis.
- Métricas de backup.

## Logs mínimos

- `INFO` para operaciones normales.
- `WARN` para estados sospechosos.
- `ERROR` para fallos recuperables/no recuperables.

Nunca loguear:

- Contraseñas.
- JWT completo.
- Refresh tokens.
- Datos de tarjeta.
- Secrets.
