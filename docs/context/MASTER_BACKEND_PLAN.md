
---
title: "Plan tecnico especializado de backend y arquitectura - Pikudo Chicken"
author: "Studios TKOH"
date: "09 de julio de 2026"
lang: es-PE
fontsize: 12pt
geometry: margin=2.54cm
mainfont: Arimo
linestretch: 1.25
toc: true
numbersections: true
---

\newpage

# Resumen ejecutivo

Este documento define el plan tecnico especializado para construir el backend, la base de datos y la arquitectura operativa del software a medida de **Pikudo Chicken**. A diferencia del plan corporativo reservado para el futuro SaaS de Studios TKOH, esta version esta pensada para un restaurante concreto, con una API propia, una base de datos PostgreSQL propia, un panel administrativo de escritorio, una landing page publica y una futura aplicacion movil para delivery.

La solucion se disenara como un sistema **on-premise / self-hosted**, ejecutable en un servidor o PC del restaurante. No sera multi-tenant ni SaaS en su primera version. Sin embargo, el diseno debe soportar crecimiento vertical de datos, historico operativo, backups automaticos, procesos asincronos, tracking GPS, reportes, promociones, descuentos, eventos, inventario y futura integracion con BigQuery.

La arquitectura recomendada para este caso es una **arquitectura multicapas con modular monolith**, no microservicios en la fase inicial. El sistema debe estar claramente dividido por modulos internos, pero desplegado como una API principal. Esta decision reduce complejidad operativa, evita costos innecesarios de infraestructura, facilita el mantenimiento para un local que correra su propio sistema y deja el camino abierto para extraer modulos independientes cuando el negocio lo justifique.

## Actualizacion operativa post-Fase 2

Fase 2 de seguridad base queda cerrada. El roadmap operativo pasa a Fase 3: integraciones base y storage. Antes de avanzar con catalogo real, pedidos avanzados o SUNAT completo, el backend debe preparar una abstraccion comun de archivos, Google Drive como storage principal, Resend como proveedor principal de email y configuracion SUNAT segura basada en `.env`.

Decisiones nuevas:

- El sistema soporta multiples administradores. El rol `ADMINISTRADOR` no es singleton; el seed inicial solo crea un usuario bootstrap si esta habilitado.
- Google Drive sera el storage principal para imagenes, evidencias y documentos tributarios. Storage local queda como fallback.
- Resend API sera el proveedor principal de email. SMTP queda como compatibilidad secundaria/no prioritaria.
- SUNAT se implementara con Project OpenUBL XBuilder/XSender, certificado `.pfx` y credenciales SOL por variables de entorno seguras.
- El cliente publico de tracking no vera coordenadas exactas ni mapa exacto. El panel administrativo y la app Flutter del repartidor si usaran ubicacion precisa durante entregas activas.
- El flujo operativo de pedidos debe diferenciar estado de preparacion/entrega, estado de pago y estado SUNAT.

# Contexto del proyecto

## Cambio de enfoque

El plan SaaS general se conservara como propiedad estrategica de Studios TKOH para una aplicacion futura mas grande. Para Pikudo Chicken se desarrollara una solucion a medida, enfocada en resolver las operaciones reales del restaurante y en entregar valor directo al local.

El sistema para Pikudo Chicken tendra los siguientes productos principales:

1. **Backend API principal** para pedidos, productos, caja, usuarios, delivery, promociones, inventario, notificaciones, pagos, reportes y backups.
2. **Panel administrativo desktop** construido con React, TypeScript, Vite y Tauri.
3. **Landing page publica** construida con Next.js, React y TypeScript, orientada a carta, marca, contacto y captacion de clientes.
4. **Modulo movil de delivery** construido con Flutter en una fase posterior.
5. **Base de datos PostgreSQL** normalizada y preparada para crecimiento vertical.
6. **Servicios de infraestructura local**: Redis, Kafka, Google Drive como storage externo, fallback local de archivos, backups diarios y observabilidad basica.

## Informacion operacional considerada

La carta de Pikudo Chicken muestra una oferta amplia de productos, incluyendo pollo a la lena, parrillas familiares, combos parrilleros, cortes BBQ, piqueos, adicionales, saltados, chaufas, carnes, pastas, sopas, ensaladas y bebidas calientes, frias y frozen. Tambien se observan medios de pago como Yape, Plin, Visa y Mastercard. Esta variedad confirma que el modelo de datos no debe limitarse a productos simples; debe soportar categorias, variantes, combos, precios, modificadores, promociones, eventos y descuentos.

## Dimensionamiento inicial

Para el diseno se asume una capacidad minima de referencia:

- Hasta **200 clientes por dia**.
- Entre **150 y 350 pedidos diarios** dependiendo de si se cuentan pedidos por mesa, delivery, recojo y pedidos telefonicos.
- Entre **800 y 2,500 items de pedido diarios** en dias de alta rotacion.
- Entre **3 y 10 usuarios internos conectados**: administrador, cajero, mozos y operadores.
- Entre **1 y 8 motorizados activos** en fase de delivery.
- Retencion operativa minima de **5 anos de datos transaccionales**.
- Backups diarios automaticos con retencion definida.

Estos numeros no son altos para PostgreSQL, pero si exigen buen modelado, indices correctos, separacion entre operacion y analitica, y politicas de archivado.

# Objetivos del sistema

## Objetivo general

Construir una plataforma backend robusta, mantenible y escalable verticalmente para la gestion integral de Pikudo Chicken, permitiendo controlar productos, carta, pedidos, mesas, caja, delivery, tracking GPS, promociones, descuentos, inventario, usuarios, reportes y respaldos automaticos.

## Objetivos especificos

1. Centralizar la informacion del restaurante en una base de datos PostgreSQL normalizada.
2. Permitir que el restaurante opere sin depender de un SaaS externo.
3. Construir una API principal segura con autenticacion JWT y control de roles.
4. Implementar WebFlux para manejar I/O concurrente, WebSocket, eventos y tracking en tiempo real.
5. Mantener Kafka como bus de eventos para desacoplar notificaciones, tracking, reportes, auditoria y futuras integraciones.
6. Mantener Redis para cache, sesiones operativas, estados temporales y tracking en caliente.
7. Usar Flyway para versionar la base de datos desde el primer dia.
8. Crear un modelo de datos preparado para alimentos, bebidas, combos, promociones, descuentos y eventos.
9. Incorporar backups automaticos diarios con restauracion documentada.
10. Preparar una ruta futura hacia BigQuery para analitica historica sin sobrecargar el servidor local.

# Alcance funcional

## Incluido en la primera version fuerte

- Gestion de usuarios, roles y permisos.
- Gestion de productos, categorias, variantes, precios, combos y disponibilidad.
- Gestion de promociones, descuentos y eventos comerciales.
- Gestion de mesas y sesiones de atencion.
- Registro de pedidos de salon, telefono, WhatsApp, recojo y delivery.
- Caja, pagos, cierre de venta y control basico de comprobantes.
- Gestion de delivery y asignacion de motorizado.
- Tracking operativo del delivery.
- Notificaciones internas y push movil en fase correspondiente.
- Inventario base, insumos, stock y movimientos.
- Reportes operativos.
- Auditoria de operaciones criticas.
- Backups diarios de PostgreSQL.
- Facturacion electronica SUNAT por fase, usando Project OpenUBL, certificado `.pfx` y almacenamiento de XML/CDR en Drive.

## Fuera de alcance inicial

- Multi-tenant SaaS para varios restaurantes.
- BI avanzado en BigQuery desde el primer sprint.
- Microservicios independientes en produccion inicial.
- Optimizacion avanzada de rutas multi-delivery en tiempo real desde el primer release.
- Integracion directa con pasarelas multiples si no existe contrato o credenciales del cliente.

# Decision arquitectonica principal

## No se recomienda iniciar con microservicios

Para este caso especifico no conviene iniciar con microservicios. Pikudo Chicken correra su propia API en un servidor o PC concreto, por lo que dividir el backend en multiples servicios desde el inicio aumentaria innecesariamente la complejidad de despliegue, monitoreo, seguridad, comunicacion interna, versionado, logs, trazabilidad y recuperacion ante fallos.

La mejor decision es construir un **modular monolith multicapas**. Esto significa que el backend sera una sola aplicacion desplegable, pero internamente estara separada en modulos de dominio: identidad, catalogo, precios, pedidos, caja, delivery, tracking, inventario, reportes, backups, notificaciones e integraciones. Si en el futuro se requiere escalar un modulo particular, por ejemplo tracking o analitica, ese modulo podra extraerse como microservicio sin reescribir todo el sistema.

