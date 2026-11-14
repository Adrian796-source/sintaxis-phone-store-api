# Tareas de Implementación: Corrección de Ventas, Reportes y Seguridad (403 / LazyInitialization)

- [x] **T1: Verificación y Actualización de Seguridad en `SecurityConfig.java`**
  - **Descripción:** Revisar y configurar las reglas de autorización para `/api/ventas/**` y `/api/reportes/**`, asegurando que requieran autenticación y roles `ADMIN` o `EMPLEADO`.
  - **Verificación:** Test unitario en `SecurityConfigTest` o integración asegurando respuesta correcta para roles permitidos y prohibidos (403).

- [x] **T2: Optimización de Consultas en `VentaRepository.java`**
  - **Descripción:** Implementar consultas con `JOIN FETCH` (vía `@EntityGraph`) para las relaciones `detalles`, `cliente` y `detalles.producto` en `VentaRepository` para prevenir `LazyInitializationException`.
  - **Verificación:** Test en `VentaRepositoryTest` (y suite completa de 371 tests) comprobando que las ventas se cargan con sus detalles y productos sin excepción.

- [ ] **T3: Refactorización y Transaccionalidad en `VentaService.java`**
  - **Descripción:** Asegurar `@Transactional(readOnly = true)` en los métodos de consulta de ventas y reportes. Añadir validaciones defensivas contra `NullPointerException` durante el mapeo de DTOs.
  - **Verificación:** Tests unitarios en `VentaServiceTest` simulando escenarios con detalles y productos.

- [ ] **T4: Verificación de Seguridad Visual en Frontend (`index.html` y `app.js`)**
  - **Descripción:** Comprobar y reforzar las reglas de visibilidad para que los componentes de administración y ventas estén ocultos para clientes rasos, cumpliendo con la Constitución del proyecto.
  - **Verificación:** Inspección visual o test estricto en los scripts de renderizado del frontend.

- [ ] **T5: Ejecución de Suite de Pruebas Completa (`mvn test`)**
  - **Descripción:** Ejecutar `mvn test` para validar que todas las pruebas unitarias y de integración pasen con éxito (`BUILD SUCCESS`).
  - **Verificación:** Salida exitosa de Maven sin fallos.
