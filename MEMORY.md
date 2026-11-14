# MEMORY - Estado del Proyecto: Sintaxis Phone Store API

## Backend (Completado y Estable)
- Entidades creadas: `Producto` (abstracta), `Celular`, `Accesorio`, `Usuario`, `Venta`, `Subcategoria`.
- Relación `@ManyToOne` robusta implementada entre `Accesorio` y `Subcategoria` basada en ID (`subcategoriaId`), asegurando persistencia limpia y DTOs de respuesta con el nombre de subcategoría asociado.
- Endpoints funcionales: `/api/celulares`, `/api/accesorios`, `/api/subcategorias` (GET público, POST protegido para ADMIN/EMPLEADO).
- Seguridad implementada con JWT y roles (ADMIN, EMPLEADO).
- Configuración de CORS y SecurityConfig ajustada para permitir archivos estáticos.
- 372 tests pasando correctamente (JUnit 5 + Mockito).

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
- [x] Actualización de `JwtService.java` en el método `generarToken` para incluir la claim de rol `.claim("rol", userDetails.getAuthorities().iterator().next().getAuthority())`, asegurando que el token JWT transporte adecuadamente el rol del usuario para los endpoints protegidos (como ventas).
- [x] Corrección del error 403 (Forbidden) y `LazyInitializationException` en la pestaña de Ventas del Panel de Admin:
  - **T1 (`app.js`):** Verificación y garantía de que la función `cargarVentasAdmin()` envía correctamente el header `'Authorization': 'Bearer ' + token` en el `fetch('/api/ventas')`.
  - **T2 (`SecurityConfig.java`):** Configuración explícita de reglas de autorización para `/api/ventas/**` permitiendo acceso a `ROLE_ADMIN` y `ROLE_EMPLEADO` antes de `.anyRequest().authenticated()`.
  - **T3 (`JwtAuthFilter.java` & `UserDetailsServiceImpl.java`):** Asegurado que los roles de los usuarios autenticados vía JWT se carguen y lean correctamente con el prefijo `ROLE_` (`ROLE_ADMIN`, `ROLE_EMPLEADO`).
  - **T4 (`VentaRepository.java` & `VentaService.java`):** Implementación de la consulta con `@EntityGraph` (`findAllWithDetalles()`) para cargar el cliente y los detalles de la venta en una única consulta, evitando `LazyInitializationException` / `NullPointerException`.
  - **T5 (Testing):** Ejecución exitosa de la suite completa de tests con **372 tests pasando sin fallos (BUILD SUCCESS)**.