## Cuando si se justificaria migrar a microservicios

La migracion parcial a microservicios solo deberia considerarse si ocurre una o mas de estas condiciones:

- El sistema empieza a atender varios locales fisicos de Pikudo o de otros restaurantes.
- El tracking GPS genera demasiados eventos para la API transaccional.
- El modulo de reportes o analitica afecta el rendimiento de caja y pedidos.
- Se requiere separar despliegues por equipo o por frecuencia de cambios.
- Se necesita alta disponibilidad real con replicas y balanceo.
- Kafka se vuelve el centro de integracion con BigQuery, sistemas contables, delivery externo o notificaciones masivas.

## Arquitectura recomendada

```text
[Desktop Admin - React/Vite/Tauri]
               |
[Landing Web - Next.js] ----> [Nginx / Reverse Proxy]
               |                         |
[Flutter Delivery App] ----------------> [Backend API - Spring Boot MVC/WebSocket]
                                         |
                   -----------------------------------------------------------
                   |                 |              |                       |
              [PostgreSQL]        [Redis]        [Kafka]          [StorageService]
                   |                                |                       |
              [Backups diarios]              [Eventos asincronos]    [Google Drive]
                   |                                                [Fallback local]
          [Export futuro a BigQuery]
```

# Arquitectura multicapas

El backend debe respetar una arquitectura multicapas estricta:

## Capa de presentacion API

Responsable de exponer endpoints REST, WebSocket y endpoints publicos. No debe contener logica de negocio compleja.

Componentes:

- Controllers REST.
- Handlers WebFlux.
- WebSocket handlers.
- DTOs de entrada y salida.
- Validaciones de request.
- OpenAPI / Swagger.

## Capa de aplicacion

Coordina casos de uso y transacciones de negocio. Es la capa donde viven los servicios de aplicacion.

Ejemplos:

- Crear pedido.
- Confirmar pago.
- Aplicar promocion.
- Asignar delivery.
- Registrar stock por venta.
- Cerrar caja.
- Generar backup.

## Capa de dominio

Contiene las reglas de negocio principales.

Ejemplos:

- Un mozo no puede cobrar pedidos.
- Una promocion no puede aplicarse fuera de su vigencia.
- Un pedido entregado no puede volver a preparacion.
- Un producto sin stock de insumos criticos debe marcarse como no disponible.
- Un delivery solo puede compartir ubicacion si tiene una entrega activa.

## Capa de infraestructura

Contiene adaptadores tecnicos.

Componentes:

- Repositorios PostgreSQL / R2DBC.
- Productores y consumidores Kafka.
- Clientes Redis.
- Clientes Google Maps / Routes API.
- Clientes Firebase Cloud Messaging.
- Cliente de almacenamiento local.
- Cliente de backups.
- Integraciones futuras con BigQuery.

## Capa de persistencia

Responsable de la base de datos, migraciones, indices, auditoria, historicos y respaldos.

Componentes:

- PostgreSQL.
- Flyway.
- Scripts de migracion.
- Indices.
- Vistas/materialized views.
- Tablas de auditoria.
- Tablas de eventos outbox.

# Tecnologias seleccionadas

## Backend

| Area | Tecnologia | Uso |
|---|---|---|
| Lenguaje | Java 21 LTS | Base estable, moderna y corporativa |
| Framework | Spring Boot 3.x | API principal |
| Web stack | Spring WebFlux | Endpoints reactivos, WebSocket, alta concurrencia I/O |
| Seguridad | Spring Security | Autenticacion y autorizacion |
| Tokens | JWT + refresh tokens | Sesiones seguras |
| Persistencia reactiva | Spring Data R2DBC | Acceso reactivo a PostgreSQL |
| Migraciones | Flyway | Versionado de base de datos |
| Eventos | Apache Kafka | Eventos asincronos y desacoplamiento |
| Cache | Redis | Cache, estados temporales y tracking caliente |
| Documentacion API | OpenAPI / Swagger | Contrato tecnico de endpoints |
| Testing | JUnit 5, Testcontainers, Mockito | Pruebas unitarias e integracion |
| Build | Gradle o Maven | Automatizacion del backend |

## Base de datos

| Area | Tecnologia | Uso |
|---|---|---|
| Motor principal | PostgreSQL | Base relacional transaccional |
| Extension geografica | PostGIS | Coordenadas, zonas, distancias y consultas espaciales |
| Backups | pg_dump + compresion | Respaldo diario |
| Auditoria | Tablas audit_log + outbox | Trazabilidad y eventos |
| Analitica futura | BigQuery | Historico, dashboards y BI |

## Frontend y aplicaciones

| Producto | Tecnologia | Uso |
|---|---|---|
| Panel desktop | React + TypeScript + Vite + Tauri | Administracion, caja y operaciones internas |
| Landing publica | Next.js + React + TypeScript | Carta, marca, contacto, captacion de clientes |
| App delivery | Flutter | GPS, estados, notificaciones, evidencia |

## Infraestructura local

| Servicio | Uso |
|---|---|
| Docker Compose | Levantar API, Redis, Kafka y conectarse al PostgreSQL externo |
| Nginx | Reverse proxy local o publico |
| Google Drive | Storage principal de imagenes, evidencias y documentos SUNAT |
| Volumen local cifrado | Fallback local, backups y contingencia |
| Logs rotativos | Diagnostico operativo |
| Health checks | Validacion de servicios activos |

# Justificacion de no usar Angular

No se recomienda usar Angular para este proyecto porque la necesidad real de Pikudo Chicken apunta a una aplicacion desktop liviana, mantenible y altamente productiva, no a una estructura pesada con boilerplate adicional. React con TypeScript, Vite y Tauri permite construir el panel administrativo con menor friccion, mejor velocidad de desarrollo, integracion directa con componentes reutilizables y mayor coherencia con la landing publica en Next.js. Para este caso, Angular no aporta una ventaja tecnica suficiente que justifique obligar al equipo a trabajar con un framework mas rigido y costoso de mantener.

# Modulos del backend

## Modulo de identidad y seguridad

Responsable de usuarios, roles, permisos, autenticacion y sesiones.

Funciones:

- Crear usuarios internos.
- Asignar roles: administrador, cajero, mozo, motorizado.
- Autenticar con usuario y contrasena.
- Emitir access token y refresh token.
- Invalidar sesiones.
- Registrar intentos fallidos.
- Controlar permisos por endpoint.

## Modulo de restaurante y configuracion

Responsable de la informacion base del negocio.

Funciones:

- Registrar datos del restaurante.
- Registrar local/sucursal principal.
- Configurar horarios de atencion.
- Configurar metodos de pago.
- Configurar parametros de caja.
- Configurar zonas de delivery.
- Configurar politica de backups.

Aunque no sea SaaS, se recomienda mantener tablas `restaurants` y `branches`. Esto permite que Pikudo crezca a una segunda sede sin redisenar la base.

## Modulo de catalogo

Responsable de productos, categorias, variantes, imagenes, combos y disponibilidad.

Funciones:

- Crear categorias.
- Crear productos.
- Crear variantes por producto.
- Registrar imagenes.
- Definir si un producto es comida, bebida, adicional, combo o promocion.
- Manejar disponibilidad por horario.
- Asociar productos con modificadores.
- Definir combos y sus componentes.

## Modulo de precios, eventos y promociones

Responsable de reglas comerciales.

Funciones:

- Crear promociones por fecha.
- Crear descuentos por producto, categoria o tipo de producto.
- Crear eventos comerciales: Dia del Pollo, feriados, aniversario, combos especiales.
- Aplicar descuentos por horario.
- Aplicar descuentos por metodo de pago.
- Aplicar cupones.
- Registrar descuentos aplicados por pedido.

## Modulo de mesas y salon

Responsable de la operacion interna del restaurante.

Funciones:

- Registrar mesas.
- Cambiar estados de mesa.
- Abrir sesion de mesa.
- Asignar mozo.
- Registrar cantidad de clientes.
- Asociar pedidos a mesa.
- Cerrar sesion despues de pago.

## Modulo de pedidos

Responsable del ciclo completo del pedido.

Funciones:

- Crear pedidos por salon, telefono, WhatsApp, recojo, delivery o web.
- Agregar productos y modificadores.
- Calcular subtotales, descuentos, impuestos y total.
- Confirmar pedido.
- Cambiar estados.
- Registrar historico de cambios.
- Emitir eventos hacia Kafka.

## Modulo de caja y pagos

Responsable de cobros, arqueo y metodos de pago.

Funciones:

