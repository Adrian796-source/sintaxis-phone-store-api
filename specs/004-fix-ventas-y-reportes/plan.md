# Plan de Arquitectura y Cambios: Corrección de Ventas, Reportes y Seguridad (403 / LazyInitialization)

## 1. Objetivo y Diagnóstico del Problema
- **Error 403 Forbidden en Endpoints de Ventas y Reportes (`/api/ventas/**` y `/api/reportes/**`):**
  - Causa raíz: En `SecurityConfig.java`, los endpoints de ventas o reportes pueden no estar correctamente permitidos para los roles `ADMIN` y `EMPLEADO` mediante `@PreAuthorize` o reglas de seguridad en la cadena de filtros HTTP (`authorizeHttpRequests`).
- **LazyInitializationException / NullPointerException en Mapeo de DTOs y Detalles de Ventas:**
  - Causa raíz: Al consultar ventas y mapearlas a DTOs (`VentaResponseDTO` / `ReporteVentaDTO`), las colecciones lazy de `detalles` o la relación con `producto` dentro de `DetalleVenta` se evalúan fuera de una transacción activa (`@Transactional`), lanzando `LazyInitializationException`. Asimismo, si algún producto viene nulo o no se inicializa correctamente con JOIN FETCH en el repositorio, se produce un `NullPointerException`.
- **Regla de Seguridad Front-End:**
  - Garantizar que los componentes de administración en la UI (`src/main/resources/static/index.html` y `app.js`) estén ocultos para clientes y solo accesibles con roles `ADMIN` o `EMPLEADO`.

---

## 2. Arquitectura de Cambios Propuestos

### 2.1. Capa de Seguridad (`SecurityConfig.java`)
- Asegurar que todas las rutas bajo `/api/ventas/**` y `/api/reportes/**` requieran autenticación y rol `ADMIN` o `EMPLEADO`.
- Revisar configuración de CORS y CSRF (mantener stateless).

### 2.2. Capa de Persistencia (`VentaRepository.java`)
- Añadir métodos con `@Query` y `LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto` (o entidades específicas) para evitar consultas perezosas fuera del servicio.

### 2.3. Capa de Servicio (`VentaService.java` y Servicios de Reportes)
- Anotar explícitamente los métodos de lectura (`listarVentas`, `obtenerVentaPorId`, reportes) con `@Transactional(readOnly = true)`.
- Asegurar mapeo seguro de entidades a DTOs verificando la existencia de productos y clientes para evitar `NullPointerException`.

---

## 3. Estructura de Ficheros de Especificación
- `specs/004-fix-ventas-y-reportes/plan.md`
- `specs/004-fix-ventas-y-reportes/tasks.md`
