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

Editar los valores reales de `DB_PASSWORD`, `JWT_SECRET` y, si aplica, el seed del administrador.

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
docker compose --env-file .env.docker config
```

Flyway ejecuta automaticamente las migraciones al arrancar la aplicacion. Hibernate queda en `validate`, por lo que no crea ni modifica tablas.