- Registrar pagos en efectivo.
- Registrar pagos por Yape y Plin.
- Registrar pagos con tarjeta.
- Registrar transacciones de pasarela.
- Dividir pagos por metodo.
- Confirmar pago total o parcial.
- Cerrar caja diaria.
- Gestionar anulaciones y devoluciones.

## Modulo de comprobantes

Responsable de boletas, facturas y notas de credito.

Funciones:

- Generar comprobante interno.
- Registrar datos de cliente para boleta/factura.
- Preparar estructura para integracion futura con SUNAT.
- Registrar notas de credito.
- Asociar comprobantes a pagos y pedidos.

## Modulo de inventario

Responsable de insumos, recetas y stock.

Funciones:

- Registrar insumos.
- Registrar unidades de medida.
- Registrar almacenes.
- Registrar stock inicial.
- Registrar movimientos de entrada, salida, ajuste y merma.
- Asociar recetas a productos o variantes.
- Descontar insumos por venta.
- Alertar stock bajo.

## Modulo de delivery y tracking

Responsable de asignacion de repartidores, estados de entrega, ubicaciones y evidencia.

Funciones:

- Registrar motorizados.
- Registrar vehiculos.
- Crear entrega.
- Asignar pedido a motorizado.
- Recibir ubicacion GPS.
- Mostrar ubicacion al administrador/cajero.
- Mostrar tracking al cliente si aplica.
- Registrar evidencia de entrega.
- Calcular distancia y ETA usando proveedor externo.

## Modulo de notificaciones

Responsable de notificaciones internas, push y eventos.

Funciones:

- Notificar nuevo pedido a caja.
- Notificar asignacion al delivery.
- Notificar cambio de estado al cliente.
- Notificar pedido cancelado.
- Registrar logs de notificacion.

## Modulo de reportes

Responsable de consultas operativas.

Funciones:

- Ventas diarias.
- Ventas mensuales.
- Productos mas vendidos.
- Pedidos por origen.
- Pedidos delivery.
- Rendimiento de motorizados.
- Metodos de pago.
- Stock y rotacion de insumos.
- Promociones mas efectivas.

## Modulo de backups

Responsable de ejecutar y auditar respaldos.

Funciones:

- Ejecutar backup diario.
- Comprimir backup.
- Registrar checksum.
- Registrar ruta del archivo.
- Controlar retencion.
- Alertar fallo.
- Validar restauracion periodica.

# Modelo de base de datos

## Principios de modelado

El modelo de base de datos debe cumplir con estos criterios:

1. **Normalizacion minima hasta 3FN** para evitar duplicidad y mantener integridad.
2. **UUID como identificador primario** para evitar dependencia de secuencias simples y facilitar integraciones futuras.
3. **Timestamps con zona horaria** usando `timestamptz`.
4. **Campos de auditoria** en tablas principales: `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted_at`.
5. **Soft delete** para maestros como productos, categorias y usuarios.
6. **Historico separado** para estados de pedidos, delivery, caja y stock.
7. **Indices por fechas, estados y claves foraneas**.
8. **Eventos outbox** para publicar cambios importantes hacia Kafka de forma confiable.
9. **Particionamiento futuro** para tablas historicas de alto crecimiento.
10. **PostGIS** para ubicaciones, zonas y calculos geograficos.

## Diagrama logico simplificado

```text
restaurants 1---N branches
branches 1---N users
roles N---N users
branches 1---N product_categories
product_categories 1---N products
products 1---N product_variants
products N---N modifier_groups
product_variants 1---N product_prices
products/variants N---N promotions
branches 1---N dining_tables
branches 1---N table_sessions
orders 1---N order_items
order_items 1---N order_item_modifiers
orders 1---N payments
orders 1---N order_status_history
orders 0---1 deliveries
deliveries 1---N delivery_location_events
deliveries 1---N delivery_evidence
product_variants 1---N recipes
recipes 1---N recipe_items
inventory_items 1---N stock_movements
orders 1---N applied_discounts
orders 1---N receipts
orders 1---N outbox_events
```

# Diccionario de tablas normalizadas

## Nucleo del restaurante

### `restaurants`

Proposito: almacenar la entidad principal del restaurante.

Campos principales:

- `id uuid pk`
- `legal_name varchar(180)`
- `trade_name varchar(180)`
- `ruc varchar(20)`
- `phone varchar(30)`
- `email varchar(120)`
- `logo_url text`
- `created_at timestamptz`
- `updated_at timestamptz`

Relaciones:

- Uno a muchos con `branches`.

### `branches`

Proposito: registrar locales o sedes. Para la primera version se usara una sede principal, pero la tabla queda lista para crecimiento.

Campos principales:

- `id uuid pk`
- `restaurant_id uuid fk`
- `name varchar(120)`
- `address text`
- `phone varchar(30)`
- `opening_time time`
- `closing_time time`
- `timezone varchar(60)`
- `is_active boolean`
- `created_at timestamptz`
- `updated_at timestamptz`

Indices:

- `idx_branches_restaurant_id`
- `idx_branches_is_active`

### `business_settings`

Proposito: guardar configuraciones operativas por sede.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `setting_key varchar(120)`
- `setting_value jsonb`
- `created_at timestamptz`
- `updated_at timestamptz`

Regla:

- `unique(branch_id, setting_key)`.

## Seguridad y usuarios

### `users`

