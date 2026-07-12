# Pikudo Chicken Backend - Help

Backend Spring Boot para la operacion self-hosted de Pikudo Chicken.

## Estado tecnico actual

- Java 21.
- Spring Boot 3.x con Spring MVC, JPA, Security, WebSocket y Actuator.
- PostgreSQL como base de datos principal.
- Flyway como unico mecanismo de migracion de schema.
- Docker Compose para API, Redis y Kafka.
- Configuracion por perfiles y archivos `.env.*`.

## Comandos principales

```powershell
.\mvnw.cmd -q test
.\mvnw.cmd -q -DskipTests package
docker compose --env-file .env.docker.example config --quiet
```

Para ejecucion local y Docker, usar [docs/run-local.md](docs/run-local.md).

## Referencias utiles

- [Apache Maven](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/3.5.14/maven-plugin)
- [Spring Web MVC](https://docs.spring.io/spring-boot/3.5.14/reference/web/servlet.html)
- [Spring Data JPA](https://docs.spring.io/spring-boot/3.5.14/reference/data/sql.html#data.sql.jpa-and-spring-data)
- [Spring Security](https://docs.spring.io/spring-boot/3.5.14/reference/web/spring-security.html)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/3.5.14/reference/actuator/index.html)
- [Flyway](https://documentation.red-gate.com/fd)
- [PostgreSQL](https://www.postgresql.org/docs/)

## Reglas de trabajo

- No usar `ddl-auto=update`.
- No editar migraciones Flyway ya aplicadas.
- No crear nuevas pantallas Angular.
- No mover el backend a microservicios en esta etapa.
