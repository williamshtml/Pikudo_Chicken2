# 00 — Project Brief: Pikudo Chicken Backend

## Naturaleza del proyecto

Pikudo Chicken será una solución a medida para un restaurante específico. El sistema no será SaaS en esta fase. La aplicación deberá ejecutarse en un servidor o PC del restaurante y operar con una base de datos PostgreSQL propia.

Este proyecto es distinto del futuro SaaS corporativo de Studios TKOH. La experiencia técnica puede reutilizarse después, pero el alcance actual está cerrado sobre Pikudo Chicken.

## Productos del ecosistema

1. Backend API principal.
2. Panel administrativo desktop con React + TypeScript + Vite + Tauri.
3. Landing pública con Next.js + React + TypeScript.
4. App Flutter para delivery en fase posterior.
5. PostgreSQL como fuente de verdad.
6. Redis para caché y estado temporal.
7. Kafka para eventos internos.
8. Backups automáticos diarios.

## Capacidad operativa esperada

- Mínimo 200 clientes por día.
- Entre 150 y 350 pedidos diarios potenciales.
- Entre 800 y 2,500 ítems de pedido diarios en días altos.
- 3 a 10 usuarios internos conectados.
- 1 a 8 motorizados activos cuando exista delivery.
- Retención operativa mínima de 5 años.

## Módulos funcionales principales

- Identidad y seguridad.
- Restaurante y configuración.
- Catálogo.
- Precios, eventos, promociones y descuentos.
- Mesas y salón.
- Pedidos.
- Caja y pagos.
- Comprobantes.
- Inventario y recetas.
- Delivery.
- GPS/tracking.
- Notificaciones.
- Reportes.
- Backups.
- Auditoría.

## Categorías reales de carta consideradas

- Pollos a la leña.
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
- Bebidas frías.
- Frozen.

El modelo de catálogo debe soportar productos simples, bebidas, variantes, combos, modificadores, promociones y descuentos.