Proposito: usuarios internos del sistema.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk null`
- `username varchar(80)`
- `email varchar(120)`
- `password_hash text`
- `full_name varchar(180)`
- `phone varchar(30)`
- `is_active boolean`
- `last_login_at timestamptz`
- `created_at timestamptz`
- `updated_at timestamptz`
- `deleted_at timestamptz null`

Indices:

- `unique(username)`
- `unique(email)` cuando aplique.
- `idx_users_branch_id`

### `roles`

Proposito: catalogo de roles.

Valores iniciales:

- `ADMIN`
- `CASHIER`
- `WAITER`
- `DRIVER`
- `KITCHEN`

Campos principales:

- `id uuid pk`
- `code varchar(50)`
- `name varchar(100)`
- `description text`

### `permissions`

Proposito: permisos granulares.

Ejemplos:

- `PRODUCT_CREATE`
- `ORDER_CONFIRM`
- `PAYMENT_REGISTER`
- `DELIVERY_ASSIGN`
- `REPORT_VIEW`
- `BACKUP_RUN`

### `user_roles`

Proposito: relacion muchos a muchos entre usuarios y roles.

Campos:

- `user_id uuid fk`
- `role_id uuid fk`
- `created_at timestamptz`

Clave primaria:

- `(user_id, role_id)`.

### `role_permissions`

Proposito: relacion muchos a muchos entre roles y permisos.

Campos:

- `role_id uuid fk`
- `permission_id uuid fk`

Clave primaria:

- `(role_id, permission_id)`.

### `refresh_tokens`

Proposito: manejo seguro de sesiones renovables.

Campos:

- `id uuid pk`
- `user_id uuid fk`
- `token_hash text`
- `expires_at timestamptz`
- `revoked_at timestamptz null`
- `created_at timestamptz`

## Clientes y direcciones

### `customers`

Proposito: clientes registrados por web, telefono, WhatsApp o caja.

Campos principales:

- `id uuid pk`
- `full_name varchar(180)`
- `phone varchar(30)`
- `email varchar(120) null`
- `document_type varchar(20) null`
- `document_number varchar(30) null`
- `created_at timestamptz`
- `updated_at timestamptz`

Indices:

- `idx_customers_phone`
- `idx_customers_document`

### `customer_addresses`

Proposito: direcciones de entrega.

Campos principales:

- `id uuid pk`
- `customer_id uuid fk`
- `label varchar(80)`
- `address_line text`
- `reference text`
- `latitude numeric(10,7)`
- `longitude numeric(10,7)`
- `geo_point geography(Point, 4326)`
- `is_default boolean`
- `created_at timestamptz`

Indices:

- `idx_customer_addresses_customer_id`
- `idx_customer_addresses_geo_point` usando GiST.

## Catalogo de productos

### `product_categories`

Proposito: categorias de carta.

Categorias iniciales sugeridas:

- Pollos a la lena.
- Parrillas familiares.
- Combos parrilleros.
- Parrillas, BBQ y cortes.
- Piqueos.
- Adicionales.
- Saltados.
- Chaufas.
- Carnes.
- Pastas.
- Sopas.
- Ensaladas.
- Bebidas calientes.
- Bebidas frias.
- Frozen.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `parent_category_id uuid fk null`
- `name varchar(120)`
- `slug varchar(140)`
- `description text null`
- `display_order int`
- `is_active boolean`
- `created_at timestamptz`
- `updated_at timestamptz`

Regla:

- `unique(branch_id, slug)`.

### `products`

Proposito: producto base de carta.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `category_id uuid fk`
- `name varchar(160)`
- `slug varchar(180)`
- `description text`
- `product_type varchar(30)`
- `is_combo boolean`
- `is_available boolean`
- `requires_recipe boolean`
- `created_at timestamptz`
- `updated_at timestamptz`
- `deleted_at timestamptz null`

Valores sugeridos para `product_type`:

- `FOOD`
- `BEVERAGE`
- `ADDON`
- `COMBO`
- `SERVICE`

### `product_variants`

Proposito: registrar tamanos, porciones o versiones de un producto.

Ejemplos:

- 1/4 pollo.
- 1/2 pollo.
- 1 pollo.
- Papas amarillas.
- Gaseosa 600 ml.
- Gaseosa 1.5 L.
- Frozen 1/2 L.
- Frozen 1 L.

Campos principales:

- `id uuid pk`
- `product_id uuid fk`
- `name varchar(120)`
- `sku varchar(80) null`
- `portion_size varchar(80) null`
- `is_default boolean`
- `is_available boolean`
- `created_at timestamptz`
- `updated_at timestamptz`

### `product_prices`

Proposito: historico de precios por variante.

Campos principales:

- `id uuid pk`
- `variant_id uuid fk`
- `price numeric(12,2)`
- `currency char(3)` default `PEN`
- `valid_from timestamptz`
- `valid_to timestamptz null`
- `created_at timestamptz`

Regla:

- Una variante solo debe tener un precio vigente por fecha.

### `product_images`

Proposito: imagenes del producto para panel y landing.

Campos principales:

- `id uuid pk`
- `product_id uuid fk`
- `image_url text`
- `alt_text varchar(180)`
- `display_order int`
- `is_main boolean`
- `created_at timestamptz`

### `modifier_groups`

Proposito: grupos de opciones configurables.

Ejemplos:

- Tipo de papa.
- Tipo de ensalada.
- Salsas.
- Bebida incluida.
- Termino de carne.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `name varchar(120)`
- `min_selection int`
- `max_selection int`
- `is_required boolean`
- `created_at timestamptz`

### `modifiers`

Proposito: opciones dentro de un grupo.

Ejemplos:

- Papas fritas clasicas.
- Papas amarillas.
- Ensalada cocida.
- Ensalada clasica.
- Cremas.
- Chorizo.
- Anticucho.

Campos principales:

- `id uuid pk`
- `modifier_group_id uuid fk`
- `name varchar(120)`
- `extra_price numeric(12,2)`
- `is_active boolean`

### `product_modifier_groups`

Proposito: asociar productos con grupos de modificadores.

Campos:

- `product_id uuid fk`
- `modifier_group_id uuid fk`

Clave primaria:

- `(product_id, modifier_group_id)`.

### `combo_components`

Proposito: definir que productos o variantes componen un combo.

Campos principales:

- `id uuid pk`
- `combo_product_id uuid fk`
- `component_variant_id uuid fk`
- `quantity numeric(10,3)`
- `is_required boolean`
- `created_at timestamptz`

Ejemplo:

Un combo podria contener 1/4 pollo, papas, ensalada, gaseosa y cremas.

## Promociones, descuentos y eventos

### `commercial_events`

Proposito: eventos comerciales que agrupan promociones.

Ejemplos:

- Dia del Pollo a la Brasa.
- Promocion de aniversario.
- Campana de fin de semana.
- Evento de bebidas frozen.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `name varchar(160)`
- `description text`
- `start_at timestamptz`
- `end_at timestamptz`
- `is_active boolean`
- `created_at timestamptz`

### `promotions`

Proposito: promocion o descuento aplicable.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `commercial_event_id uuid fk null`
- `name varchar(160)`
- `description text`
- `discount_type varchar(30)`
- `discount_value numeric(12,2)`
- `max_discount_amount numeric(12,2) null`
- `min_order_amount numeric(12,2) null`
- `start_at timestamptz`
- `end_at timestamptz`
- `is_active boolean`
- `created_at timestamptz`

Valores de `discount_type`:

- `PERCENTAGE`
- `FIXED_AMOUNT`
- `SPECIAL_PRICE`
- `FREE_ITEM`

### `promotion_targets`

Proposito: definir a que aplica una promocion.

Campos principales:

- `id uuid pk`
- `promotion_id uuid fk`
- `target_type varchar(30)`
- `target_id uuid null`

Valores de `target_type`:

- `PRODUCT`
- `VARIANT`
- `CATEGORY`
- `PRODUCT_TYPE`
- `ORDER_TOTAL`
- `PAYMENT_METHOD`

### `coupons`

Proposito: cupones para landing o campanas.

Campos principales:

- `id uuid pk`
- `promotion_id uuid fk`
- `code varchar(60)`
- `max_uses int null`
- `uses_count int`
- `valid_from timestamptz`
- `valid_to timestamptz`
- `is_active boolean`

### `applied_discounts`

Proposito: registrar descuentos aplicados a un pedido.

Campos principales:

- `id uuid pk`
- `order_id uuid fk`
- `promotion_id uuid fk null`
- `coupon_id uuid fk null`
- `description text`
- `amount numeric(12,2)`
- `created_at timestamptz`

## Mesas y salon

### `dining_tables`

Proposito: mesas del local.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `code varchar(30)`
- `capacity int`
- `status varchar(30)`
- `is_active boolean`
- `created_at timestamptz`
- `updated_at timestamptz`

Estados:

- `FREE`
- `OCCUPIED`
- `ORDER_SENT`
- `WAITING_PAYMENT`
- `AVAILABLE`
- `OUT_OF_SERVICE`

### `table_sessions`

Proposito: sesion de atencion de una mesa.

Campos principales:

- `id uuid pk`
- `table_id uuid fk`
- `waiter_user_id uuid fk`
- `customer_count int`
- `opened_at timestamptz`
- `closed_at timestamptz null`
- `status varchar(30)`

Estados:

- `OPEN`
- `ORDERING`
- `WAITING_PAYMENT`
- `PAID`
- `CLOSED`
- `CANCELLED`

## Pedidos

### `orders`

Proposito: cabecera de pedido.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `customer_id uuid fk null`
- `table_session_id uuid fk null`
- `order_code varchar(40)`
- `tracking_code varchar(80)`
- `source varchar(30)`
- `service_type varchar(30)`
- `status varchar(30)`
- `subtotal numeric(12,2)`
- `discount_total numeric(12,2)`
- `tax_total numeric(12,2)`
- `delivery_fee numeric(12,2)`
- `total_amount numeric(12,2)`
- `notes text null`
- `created_by uuid fk null`
- `created_at timestamptz`
- `updated_at timestamptz`

Valores de `source`:

- `DINE_IN`
- `WEB`
- `WHATSAPP`
- `PHONE`
- `WALK_IN`

Valores de `service_type`:

- `DINE_IN`
- `DELIVERY`
- `PICKUP`

Estados del pedido:

- `DRAFT`
- `CONFIRMED`
- `PREPARING`
- `READY`
- `ASSIGNED`
- `ON_ROUTE`
- `DELIVERED`
- `PAID`
- `CANCELLED`

Indices:

- `idx_orders_branch_created_at`
- `idx_orders_status`
- `idx_orders_tracking_code unique`
- `idx_orders_source`

### `order_items`

Proposito: detalle del pedido.

Campos principales:

- `id uuid pk`
- `order_id uuid fk`
- `product_id uuid fk`
- `variant_id uuid fk`
- `product_name_snapshot varchar(180)`
- `variant_name_snapshot varchar(120)`
- `unit_price numeric(12,2)`
- `quantity numeric(10,3)`
- `discount_amount numeric(12,2)`
- `line_total numeric(12,2)`
- `notes text null`
- `created_at timestamptz`

Justificacion:

Se guardan nombres y precios snapshot para que el historico no cambie si se edita la carta.

### `order_item_modifiers`

Proposito: modificadores seleccionados en un item de pedido.

Campos principales:

- `id uuid pk`
- `order_item_id uuid fk`
- `modifier_id uuid fk null`
- `name_snapshot varchar(120)`
- `extra_price numeric(12,2)`
- `quantity numeric(10,3)`

### `order_status_history`

Proposito: historial de cambios de estado.

Campos principales:

- `id uuid pk`
- `order_id uuid fk`
- `from_status varchar(30) null`
- `to_status varchar(30)`
- `changed_by uuid fk null`
- `reason text null`
- `created_at timestamptz`

## Caja y pagos

### `cash_registers`

Proposito: cajas fisicas o logicas.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `name varchar(120)`
- `is_active boolean`

### `cash_sessions`

Proposito: turno de caja.

Campos principales:

- `id uuid pk`
- `cash_register_id uuid fk`
- `opened_by uuid fk`
- `closed_by uuid fk null`
- `opening_amount numeric(12,2)`
- `closing_amount numeric(12,2) null`
- `opened_at timestamptz`
- `closed_at timestamptz null`
- `status varchar(30)`

### `payment_methods`

Proposito: metodos de pago habilitados.

Valores iniciales:

- `CASH`
- `YAPE`
- `PLIN`
- `CARD`
- `CULQI`
- `TRANSFER`

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `code varchar(40)`
- `name varchar(80)`
- `is_active boolean`

### `payments`

Proposito: pagos asociados a pedidos.

Campos principales:

- `id uuid pk`
- `order_id uuid fk`
- `cash_session_id uuid fk null`
- `payment_method_id uuid fk`
- `amount numeric(12,2)`
- `status varchar(30)`
- `reference_code varchar(120) null`
- `paid_at timestamptz null`
- `created_by uuid fk`
- `created_at timestamptz`

Estados:

- `PENDING`
- `PAID`
- `REJECTED`
- `CANCELLED`
- `REFUNDED`

### `payment_transactions`

Proposito: registrar respuesta de pasarela o validacion externa.

Campos principales:

- `id uuid pk`
- `payment_id uuid fk`
- `provider varchar(60)`
- `provider_transaction_id varchar(160)`
- `raw_request jsonb null`
- `raw_response jsonb null`
- `status varchar(40)`
- `created_at timestamptz`

## Comprobantes

### `receipts`

Proposito: comprobantes internos o tributarios.

Campos principales:

- `id uuid pk`
- `order_id uuid fk`
- `receipt_type varchar(30)`
- `series varchar(20)`
- `number varchar(40)`
- `customer_document_type varchar(20) null`
- `customer_document_number varchar(30) null`
- `customer_name varchar(180) null`
- `total_amount numeric(12,2)`
- `status varchar(30)`
- `issued_at timestamptz`
- `created_at timestamptz`

Tipos:

- `BOLETA`
- `FACTURA`
- `INTERNAL_TICKET`

### `credit_notes`

Proposito: notas de credito o anulaciones.

Campos principales:

- `id uuid pk`
- `receipt_id uuid fk`
- `reason text`
- `amount numeric(12,2)`
- `status varchar(30)`
- `issued_at timestamptz`

## Inventario

### `measurement_units`

Proposito: unidades de medida.

Ejemplos:

- Unidad.
- Kilogramo.
- Gramo.
- Litro.
- Mililitro.
- Porcion.

Campos:

- `id uuid pk`
- `code varchar(30)`
- `name varchar(80)`

### `inventory_items`

Proposito: insumos.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `unit_id uuid fk`
- `name varchar(160)`
- `sku varchar(80) null`
- `minimum_stock numeric(12,3)`
- `current_stock numeric(12,3)`
- `is_active boolean`
- `created_at timestamptz`
- `updated_at timestamptz`

Ejemplos:

- Pollo entero.
- Papa amarilla.
- Papa blanca.
- Chorizo.
- Lomo fino.
- Gaseosa 600 ml.
- Gaseosa 1.5 L.
- Limonada.
- Cremas.

### `warehouses`

Proposito: almacenes fisicos o logicos.

Campos:

- `id uuid pk`
- `branch_id uuid fk`
- `name varchar(120)`
- `is_active boolean`

### `stock_movements`

Proposito: movimientos de inventario.

Campos principales:

- `id uuid pk`
- `warehouse_id uuid fk`
- `inventory_item_id uuid fk`
- `movement_type varchar(30)`
- `quantity numeric(12,3)`
- `unit_cost numeric(12,2) null`
- `reference_type varchar(40) null`
- `reference_id uuid null`
- `notes text null`
- `created_by uuid fk null`
- `created_at timestamptz`

Tipos:

- `PURCHASE`
- `SALE_CONSUMPTION`
- `ADJUSTMENT`
- `WASTE`
- `TRANSFER_IN`
- `TRANSFER_OUT`

### `recipes`

Proposito: receta asociada a producto o variante.

Campos principales:

- `id uuid pk`
- `variant_id uuid fk`
- `name varchar(160)`
- `is_active boolean`
- `created_at timestamptz`

### `recipe_items`

Proposito: insumos consumidos por receta.

Campos principales:

- `id uuid pk`
- `recipe_id uuid fk`
- `inventory_item_id uuid fk`
- `quantity numeric(12,3)`
- `unit_id uuid fk`

## Delivery y GPS

### `drivers`

Proposito: perfil de motorizado.

Campos principales:

- `id uuid pk`
- `user_id uuid fk`
- `license_number varchar(80) null`
- `status varchar(30)`
- `created_at timestamptz`
- `updated_at timestamptz`

Estados:

- `AVAILABLE`
- `BUSY`
- `ON_ROUTE`
- `OFFLINE`
- `OUT_OF_SERVICE`

### `vehicles`

Proposito: vehiculos de delivery.

Campos principales:

- `id uuid pk`
- `driver_id uuid fk null`
- `plate varchar(30)`
- `vehicle_type varchar(40)`
- `brand varchar(80) null`
- `model varchar(80) null`
- `is_active boolean`

### `delivery_zones`

Proposito: zonas de reparto.

Campos principales:

- `id uuid pk`
- `branch_id uuid fk`
- `name varchar(120)`
- `polygon geography(Polygon, 4326) null`
- `base_fee numeric(12,2)`
- `is_active boolean`

### `deliveries`

Proposito: entrega asociada a pedido.

Campos principales:

- `id uuid pk`
- `order_id uuid fk`
- `driver_id uuid fk null`
- `customer_address_id uuid fk null`
- `status varchar(30)`
- `assigned_at timestamptz null`
- `picked_up_at timestamptz null`
- `delivered_at timestamptz null`
- `estimated_distance_meters int null`
- `estimated_duration_seconds int null`
- `created_at timestamptz`

Estados:

- `PENDING`
- `ASSIGNED`
- `ACCEPTED`
- `PICKED_UP`
- `ON_ROUTE`
- `DELIVERED`
- `FAILED`
- `CANCELLED`

### `delivery_stops`

Proposito: permitir una o varias paradas de entrega.

Campos principales:

- `id uuid pk`
- `delivery_id uuid fk`
- `stop_order int`
- `address_text text`
- `reference text null`
- `latitude numeric(10,7)`
- `longitude numeric(10,7)`
- `geo_point geography(Point, 4326)`
- `status varchar(30)`

### `delivery_location_events`

Proposito: historico de ubicaciones del motorizado durante una entrega.

Campos principales:

- `id uuid pk`
- `delivery_id uuid fk`
- `driver_id uuid fk`
- `latitude numeric(10,7)`
- `longitude numeric(10,7)`
- `geo_point geography(Point, 4326)`
- `accuracy_meters numeric(8,2) null`
- `speed_mps numeric(8,2) null`
- `battery_level int null`
- `recorded_at timestamptz`
- `created_at timestamptz`

Indices:

- `idx_delivery_location_delivery_time`
- `idx_delivery_location_driver_time`
- `idx_delivery_location_geo_point` usando GiST.

### `delivery_evidence`

Proposito: evidencia de entrega.

Campos principales:

- `id uuid pk`
- `delivery_id uuid fk`
- `file_url text`
- `evidence_type varchar(30)`
- `notes text null`
- `created_at timestamptz`

Tipos:

- `PHOTO`
- `SIGNATURE`
- `NOTE`

## Notificaciones y dispositivos

### `notification_devices`

Proposito: tokens de dispositivos para push.

Campos principales:

- `id uuid pk`
- `user_id uuid fk null`
- `customer_id uuid fk null`
- `device_token text`
- `platform varchar(30)`
- `is_active boolean`
- `created_at timestamptz`
- `updated_at timestamptz`

### `notifications`

Proposito: notificaciones generadas.

Campos principales:

- `id uuid pk`
- `recipient_user_id uuid fk null`
- `recipient_customer_id uuid fk null`
- `channel varchar(30)`
- `title varchar(160)`
- `body text`
- `payload jsonb null`
- `status varchar(30)`
- `created_at timestamptz`
- `sent_at timestamptz null`

Canales:

- `IN_APP`
- `PUSH`
- `EMAIL`
- `WHATSAPP_MANUAL`

## Auditoria, eventos y backups

### `audit_logs`

Proposito: registrar operaciones criticas.

Campos principales:

- `id uuid pk`
- `user_id uuid fk null`
- `action varchar(120)`
- `entity_name varchar(120)`
- `entity_id uuid null`
- `old_value jsonb null`
- `new_value jsonb null`
- `ip_address varchar(80) null`
- `user_agent text null`
- `created_at timestamptz`

### `outbox_events`

Proposito: patron outbox para publicar eventos a Kafka sin perder consistencia transaccional.

Campos principales:

- `id uuid pk`
- `aggregate_type varchar(120)`
- `aggregate_id uuid`
- `event_type varchar(160)`
- `payload jsonb`
- `status varchar(30)`
- `created_at timestamptz`
- `published_at timestamptz null`
- `retry_count int`
- `last_error text null`

Estados:

- `PENDING`
- `PUBLISHED`
- `FAILED`

### `backup_jobs`

Proposito: registrar cada ejecucion de backup.

Campos principales:

- `id uuid pk`
- `job_type varchar(40)`
- `status varchar(30)`
- `file_path text null`
- `file_size_bytes bigint null`
- `checksum_sha256 varchar(128) null`
- `started_at timestamptz`
- `finished_at timestamptz null`
- `error_message text null`
- `created_at timestamptz`

# Esquema SQL inicial referencial

El siguiente ejemplo no reemplaza las migraciones completas de Flyway, pero define el estilo que deben seguir los scripts.

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE restaurants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legal_name VARCHAR(180) NOT NULL,
    trade_name VARCHAR(180) NOT NULL,
    ruc VARCHAR(20),
    phone VARCHAR(30),
    email VARCHAR(120),
    logo_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    name VARCHAR(120) NOT NULL,
    address TEXT,
    phone VARCHAR(30),
    opening_time TIME,
    closing_time TIME,
    timezone VARCHAR(60) NOT NULL DEFAULT 'America/Lima',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_branches_restaurant_id ON branches(restaurant_id);

CREATE TABLE product_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id UUID NOT NULL REFERENCES branches(id),
    parent_category_id UUID REFERENCES product_categories(id),
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL,
    description TEXT,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(branch_id, slug)
);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id UUID NOT NULL REFERENCES branches(id),
    category_id UUID NOT NULL REFERENCES product_categories(id),
    name VARCHAR(160) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description TEXT,
    product_type VARCHAR(30) NOT NULL,
    is_combo BOOLEAN NOT NULL DEFAULT false,
    is_available BOOLEAN NOT NULL DEFAULT true,
    requires_recipe BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    UNIQUE(branch_id, slug)
);

CREATE INDEX idx_products_branch_category ON products(branch_id, category_id);
CREATE INDEX idx_products_type_available ON products(product_type, is_available);
```

