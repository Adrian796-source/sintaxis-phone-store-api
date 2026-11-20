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
- [x] **Corrección del Cierre del Modal de Historial de Compras ("Mis Compras"):** Se añadió la función global `toggleMisComprasModal(abrir)` en `src/main/resources/static/app.js` para resolver el error de referencia en consola y permitir el cierre fluido y correcto del modal de historial de órdenes del cliente.
- [x] **Validación Estricta de Stock y Control de Agotados en Carrito y Catálogo:** 
  - Se implementó la validación en tiempo real al añadir productos al carrito y al modificar cantidades (`cambiarCantidad`), mostrando notificaciones Toast de advertencia si se intenta superar el stock disponible.
  - Las tarjetas de producto en el catálogo ahora detectan automáticamente si un producto tiene stock `0`, deshabilitando el botón de compra con una etiqueta visual de **"AGOTADO"** en rojo neón.
  - Al procesar la compra exitosamente, el carrito se vacía y el catálogo actualiza instantáneamente los stocks y estados.
- [x] **Alineación Correcta de Columnas en la Tabla de Clientes (Frontend):** Se reestructuró la tabla de Clientes en `src/main/resources/static/index.html` y `src/main/resources/static/app.js` separando la información en **7 columnas exactas**: `ID / Nombre`, `Email`, `Teléfono`, `Dirección`, `Estado`, `Puntos` y `Acciones`. Ahora los puntos y los botones de acción (Editar/Eliminar) tienen sus columnas propias e independientes.
- [x] **Gestión de Borrado Lógico de Clientes:** Actualización de `ClienteRepository` con `findByActivoTrue()` y `ClienteService.listarTodos()` para filtrar únicamente los clientes activos.
- [x] **Historial de Compras del Cliente en Frontend:** Se implementó el botón dinámico **"📦 Mis Compras"** en la barra de navegación superior (visible únicamente para usuarios con sesión iniciada que no son administradores/empleados). Al pulsarlo, se abre un modal flotante con estilo Kyū que consume de forma segura el endpoint protegido `/api/ventas/mis-ventas` (vía token JWT), listando todas las órdenes del cliente con sus fechas, estados, métodos de pago, totales y desglose completo de productos adquiridos.
- [x] **Sistema de Notificaciones Toast (Kyū Dark Mode):** Se reemplazaron las alertas nativas (`alert()`) por un sistema moderno de notificaciones flotantes animadas (`mostrarToast`) con iconos de éxito, error e información, elevando la experiencia de usuario a estándares de producción.
