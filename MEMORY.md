# MEMORY - Estado del Proyecto: Sintaxis Phone Store API

## Backend (Completado y Estable)
- Entidades creadas: `Producto` (abstracta), `Celular`, `Accesorio`, `Usuario`, `Venta`, `Subcategoria`.
- Relación `@ManyToOne` robusta implementada entre `Accesorio` y `Subcategoria` basada en ID (`subcategoriaId`), asegurando persistencia limpia y DTOs de respuesta con el nombre de subcategoría asociado.
- Endpoints funcionales: `/api/celulares`, `/api/accesorios`, `/api/subcategorias` (GET público, POST protegido para ADMIN/EMPLEADO).
- Seguridad implementada con JWT y roles (ADMIN, EMPLEADO).
- Configuración de CORS y SecurityConfig ajustada para permitir archivos estáticos.
- 373 tests pasando correctamente (JUnit 5 + Mockito).

## Frontend (Completado y Actualizado)
- [x] Estructura base en `src/main/resources/static/index.html` con diseño oscuro "Kyū" (Dark Mode con acentos cyan y rosa neón).
- [x] Consumo de API con `Promise.all()` para `/api/celulares` y `/api/accesorios`.
- [x] Pestañas dedicadas en el catálogo principal para **"Todos"**, **"Celulares"** y **"Accesorios"**.
- [x] Subcategorías dinámicas para **Accesorios** ("Todas", "Fundas", "Templados", "Cargadores", "Auriculares", "Cables") con filtrado en cliente sin peticiones extra al backend.
- [x] Carrito de compras persistente en `localStorage`.
- [x] Autenticación JWT y ocultamiento estricto del botón de Panel Admin para clientes / usuarios no logueados (Regla 7 de la Constitución).
- [x] **Panel de Administración avanzado:** Pestañas para Productos (Todos, Celulares, Accesorios), Usuarios, Ventas y Clientes.
- [x] **Gestión Dinámica de Subcategorías y Relación por ID:** Selector de subcategoría en el formulario de accesorios configurado con el ID (`value="${idSub}"`) y envío correcto de `subcategoriaId` en los payloads de guardado/actualización de accesorios, resolviendo definitivamente el problema del 'N/A' en la tabla.

## Tareas Completadas Recientes
- [x] **Botón de Agregar Stock en Panel Admin:** Se agregó el botón rápido `📦 + Stock` en cada fila de producto (tanto celulares como accesorios) dentro del panel de administración. Al pulsarlo, abre un modal intuitivo para ingresar la cantidad a sumar al stock actual y actualiza dinámicamente el producto mediante la API REST segura con autenticación JWT.
- [x] **Alineación Correcta de Columnas en la Tabla de Clientes (Frontend):** Se reestructuró la tabla de Clientes en `src/main/resources/static/index.html` y `src/main/resources/static/app.js` separando la información en **7 columnas exactas**: `ID / Nombre`, `Email`, `Teléfono`, `Dirección`, `Estado`, `Puntos` y `Acciones`. Ahora los puntos y los botones de acción (Editar/Eliminar) tienen sus columnas propias e independientes.
- [x] **Gestión de Borrado Lógico de Clientes:** Actualización de `ClienteRepository` con `findByActivoTrue()` y `ClienteService.listarTodos()` para filtrar únicamente los clientes activos.
- [x] **Suite de Tests Validada:** Todos los tests de controladores y servicios pasan exitosamente (`BUILD SUCCESS`).