# Flyway y versionado de base de datos

Flyway debe estar configurado desde el primer commit del backend.

Estructura recomendada:

```text
src/main/resources/db/migration/
  V1__init_extensions.sql
  V2__create_core_restaurant_tables.sql
  V3__create_security_tables.sql
  V4__create_catalog_tables.sql
  V5__create_promotions_tables.sql
  V6__create_orders_tables.sql
  V7__create_payments_tables.sql
  V8__create_inventory_tables.sql
  V9__create_delivery_tracking_tables.sql
  V10__create_notifications_outbox_audit.sql
  V11__seed_initial_roles_permissions.sql
  V12__seed_pikudo_initial_menu_categories.sql
```

Reglas:

1. No editar una migracion ya aplicada en produccion.
2. Crear una nueva migracion para cada cambio.
3. Separar estructura, indices y seed data cuando sea posible.
4. Ejecutar migraciones en ambiente local y staging antes de produccion.
5. Versionar scripts de rollback manual cuando el cambio sea riesgoso.

# Kafka y eventos internos

Kafka se mantendra, pero debe usarse con criterio. No todo debe ser evento. La base transaccional sigue siendo PostgreSQL.

## Eventos recomendados

| Evento | Cuando ocurre | Consumidores |
|---|---|---|
| `OrderCreated` | Se crea un pedido confirmado | Notificaciones, reportes |
| `OrderStatusChanged` | Cambia estado de pedido | WebSocket, cliente, caja |
| `PaymentRegistered` | Se registra pago | Caja, reportes, auditoria |
| `DeliveryAssigned` | Se asigna motorizado | App delivery, notificaciones |
| `DriverLocationUpdated` | Motorizado envia ubicacion | Tracking, WebSocket, Redis |
| `StockMovementCreated` | Se mueve inventario | Reportes, alertas |
| `BackupCompleted` | Finaliza backup | Auditoria, alertas |
| `PromotionApplied` | Se aplica descuento | Analitica, reportes |

