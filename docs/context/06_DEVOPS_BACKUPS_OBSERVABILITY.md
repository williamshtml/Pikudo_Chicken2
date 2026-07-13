# 06 - DevOps, Backups and Observability

## Objetivo

Preparar el backend para ejecucion self-hosted con seguridad minima, recuperacion ante fallos, integraciones externas controladas y diagnostico operativo.

## Docker Compose objetivo

Servicios sugeridos:

```text
pikudo-api
redis
kafka
nginx
backup-runner
```

PostgreSQL no se crea desde este repo. Se asume externo como `global_postgres_db` en la red Docker `pikudo_postgres_net`.

## Estructura de despliegue

```text
pikudo-deploy/
  docker-compose.yml
  .env
  nginx/
    nginx.conf
  backups/
  storage/
    local-fallback/
      product-images/
      delivery-evidence/
      sunat-documents/
  logs/
  secrets/
```

Google Drive sera el storage principal para archivos operativos. El filesystem local queda como fallback y para desarrollo.

## Variables de entorno minimas

```text
SPRING_PROFILES_ACTIVE=prod
DB_HOST=global_postgres_db
DB_PORT=5432
DB_NAME=pikudo_db
DB_USERNAME=pikudo_app
DB_PASSWORD=********
JWT_SECRET=********
REDIS_HOST=redis
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

APP_STORAGE_PROVIDER=google-drive
APP_STORAGE_LOCAL_BASE_PATH=uploads
GOOGLE_DRIVE_ENABLED=true
DRIVE_OAUTH_CLIENT_ID=********
DRIVE_OAUTH_CLIENT_SECRET=********
DRIVE_OAUTH_REFRESH_TOKEN=********
DRIVE_FOLDERS_PRODUCTS=********
DRIVE_FOLDERS_AVATAR_USERS=********
DRIVE_FOLDERS_DELIVERY_EVIDENCE=********
DRIVE_FOLDERS_SUNAT_ROOT=********

RESEND_ENABLED=true
RESEND_API_KEY=********
RESEND_FROM_EMAIL=Pikudo Chicken <no-reply@dominio-validado.com>

SUNAT_ENABLED=false
SUNAT_MODE=disabled
SUNAT_RUC=***********
SUNAT_SOL_USERNAME=********
SUNAT_SOL_PASSWORD=********
SUNAT_PFX_BASE64=********
SUNAT_PFX_PASSWORD=********
```

No versionar valores reales. Los `.env.*.example` solo deben contener placeholders seguros.

## Google Drive

Estructura recomendada:

```text
PIKUDO_CHICKEN/
  PRODUCTOS/
  AVATARES_USUARIOS/
  DELIVERY_EVIDENCE/
  SUNAT/
    FACTURAS/
      yyyy/MM/dd/{cliente-o-ruc}/
    BOLETAS/
      yyyy/MM/dd/{cliente-o-publico-general}/
    NOTAS_DE_CREDITO/
      yyyy/MM/dd/{cliente-o-ruc}/
    NOTAS_DE_DEBITO/
      yyyy/MM/dd/{cliente-o-ruc}/
```

Para boleta simple sin documento usar carpeta `PUBLICO_GENERAL` o `SIN_DOCUMENTO`.

## SUNAT

El certificado `.pfx` debe tratarse como secreto. Reglas:

- No subirlo al repo.
- Preferir `SUNAT_PFX_BASE64` para Docker/env controlado o un path montado como secreto si el despliegue lo permite.
- No loguear password, contenido del pfx, XML firmado completo ni CDR completo.
- Guardar XML/CDR/PDF en Drive y metadata en PostgreSQL.
- Mantener modo `disabled` para desarrollo sin credenciales.

## Backups

La base PostgreSQL debe tener backup diario.

Politica recomendada:

| Elemento | Valor |
|---|---|
| Frecuencia | Diario |
| Hora | 02:00 a.m. |
| Formato | `.dump` comprimido |
| Herramienta | `pg_dump` |
| Checksum | SHA-256 |
| Retencion diaria | 30 dias |
| Retencion mensual | 12 meses |
| Prueba de restauracion | Mensual |

## Comando base

```bash
pg_dump \
  --host=global_postgres_db \
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
Opcionalmente sube copia a Drive
        |
Marca backup_jobs SUCCESS
        |
Elimina backups vencidos
        |
Emite evento BackupCompleted
```

## Regla critica

Un backup no probado no es confiable. Debe existir procedimiento de restauracion documentado.

## Auditoria minima

Registrar:

- Login exitoso/fallido.
- Creacion/cancelacion de pedidos.
- Cambios de estado operativo.
- Pagos registrados.
- Emision/reintento/anulacion de comprobantes.
- Cambios de estado SUNAT.
- Asignacion de delivery.
- Fallos de notificacion.
- Fallos de Drive/Resend/SUNAT.
- Fallos de backup.
- Cambios de precio.
- Descuentos aplicados.

## Observabilidad

Agregar progresivamente:

- Spring Boot Actuator.
- Health checks.
- Logs rotativos.
- Metricas de endpoints.
- Metricas de eventos Kafka pendientes.
- Metricas de Redis.
- Metricas de backup.
- Metricas de upload Drive.
- Metricas de envio Resend.
- Metricas de SUNAT por estado.

## Logs minimos

- `INFO` para operaciones normales.
- `WARN` para estados sospechosos.
- `ERROR` para fallos recuperables/no recuperables.

Nunca loguear:

- Contrasenas.
- JWT completo.
- Refresh tokens.
- Datos de tarjeta.
- PFX o password de PFX.
- OAuth refresh token de Drive.
- API key de Resend.
- Credenciales SOL.
- Secrets.
