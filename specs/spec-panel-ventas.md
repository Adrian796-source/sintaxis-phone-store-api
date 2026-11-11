# Especificación: Panel de Ventas e Historial de Transacciones para Administradores y Empleados

## 1. Objetivo
Implementar la visualización del historial de ventas y reportes de ingresos totales dentro del panel de administración del frontend (`src/main/resources/static/index.html`), permitiendo a los usuarios con rol `ADMIN` o `EMPLEADO` supervisar las transacciones de la tienda.

## 2. Requerimientos Funcionales
- **Pestaña "Ventas" en el Panel de Administración:** Añadir una nueva pestaña en el modal de administración existente, junto a "Productos" y "Usuarios".
- **Tabla de Historial de Ventas:** Mostrar una tabla con columnas:
  - ID de Venta (`id`)
  - Fecha (`fecha`)
  - Cliente (`clienteNombre` o email)
  - Total (`total`)
  - Estado (`estado`: Pendiente, Pagada, Entregada, Cancelada) con distintivos visuales (badges).
- **Resumen de Ingresos:** Tarjeta de resumen superior que muestre el total general de ingresos o recaudación (filtrado por ventas pagadas / completadas).
- **Control de Acceso:** Accesible únicamente para usuarios autenticados con rol `ADMIN` o `EMPLEADO`.
- **Consumo de API:** Consumir los endpoints existentes en `/api/ventas` (ej. GET `/api/ventas` para listar, GET `/api/ventas/reportes/total-recaudado` o cálculo directo, y PATCH `/api/ventas/{id}/estado` para cambiar el estado si es necesario).

## 3. Requerimientos de Diseño (Kyū Dark Mode)
- Estética minimalista oscura con fondos `#0f1015`, tarjetas `#161821`, bordes sutiles y tipografía moderna.
- Acentos en cian neón (`#00f2fe`) y rosa/violeta neón (`#4facfe` / `#ff007f`).
- Tablas con cabeceras oscuras, filas alternas y badges de estado con colores distintivos (verde para Pagada/Entregada, amarillo para Pendiente, rojo para Cancelada).
- Completamente responsivo.