## Topicos iniciales

```text
pikudo.orders
pikudo.payments
pikudo.delivery
pikudo.tracking
pikudo.inventory
pikudo.notifications
pikudo.audit
pikudo.backups
```

## Patron Outbox

Para evitar que una transaccion se guarde en PostgreSQL pero el evento se pierda en Kafka, se usara el patron outbox:

1. El caso de uso guarda el pedido y el evento en `outbox_events` dentro de la misma transaccion.
2. Un publicador lee eventos `PENDING`.
3. Publica en Kafka.
4. Marca el evento como `PUBLISHED`.
5. Si falla, incrementa `retry_count` y registra `last_error`.

# Redis

Redis se usara para informacion temporal y de alta lectura.

Usos recomendados:

- Cache de carta publica.
- Cache de categorias y productos activos.
- Sesiones operativas temporales.
- Estado actual de mesas.
- Ultima ubicacion de delivery.
- Locks de procesos criticos.
- Rate limiting.
- Canales pub/sub internos si se requiere.

Claves sugeridas:

```text
menu:branch:{branchId}
table:status:{tableId}
delivery:last-location:{deliveryId}
order:tracking:{trackingCode}
rate-limit:user:{userId}
```

# Plan concreto para GPS y tracking

## Objetivo

Permitir que el restaurante visualice el recorrido del delivery y que el cliente pueda consultar el avance del pedido mediante un enlace o pantalla de seguimiento.

## Principios

1. El tracking solo debe activarse cuando el motorizado tenga una entrega asignada.
2. El sistema no debe guardar ubicacion permanente fuera de entregas activas.
3. La ubicacion actual debe almacenarse en Redis para lectura rapida.
4. El historico debe guardarse en PostgreSQL para auditoria.
5. El calculo de rutas debe delegarse a Google Maps / Routes API u otro proveedor.
6. PostGIS debe usarse para almacenar puntos, zonas y consultas geograficas.

## Flujo GPS inicial

```text
Pedido delivery confirmado
        |
Caja asigna motorizado
        |
Backend crea delivery
        |
Flutter recibe notificacion
        |
Motorizado acepta pedido
        |
App inicia tracking activo
        |
Cada 5-10 segundos envia ubicacion
        |
Backend guarda ultima ubicacion en Redis
        |
Backend guarda historico en PostgreSQL
        |
Backend emite evento DriverLocationUpdated
        |
Panel desktop y cliente reciben actualizacion por WebSocket
```

## Frecuencia recomendada

| Estado | Frecuencia GPS |
|---|---|
| Motorizado disponible sin pedido | No enviar ubicacion constante |
| Pedido asignado, aun no recogido | Cada 15-30 segundos |
| En camino a cliente | Cada 5-10 segundos |
| App en segundo plano | Cada 15-30 segundos, segun permisos de Android |
| Pedido entregado | Detener tracking |

## Endpoints GPS

```text
POST /api/v1/deliveries/{deliveryId}/accept
POST /api/v1/deliveries/{deliveryId}/reject
POST /api/v1/deliveries/{deliveryId}/locations
GET  /api/v1/deliveries/{deliveryId}/last-location
GET  /api/v1/orders/tracking/{trackingCode}
WS   /ws/tracking/orders/{trackingCode}
WS   /ws/admin/deliveries/{deliveryId}
```

## Payload de ubicacion

```json
{
  "latitude": -12.046374,
  "longitude": -77.042793,
  "accuracyMeters": 8.5,
  "speedMps": 4.2,
  "batteryLevel": 76,
  "recordedAt": "2026-07-09T18:30:00-05:00"
}
```

## Fases del GPS

### Fase GPS 1 - Tracking basico

- Flutter envia ubicacion.
- Backend guarda ultima ubicacion en Redis.
- Backend guarda historico en PostgreSQL.
- Panel admin muestra motorizado en mapa.
- Motorizado abre Google Maps con destino mediante deep link.

### Fase GPS 2 - Tracking para cliente

