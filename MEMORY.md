# MEMORY - Estado del Proyecto: Sintaxis Phone Store API

## Backend (Completado y Estable)
- Entidades creadas: `Producto` (abstracta), `Celular`, `Accesorio`, `Usuario`, `Venta`.
- Endpoints de productos funcionales: `/api/celulares` y `/api/accesorios` (GET público, POST/PUT/DELETE protegidos).
- Seguridad implementada con JWT y roles (ADMIN, EMPLEADO).
- Configuración de CORS y SecurityConfig ajustada para permitir archivos estáticos.
- 373 tests pasando correctamente (JUnit 5 + Mockito).

## Frontend (Completado y Estable)
- [x] Crear estructura base en `src/main/resources/static/` (index.html, styles.css, app.js).
- [x] Implementar diseño oscuro "Kyū" (Dark Mode con acentos cyan y rosa neón).
- [x] Consumo de la API: Se utiliza `Promise.all()` para traer datos de `/api/celulares` y `/api/accesorios` simultáneamente.
- [x] Pestañas (Tabs) para separar vistas de "Todos", "Celulares" y "Accesorios".
- [x] Carrito de compras funcional con persistencia en `localStorage`.
- [x] Botón de agregar actualizado a **"Agregar al carrito"**.
- [x] Buscador directo por nombre y marca.
- [x] Funcionalidad de Login conectada al endpoint `/api/auth/login`, guardado del token JWT en `localStorage`, botón de "Cerrar Sesión" (con llamada a `/api/auth/logout`), y botón condicional de "Panel de Administración" para roles ADMIN o EMPLEADO.
- [x] Implementación completa del Panel de Administración (modal con estilo Kyū Dark Mode) para la creación (POST), edición (PUT) y eliminación lógica (DELETE) de productos (celulares y accesorios) utilizando encabezados HTTP `Authorization: Bearer <token>`.
- [x] Implementación de la **Gestión de Usuarios y Roles**: Pestaña "Usuarios" dentro del Panel de Administración (protegida para administradores mediante `@PreAuthorize("hasRole('ADMIN')")`), visualización de usuarios registrados, y botón "+ Nuevo Usuario" para crear EMPLEADOS, CLIENTES o ADMINS.
- [x] Implementación del **Registro Público**: Enlace/botón "Crear cuenta" en el modal de login que permite registrarse como nuevo CLIENTE con creación automática de cuenta y perfil.

## Próximos Pasos (Pendientes)
- [ ] Ampliar reportes de ventas o historial de transacciones para el administrador en el frontend.

## Tareas completadas
- [x] Corrección de la renderización de URLs absolutas antiguas (`http://localhost:8080/api/productos/...`) en el frontend (`src/main/resources/static/index.html`) para transformarlas automáticamente a la ruta correcta (`/api/imagenes/...`), evitando errores 403 y fallos de sintaxis en el navegador.
- [x] Cambio de la ruta de imágenes en los servicios (`AccesorioService.java`, `CelularService.java`, `ProductoService.java`) y creación de `ImagenController.java` para exponer la ruta genérica `/api/imagenes/**`.
- [x] Configuración de seguridad en `SecurityConfig.java` para marcar `/api/imagenes/**` como pública (`permitAll()`).
- [x] Actualización de pruebas unitarias (`AccesorioServiceTest.java`) y verificación exitosa de todos los tests.
- [x] Incorporación del evento `onerror` en las etiquetas `<img>` de `src/main/resources/static/index.html` (tanto en la grilla principal, el carrito y el panel de administración) con un placeholder SVG embebido (`data:image/svg+xml;...`) para prevenir iconos de imagen rota cuando las URLs externas fallen o bloqueen solicitudes.
- [x] Verificación de la no existencia de llamadas a `/api/productos/` en el frontend, confirmando que las operaciones CRUD de productos utilizan correctamente `/api/celulares` y `/api/accesorios`.
- [x] Implementación completa del flujo de Autenticación y Login en el frontend con diseño Kyū Dark Mode, manejo de tokens JWT, almacenamiento en `localStorage`, control de cierre de sesión y acceso condicional para roles ADMIN/EMPLEADO.
- [x] Implementación completa del Panel de Administración interactivo en el frontend con diseño Kyū Dark Mode. Permite crear, editar y eliminar celulares y accesorios consumiendo los endpoints POST, PUT y DELETE de la API REST mediante token JWT almacenado en `localStorage`.
- [x] Implementación completa de la **Gestión de Usuarios y Roles** y el **Registro Público**, incluyendo la protección de seguridad en backend (`@PreAuthorize("hasRole('ADMIN')")`), endpoints en `AuthController` (`/api/auth/usuarios`), y modales dedicados en el frontend (`src/main/resources/static/index.html`) con diseño Kyū Dark Mode. Todos los tests automatizados (373) pasan con éxito.
