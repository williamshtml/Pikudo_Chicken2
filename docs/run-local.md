# Ejecucion local - Pikudo Chicken API

## Requisitos

- Java 21 para ejecucion local.
- Docker Desktop para ejecutar la API en contenedor.
- PostgreSQL externo disponible como `global_postgres_db` cuando se use Docker.
- Red Docker externa `pikudo_postgres_net` creada previamente y compartida con `global_postgres_db`.

Crear la red si todavia no existe:

```powershell
docker network create pikudo_postgres_net
```

## Archivos de entorno

Los archivos reales `.env.local`, `.env.docker`, `.env.dev` y `.env.prod` no se versionan porque contienen secretos. Crear cada uno desde su plantilla:

```powershell
Copy-Item .env.local.example .env.local
Copy-Item .env.docker.example .env.docker
Copy-Item .env.dev.example .env.dev
Copy-Item .env.prod.example .env.prod
```

Editar los valores reales de:

- `DB_PASSWORD`
- `JWT_SECRET`
- `ADMIN_*` si se habilita seed local
- `DRIVE_OAUTH_*` cuando se active Google Drive
- `RESEND_API_KEY` y `RESEND_FROM_EMAIL` cuando se active Resend
- `SUNAT_*` cuando se active emision electronica

No commitear archivos `.env.*` reales ni certificados `.pfx`.

## Integraciones

Por defecto, Drive, Resend y SUNAT quedan deshabilitados en las plantillas:

```text
GOOGLE_DRIVE_ENABLED=false
RESEND_ENABLED=false
SUNAT_ENABLED=false
SUNAT_MODE=disabled
```

Los providers de Fase 3 ya existen. Si una integracion queda deshabilitada, la API debe arrancar sin secretos reales.

### Google Drive

Se usara OAuth refresh token:

```text
DRIVE_OAUTH_CLIENT_ID=
DRIVE_OAUTH_CLIENT_SECRET=
DRIVE_OAUTH_REFRESH_TOKEN=
DRIVE_FOLDERS_PRODUCTS=
DRIVE_FOLDERS_AVATAR_USERS=
DRIVE_FOLDERS_DELIVERY_EVIDENCE=
DRIVE_FOLDERS_SUNAT_ROOT=
```

`APP_STORAGE_PROVIDER=local` usa disco local como fallback. `APP_STORAGE_PROVIDER=google-drive` usa Google Drive cuando `GOOGLE_DRIVE_ENABLED=true`.

Estructura operativa esperada en Drive:

```text
PRODUCTOS
AVATARES_USUARIOS
EVIDENCIAS_DELIVERY
SUNAT/
  FACTURAS
  BOLETAS
  NOTAS_DE_CREDITO
  NOTAS_DE_DEBITO
```

La creacion real de carpetas es operativa/manual. El backend valida los folder IDs cuando `GOOGLE_DRIVE_ENABLED=true`.

### Resend

Se usara Resend API:

```text
RESEND_ENABLED=false
RESEND_API_KEY=
RESEND_FROM_EMAIL=Pikudo Chicken <no-reply@example.com>
```

Las variables `SMTP_*` quedan como compatibilidad secundaria/no prioritaria.

### SUNAT

Para la integracion futura con Project OpenUBL XBuilder/XSender:

```text
SUNAT_ENABLED=false
SUNAT_MODE=disabled
SUNAT_RUC=
SUNAT_SOL_USERNAME=
SUNAT_SOL_PASSWORD=
SUNAT_PFX_BASE64=
SUNAT_PFX_PASSWORD=
SUNAT_ENDPOINT_BETA=
SUNAT_ENDPOINT_PROD=
```

Usar `SUNAT_PFX_BASE64` o un secreto montado fuera del repo. No guardar el `.pfx` en Git.

## Perfil local

Usa PostgreSQL en `localhost`.

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd spring-boot:run
```

Health check:

```powershell
Invoke-WebRequest http://localhost:8080/actuator/health
```

Swagger queda habilitado en:

```text
http://localhost:8080/swagger-ui.html
```

## Perfil docker

Usa `global_postgres_db` a traves de `pikudo_postgres_net`.

```powershell
docker compose --env-file .env.docker up --build
```

La API queda publicada en:

```text
http://localhost:8080
```

## Perfil dev

Usa el mismo host de DB que Docker, con logs mas verbosos.

```powershell
docker compose --env-file .env.dev up --build
```

## Perfil prod

Usa configuracion de produccion: logs reducidos, OpenAPI deshabilitado y pool Hikari ajustable desde `.env.prod`.

```powershell
docker compose --env-file .env.prod up --build -d
```

## Validaciones basicas

```powershell
.\mvnw.cmd -q test
.\mvnw.cmd -q -DskipTests package
docker compose --env-file .env.docker.example config --quiet
```

Flyway ejecuta automaticamente las migraciones al arrancar la aplicacion. Hibernate queda en `validate`, por lo que no crea ni modifica tablas.

## Smoke manual de integraciones

### Storage local

Usar:

```text
APP_STORAGE_PROVIDER=local
GOOGLE_DRIVE_ENABLED=false
```

Arrancar la API y subir una imagen con `POST /api/imagenes/subir/productos`. La respuesta debe ser `/api/files/{uuid}/content` y el archivo debe quedar registrado en `storage_files`.

### Google Drive

Usar:

```text
APP_STORAGE_PROVIDER=google-drive
GOOGLE_DRIVE_ENABLED=true
DRIVE_OAUTH_CLIENT_ID=...
DRIVE_OAUTH_CLIENT_SECRET=...
DRIVE_OAUTH_REFRESH_TOKEN=...
DRIVE_FOLDERS_PRODUCTS=...
DRIVE_FOLDERS_AVATAR_USERS=...
DRIVE_FOLDERS_DELIVERY_EVIDENCE=...
DRIVE_FOLDERS_SUNAT_ROOT=...
```

Arrancar la API y subir una imagen de producto. La metadata debe quedar en `storage_files` con provider `google-drive`.

### Resend

Usar:

```text
RESEND_ENABLED=true
RESEND_API_KEY=...
RESEND_FROM_EMAIL=Pikudo Chicken <no-reply@dominio-validado>
SMTP_HOST=
SMTP_USER=
SMTP_PASSWORD=
```

El envio real se valida manualmente desde un flujo que use `EmailService`. No usar SMTP en esta fase.

### SUNAT

Para esta fase mantener:

```text
SUNAT_ENABLED=false
SUNAT_MODE=disabled
```

Si se prueba sandbox mas adelante, completar `SUNAT_*`; la API solo valida configuracion. No emite comprobantes todavia.