- Backend genera tracking code.
- Cliente abre enlace publico.
- Web muestra estados del pedido.
- Web muestra ubicacion aproximada cuando el pedido esta en camino.
- WebSocket actualiza posicion sin recargar.

### Fase GPS 3 - ETA y rutas

- Integrar Google Routes API.
- Calcular distancia y tiempo estimado.
- Guardar `estimated_distance_meters` y `estimated_duration_seconds`.
- Recalcular ETA solo cuando cambie significativamente la ubicacion para evitar costos.

### Fase GPS 4 - Multiples paradas

- Activar `delivery_stops`.
- Permitir agrupar varios pedidos por motorizado.
- Ordenar paradas manualmente al inicio.
- Luego evaluar optimizacion de ruta con proveedor externo.

### Fase GPS 5 - Analitica de delivery

- Calcular tiempo promedio de entrega.
- Calcular zonas con mayor demanda.
- Calcular rendimiento por motorizado.
- Exportar historicos a BigQuery.

# Backups automaticos diarios

## Objetivo

Proteger la base de datos del restaurante ante fallos del equipo, corrupcion de datos, error humano o perdida fisica del servidor.

## Estrategia recomendada

La estrategia mas segura es implementar backup en dos niveles:

1. **Backup coordinado por backend** mediante un modulo `BackupService` y registros en `backup_jobs`.
2. **Backup operativo por sistema** mediante cron, Task Scheduler o contenedor auxiliar que ejecute `pg_dump`.

Aunque se puede ejecutar un cron job dentro del backend, para produccion es mas confiable que el proceso de backup sea un servicio separado o una tarea del sistema operativo. El backend puede coordinar, auditar y notificar el resultado.

## Politica de backup

| Elemento | Politica |
|---|---|
| Frecuencia | Diario |
| Hora sugerida | 02:00 a.m. |
| Formato | `.dump.gz` o `.sql.gz` |
| Compresion | gzip o zstd |
| Checksum | SHA-256 |
| Retencion diaria | 30 dias |
| Retencion mensual | 12 meses |
| Ubicacion primaria | Disco local externo o particion separada |
| Ubicacion secundaria | Nube o almacenamiento externo |
| Prueba de restauracion | Mensual |

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
Elimina backups vencidos segun retencion
        |
Emite evento BackupCompleted
```

## Restauracion

Cada backup debe tener instrucciones de restauracion.

```bash
createdb pikudo_restore_test
pg_restore \
  --host=localhost \
  --port=5432 \
  --username=pikudo_app \
  --dbname=pikudo_restore_test \
  /backups/pikudo_YYYYMMDD_HHMMSS.dump
```

Regla obligatoria:

> Un backup que nunca se prueba no puede considerarse un backup confiable.

# Analitica y BigQuery a futuro

## Problema a prevenir

Si el mismo servidor local responde caja, pedidos, tracking, landing y reportes historicos pesados, con el tiempo los reportes pueden afectar la operacion diaria.

## Solucion por fases

### Fase analitica 1 - Reportes operativos en PostgreSQL

- Consultas con indices.
- Vistas para ventas diarias.
- Materialized views para productos mas vendidos.
- Reportes limitados por rango de fecha.

### Fase analitica 2 - Tablas agregadas

- Tabla `daily_sales_summary`.
- Tabla `daily_product_sales_summary`.
- Tabla `daily_payment_summary`.
- Tabla `daily_delivery_summary`.

### Fase analitica 3 - Export batch

- Export nocturno de ventas, pedidos, pagos y productos.
- Archivos Parquet/CSV.
- Carga a BigQuery.

### Fase analitica 4 - Event streaming

- Kafka como fuente de eventos.
- Conector o worker para exportar eventos.
- BigQuery como data warehouse.
- Dashboards externos sin tocar la base operacional.

# Endpoints API propuestos

## Autenticacion

```text
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /api/v1/auth/me
```

## Catalogo

```text
GET    /api/v1/categories
POST   /api/v1/categories
PUT    /api/v1/categories/{id}
DELETE /api/v1/categories/{id}

GET    /api/v1/products
POST   /api/v1/products
GET    /api/v1/products/{id}
PUT    /api/v1/products/{id}
DELETE /api/v1/products/{id}

