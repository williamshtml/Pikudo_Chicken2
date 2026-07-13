# 01 — Current State Audit

## Resumen del estado actual

El proyecto ya tiene una base funcional avanzada, pero no coincide completamente con el plan técnico objetivo.

## Backend actual

El backend actual está construido con Spring Boot y Maven. El `pom.xml` contiene:

- `spring-boot-starter-web`.
- `spring-boot-starter-data-jpa`.
- `spring-boot-starter-security`.
- `spring-boot-starter-validation`.
- `spring-boot-starter-websocket`.
- MySQL connector.
- Lombok.
- JJWT.
- Springdoc OpenAPI para WebMVC.
- MapStruct.

Esto indica que actualmente el backend es principalmente Spring MVC + JPA + MySQL, no WebFlux + PostgreSQL.

## Configuración actual

`application.properties` apunta a MySQL local:

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/pikudo_chicken_db
spring.datasource.username=root
spring.datasource.password=mysql
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

Problemas a corregir:

1. Migrar de MySQL a PostgreSQL.
2. Eliminar `ddl-auto=update` como mecanismo final.
3. Introducir Flyway.
4. Externalizar credenciales mediante variables de entorno.
5. Crear perfiles `local`, `dev`, `prod`.

## Paquetes actuales detectados

La raiz Java oficial del backend es `com.pikudo.restaurant`. La clase principal vive en `src/main/java/com/pikudo/restaurant/PikudoChicken2Application.java` y usa el component scan normal de Spring Boot desde esa raiz.

Paquetes detectados por búsqueda:

- `controller`
- `controller.delivery`
- `controller.payment`
- `controller.integration`
- `controller.notificacion`
- `dto`
- `dto.delivery`
- `dto.payment`
- `dto.tracking`
- `dto.sunat`
- `entity`
- `entity.delivery`
- `entity.payment`
- `exception`
- `mapper`
- `repository`
- `repository.delivery`
- `service`
- `service.impl`
- `service.delivery`
- `service.payment`
- `service.integration`
- `service.notificacion`
- `util`

## Módulos ya iniciados

Se detectan avances relacionados con:

- Autenticación.
- Usuarios.
- Roles.
- Categorías.
- Productos.
- Pedidos.
- Caja.
- Mesas.
- Inventario.
- Reportes.
- Delivery.
- Rutas.
- Zonas.
- Tracking.
- Pagos.
- Culqi.
- Google Maps.
- SUNAT.
- Notificaciones.
- Archivos.
- Impresión.

## Observación sobre entidades actuales

Algunas entidades usan `Long` autoincremental. El plan objetivo recomienda UUID para entidades principales, pero la migración debe hacerse con cuidado. No forzar cambio global si afecta demasiado el avance. Para nuevas tablas críticas puede evaluarse UUID desde el inicio.

## Pedido actual

La entidad `Pedido` ya maneja:

- Mesa.
- Mesero.
- Cajero.
- Repartidor.
- Total.
- Estado.
- Tipo comprobante.
- Tipo pedido.
- Dirección.
- URL Maps.
- Teléfono cliente.
- Observaciones.
- Detalles.
- `@Version` para concurrencia optimista.

Esto es una buena base para evolucionar hacia estados, tracking y pagos, pero faltan piezas normalizadas como `order_status_history`, `deliveries`, `delivery_location_events`, `payments` y snapshots robustos.

## Producto actual

La entidad `Producto` contiene:

- Nombre.
- Precio.
- Stock.
- Estado.
- Categoría.

Para el modelo objetivo esto es insuficiente. Debe evolucionar a:

- Producto base.
- Variantes.
- Precios históricos.
- Imágenes.
- Modificadores.
- Combos.
- Recetas.
- Disponibilidad.

## Seguridad actual

`SecurityConfig` usa JWT filter, stateless session, `@EnableMethodSecurity`, BCrypt y CORS. Actualmente permite Swagger y `/api/auth/**` sin autenticación.

Problema actual: CORS apunta a `http://localhost:4200`, asociado a Angular. Esto debe moverse a variables de entorno y contemplar Tauri, Next.js y ambientes locales.

## Frontend actual

Existe carpeta `fronted/` con Angular. El `package.json` contiene Angular 22 y scripts `ng serve`, `ng build`, `ng test`.

Decisión: este frontend no debe crecer. Se puede conservar como referencia temporal, pero el stack objetivo es:

- Desktop administrativo: React + TypeScript + Vite + Tauri.
- Landing pública: Next.js + React + TypeScript.
- Mobile delivery: Flutter.

## Riesgos técnicos actuales

1. Base de datos con `ddl-auto=update`.
2. MySQL en lugar de PostgreSQL.
3. Credenciales hardcodeadas.
4. Angular en el repositorio pese al stack objetivo.
5. Arquitectura todavía por capas clásicas, no modularizada por dominio.
6. Potencial mezcla de lógica en controllers/services sin casos de uso claros.
7. Falta de Flyway.
8. Falta de Docker Compose completo.
9. Falta de backup automatizado auditable.
10. Falta de contrato OpenAPI versionado por módulos.

## Recomendación de migración

No borrar todo. Convertir el proyecto gradualmente:

1. Congelar cambios funcionales grandes.
2. Crear documentación de contexto.
3. Configurar Docker Compose.
4. Migrar configuración a PostgreSQL.
5. Agregar Flyway.
6. Crear migraciones iniciales desde el modelo existente.
7. Reorganizar por módulos sin romper endpoints.
8. Recién después mejorar modelo de productos, pedidos, delivery y pagos.
