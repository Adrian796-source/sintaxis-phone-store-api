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
- [x] Limpieza de `index.html`: Eliminación completa del bloque `<script>` duplicado y viejo (que empezaba en la línea 565).
- [x] Referencia única: Asegurado que al final de `index.html` (antes de `</body>`) solo quede `<script src="app.js" defer></script>`.
- [x] Verificación de `app.js`: Confirmado que `celularesGlobal` y `accesoriosGlobal` están declarados correctamente una única vez al principio con `let`.
- [x] Verificación y pruebas: Todos los contadores de "Celulares" y "Accesorios" y las tarjetas de productos se actualizan y renderizan correctamente.
- [x] Ejecución exitosa de los 371 tests de la API (BUILD SUCCESS).
