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
- [x] **Corrección en el Registro de Invitados Post-Compra:**
  - Se ajustó el envío del rol en `modal_pago.js` de `"ROLE_CLIENTE"` a `"CLIENTE"` para coincidir exactamente con el enum de roles aceptados por el backend.
  - Se fortaleció el backend (`AuthService.java`) para que acepte tanto `CLIENTE` como `ROLE_CLIENTE` de manera robusta, evitando errores de `RolInvalidoException` (400 Bad Request) tras el registro de un cliente invitado.