POST   /api/v1/products/{id}/variants
POST   /api/v1/products/{id}/images
POST   /api/v1/products/{id}/availability
```

## Promociones

```text
GET    /api/v1/promotions
POST   /api/v1/promotions
PUT    /api/v1/promotions/{id}
POST   /api/v1/promotions/{id}/targets
POST   /api/v1/promotions/preview
```

## Pedidos

```text
GET    /api/v1/orders
POST   /api/v1/orders
GET    /api/v1/orders/{id}
POST   /api/v1/orders/{id}/confirm
POST   /api/v1/orders/{id}/cancel
POST   /api/v1/orders/{id}/items
DELETE /api/v1/orders/{id}/items/{itemId}
POST   /api/v1/orders/{id}/status
GET    /api/v1/orders/tracking/{trackingCode}
```

## Caja y pagos

```text
POST /api/v1/cash-sessions/open
POST /api/v1/cash-sessions/{id}/close
GET  /api/v1/cash-sessions/current
POST /api/v1/orders/{id}/payments
GET  /api/v1/payments
POST /api/v1/payments/{id}/refund
```

## Delivery

```text
GET  /api/v1/deliveries
POST /api/v1/orders/{id}/delivery
POST /api/v1/deliveries/{id}/assign
POST /api/v1/deliveries/{id}/accept
POST /api/v1/deliveries/{id}/reject
POST /api/v1/deliveries/{id}/pickup
POST /api/v1/deliveries/{id}/complete
POST /api/v1/deliveries/{id}/locations
GET  /api/v1/deliveries/{id}/last-location
POST /api/v1/deliveries/{id}/evidence
```

## Inventario

```text
GET  /api/v1/inventory/items
POST /api/v1/inventory/items
POST /api/v1/inventory/movements
GET  /api/v1/inventory/low-stock
POST /api/v1/recipes
POST /api/v1/recipes/{id}/items
```

## Reportes

```text
GET /api/v1/reports/sales/daily
GET /api/v1/reports/sales/monthly
GET /api/v1/reports/products/top
GET /api/v1/reports/payments
GET /api/v1/reports/delivery
GET /api/v1/reports/inventory
```

## Backups

```text
GET  /api/v1/backups
POST /api/v1/backups/run
GET  /api/v1/backups/{id}
POST /api/v1/backups/{id}/verify
```

# Seguridad

## Reglas principales

1. Todas las rutas internas deben requerir JWT.
2. Las rutas publicas deben estar limitadas a landing, carta y tracking publico.
3. Los tokens deben tener expiracion corta.
4. Los refresh tokens deben guardarse hasheados.
5. Las contrasenas deben almacenarse con BCrypt o Argon2.
6. Los endpoints criticos deben registrar auditoria.
7. Debe existir rate limiting en login y tracking publico.
8. No se debe exponer informacion sensible en notificaciones push.
9. Los backups deben estar protegidos por permisos de sistema y, si es posible, cifrados.
10. La ubicacion del motorizado solo debe registrarse durante una entrega activa.

## Matriz de permisos base

| Modulo | Admin | Cajero | Mozo | Motorizado |
|---|---:|---:|---:|---:|
| Usuarios | Si | No | No | No |
| Productos | Si | Consulta | Consulta | No |
| Pedidos | Si | Si | Crear/consultar | Ver asignados |
| Caja | Si | Si | No | No |
| Delivery | Si | Si | No | Actualizar propios |
| Inventario | Si | Consulta limitada | No | No |
| Reportes | Si | Limitado | No | No |
| Backups | Si | No | No | No |

# Observabilidad y logs

## Logs minimos

- Login exitoso/fallido.
- Creacion y cancelacion de pedidos.
- Cambios de estado.
- Pagos registrados.
- Asignaciones de delivery.
- Fallos de notificacion.
- Fallos de backup.
- Errores de integracion externa.

## Metricas recomendadas

- Pedidos por hora.
- Tiempo promedio de preparacion.
- Tiempo promedio de entrega.
- Uso de CPU y memoria.
- Tiempo de respuesta de endpoints.
- Eventos Kafka pendientes.
- Cache hit ratio en Redis.
- Backups exitosos/fallidos.

## Herramientas opcionales

- Actuator para health checks.
- Prometheus para metricas.
- Grafana para dashboard local.
- Loki o archivos rotativos para logs.

# Despliegue on-premise

## Opcion recomendada

Usar Docker Compose para levantar los servicios del servidor local.

Servicios:

```text
pikudo-api
postgres
redis
kafka
nginx
backup-runner
```

## Estructura sugerida

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

## Variables de entorno

```text
DB_HOST=global_postgres_db
DB_PORT=5432
DB_NAME=pikudo_db
DB_USERNAME=pikudo_app
DB_PASSWORD=********
JWT_SECRET=********
REDIS_HOST=redis
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
APP_STORAGE_PROVIDER=google-drive
GOOGLE_DRIVE_ENABLED=true
DRIVE_OAUTH_CLIENT_ID=********
DRIVE_OAUTH_CLIENT_SECRET=********
DRIVE_OAUTH_REFRESH_TOKEN=********
RESEND_ENABLED=true
RESEND_API_KEY=********
SUNAT_ENABLED=false
SUNAT_MODE=disabled
SUNAT_PFX_BASE64=********
```

# Plan por fases

La numeracion canonica del roadmap operativo se mantiene en `TASKS.md` y `docs/context/04_IMPLEMENTATION_ROADMAP.md`.

## Fase 0 - Contexto y decisiones

Completada. Consolida `AGENTS.md`, documentos de contexto y decisiones tecnicas.

## Fase 1 - Infraestructura local

Completada. Java 21, PostgreSQL externo `global_postgres_db`, Flyway, perfiles `.env`, Docker Compose, Redis, Kafka y Actuator.

## Fase 2 - Seguridad base

Completada. JWT configurable, refresh tokens, roles, permisos, auditoria minima y endpoints de sesion.

## Fase 3 - Integraciones base y storage

Activa. Preparar `StorageService`, provider local, provider Google Drive, Resend API, variables SUNAT y configuracion segura de `.pfx`.

## Fase 4 - Catalogo real con Drive

Categorias, productos, variantes, precios historicos, modificadores, combos e imagenes en Google Drive mediante `StorageService`.

## Fase 5 - Flujo operativo de pedidos, mesas y caja

Separar estado operativo, estado de pago y estado SUNAT. Implementar historial de estados, sesiones de mesa, snapshots robustos y caja/pagos.

## Fase 6 - Comprobantes SUNAT

Factura, boleta simple, boleta con documento, nota de credito, nota de debito, XML firmado con Project OpenUBL, CDR y almacenamiento en Drive.

## Fase 7 - Delivery, GPS y tracking real

Entregas normalizadas, tracking solo con entrega activa, ultima ubicacion en Redis, historico PostgreSQL, vista publica aproximada y mapa exacto admin/Flutter.

## Fase 8 - Promociones, descuentos y eventos

Eventos comerciales, promociones, cupones, reglas de descuento y registro de descuentos aplicados.

## Fase 9 - Inventario y recetas

Insumos, unidades, almacenes, recetas, movimientos de stock y alertas de stock bajo.

## Fase 10 - Backups, auditoria, reportes y hardening

Backup diario restaurable, auditoria ampliada, reportes operativos, indices finales y ruta futura a BigQuery.

# Criterios de aceptacion

## Backend

- La API inicia correctamente desde Docker Compose.
- Flyway ejecuta todas las migraciones sin errores.
- El login emite JWT valido.
- Los roles bloquean endpoints no autorizados.
- El catalogo permite crear productos, variantes y precios.
- Un pedido puede crearse, confirmarse, pagarse y cerrarse.
- El historial de estados queda registrado.
- Kafka recibe eventos principales.
- Redis cachea carta y ultima ubicacion.
- Los backups se ejecutan y registran correctamente.

## Base de datos

- No existen productos duplicados por slug en la misma sede.
- Los pedidos guardan snapshot de nombre y precio.
- Los pagos no superan el total del pedido salvo reglas controladas.
- Las promociones no se aplican fuera de vigencia.
- El stock se descuenta por receta cuando el pedido se confirma.
- Las ubicaciones se guardan con fecha y delivery asociado.
- Los indices principales existen.

## GPS

- La app Flutter envia ubicacion solo con delivery activo.
- El panel ve la ultima ubicacion sin recargar.
- El historico se guarda en PostgreSQL.
- El cliente no puede ver datos sensibles del motorizado.
- El tracking se detiene al entregar.

## Backups

- Existe backup diario.
- El backup tiene checksum.
- El backup queda registrado en `backup_jobs`.
- La retencion elimina archivos antiguos.
- Existe prueba de restauracion documentada.

# Riesgos y mitigaciones

| Riesgo | Impacto | Mitigacion |
|---|---|---|
| Servidor local falla | Alto | Backups diarios y copia externa |
| Kafka aumenta complejidad | Medio | Usarlo solo para eventos importantes |
| WebFlux mal usado con bloqueos | Alto | Evitar llamadas bloqueantes en el event loop |
| GPS consume bateria | Medio | Frecuencia variable y tracking solo activo |
| Reportes lentos | Medio | Indices, vistas y export futuro a BigQuery |
| Mala carga de carta | Medio | Modelo con variantes y modificadores |
| Perdida de datos por error humano | Alto | Auditoria y backups restaurables |

# Recomendacion final

Para Pikudo Chicken, la decision tecnica correcta es construir una **API unica, modular, multicapas y self-hosted**, usando PostgreSQL como fuente principal de verdad, Redis para velocidad operativa, Kafka para eventos, Flyway para migraciones y WebFlux para endpoints reactivos y tracking en tiempo real.

No se recomienda microservicios en la primera version. La complejidad operativa no compensa para un restaurante que ejecutara su propio sistema. Lo correcto es separar bien los modulos internamente y dejar puntos claros de extraccion futura.

El backend debe priorizar consistencia transaccional, backups, modelo de datos fuerte, seguridad, auditoria y capacidad de crecimiento vertical. Con este enfoque, Pikudo Chicken tendra una solucion profesional a medida, mientras Studios TKOH conserva la posibilidad de usar la experiencia obtenida para construir su SaaS corporativo en el futuro.

# Anexo A - Mapeo inicial de la carta de Pikudo Chicken

| Pagina de carta | Categoria detectada | Uso en el sistema |
|---|---|---|
| 1 | Resena historica y medios de pago | Landing, branding, metodos de pago |
| 2 | Pollos a la lena y combos | Categorias, productos, variantes y combos |
| 3 | Parrillas familiares | Productos tipo comida y combos familiares |
| 4 | Combos parrilleros | Combos con componentes y modificadores |
| 5 | Parrillas, BBQ y cortes | Productos con variantes por peso/porcion |
| 6 | Piqueos y adicionales | Productos adicionales y modificadores |
| 7 | Saltados, chaufas, carnes y pastas | Categorias de cocina criolla y platos variados |
| 8 | Sopas | Categoria independiente de comida |
| 9 | Ensaladas | Productos tipo comida ligera/adicional |
| 10 | Bebidas calientes, frias y frozen | Productos tipo bebida con variantes por tamano |

# Anexo B - Convencion de paquetes backend

```text
com.studiostkoh.pikudo
  config
  shared
    domain
    infrastructure
    security
    events
  identity
    api
    application
    domain
    infrastructure
  restaurant
  catalog
  pricing
  orders
  payments
  tables
  delivery
  tracking
  inventory
  notifications
  reports
  backups
  audit
```

# Anexo C - Convencion de eventos de dominio

```json
{
  "eventId": "uuid",
  "eventType": "OrderStatusChanged",
  "aggregateType": "Order",
  "aggregateId": "uuid",
  "occurredAt": "2026-07-09T18:30:00-05:00",
  "payload": {
    "orderId": "uuid",
    "fromStatus": "PREPARING",
    "toStatus": "READY",
    "changedBy": "uuid"
  }
}
```

# Referencias tecnicas

- Apache Kafka. Documentacion oficial. https://kafka.apache.org/documentation/
- Docker. Documentacion oficial de Docker Compose. https://docs.docker.com/compose/
- Firebase. Firebase Cloud Messaging. https://firebase.google.com/docs/cloud-messaging
- Flyway by Redgate. Documentacion de migraciones. https://documentation.red-gate.com/fd/migrations-271585107.html
- Google Maps Platform. Routes API. https://developers.google.com/maps/documentation/routes
- Next.js. Documentacion oficial. https://nextjs.org/docs
- PostgreSQL Global Development Group. PostgreSQL About. https://www.postgresql.org/about/
- PostGIS. Sitio oficial. https://postgis.net/
- Redis. Documentacion oficial. https://redis.io/docs/latest/
- Spring Framework. Spring WebFlux. https://docs.spring.io/spring-framework/reference/web/webflux.html
- Tauri. Create a Project. https://v2.tauri.app/start/create-project/
