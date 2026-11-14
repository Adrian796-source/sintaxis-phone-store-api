# Especificación Técnica: Corrección de Ventas, Seguridad y Reportes (004-fix-ventas-y-reportes)

## 1. Contexto y Objetivos
Esta especificación aborda tres problemas críticos detectados en el módulo de ventas, seguridad y reportes de la API de Sintaxis Phone Store:
1. **Error 403 Forbidden en Endpoints de Ventas:** El endpoint `GET /api/ventas` (y la familia `/api/ventas/**`) devuelve un error `403 Forbidden` incluso al autenticarse con un token válido con rol `ROLE_ADMIN` o `ADMIN`. Se debe asegurar la correcta concordancia entre los roles permitidos en `SecurityConfig` y los otorgados por `UserDetailsServiceImpl`.
2. **LazyInitializationException / NullPointerException en Mapeo de Ventas:** Al invocar métodos de lectura como `listarTodos`, `buscarPorId`, etc., el servicio intenta acceder a la colección lazy `detalles` y a las relaciones `producto` dentro de `toDTO` y `toDetalleDTO` fuera de una transacción activa, provocando fallos de persistencia. Se solucionará añadiendo `@Transactional` o `@Transactional(readOnly = true)` en los métodos de la capa de servicio correspondientes o mediante consultas con JOIN FETCH optimizadas en `VentaRepository`.
3. **Requisitos de Reportes de Ventas:** Consolidar y documentar formalmente los requerimientos funcionales para los reportes de ventas ya implementados en la API (`total-recaudado`, `por-estado`, `top-clientes`), asegurando su correcta accesibilidad para los roles `ADMIN` y `EMPLEADO` bajo el diseño "Kyū Dark Mode" y la constitución del proyecto.

---

## 2. Requisitos Funcionales (RF)

### RF-01: Control de Acceso y Seguridad en Ventas
- **RF-01.1:** El endpoint `GET /api/ventas` (página de ventas) y sus subrecursos de gestión deben permitir el acceso a usuarios autenticados con rol `ADMIN` o `EMPLEADO`.
- **RF-01.2:** Se deben unificar los prefijos de autoridad en `SecurityConfig` para soportar tanto `ROLE_ADMIN`/`ROLE_EMPLEADO` como `ADMIN`/`EMPLEADO` (según convención de Spring Security y la estructura del token JWT).
- **RF-01.3:** El endpoint `/api/ventas/mis-ventas` permanecerá exclusivamente accesible para el rol `CLIENTE`.

### RF-02: Persistencia y Transaccionalidad (Prevención de LazyInitializationException)
- **RF-02.1:** Todos los métodos de lectura y escritura en `VentaService` que accedan a colecciones perezosas (`detalles`, `producto`, `cliente`) deben estar anotados explícitamente con `@Transactional` o `@Transactional(readOnly = true)`.
- **RF-02.2:** Opcionalmente, se pueden optimizar las consultas en `VentaRepository` usando `JOIN FETCH v.detalles d JOIN FETCH d.producto` para evitar problemas N+1 y garantizar la disponibilidad de los datos en memoria durante el mapeo DTO.

### RF-03: Consolidación de Reportes de Ventas
- **RF-03.1:** El sistema debe proveer un reporte de **Total Recaudado por Período** (`/api/ventas/reportes/total-recaudado`) filtrando por ventas en estado `Pagada` entre dos marcas de tiempo (ISO-8601).
- **RF-03.2:** El sistema debe proveer un reporte de **Ventas Agrupadas por Estado** (`/api/ventas/reportes/por-estado`) con contadores exactos para: `Pendiente`, `Pagada`, `Entregada`, `Cancelada`.
- **RF-03.3:** El sistema debe proveer un reporte de **Top Clientes** (`/api/ventas/reportes/top-clientes`) ordenados por el volumen total de compras efectivas (`Pagada`).
- **RF-03.4:** Todos los reportes requerirán obligatoriamente autenticación y roles `ADMIN` o `EMPLEADO`.

---

## 3. Criterios de Aceptación (CA)

1. **CA-01 (Seguridad Ventas):** Una petición `GET /api/ventas` realizada con un token JWT válido de un usuario con rol `ADMIN` o `EMPLEADO` responde con código `200 OK` y la paginación de ventas (nunca un error 403).
2. **CA-02 (Transaccionalidad):** Las llamadas a `buscarPorId`, `listarTodos`, `buscarPorCliente`, etc., completan su ejecución sin lanzar `LazyInitializationException` ni `NullPointerException`.
3. **CA-03 (Reportes):** Los endpoints de reportes bajo `/api/ventas/reportes/**` retornan datos correctos en formato JSON y están protegidos contra accesos no autorizados.
4. **CA-04 (Tests Unitarios):** Todos los tests existentes en `VentaServiceTest`, `VentaControllerTest` y `SecurityConfigTest` pasan de forma exitosa (`BUILD SUCCESS` con `mvn test`).
