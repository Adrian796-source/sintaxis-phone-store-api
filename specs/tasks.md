# Lista de Tareas (Tasks): Separación de Catálogo y Pestaña de Clientes en Admin

- [ ] T1: Actualizar la especificación y el plan de trabajo en `/specs/spec.md` y `/specs/plan.md` (Completado).
- [ ] T2: Refactorizar las pestañas del catálogo principal en `src/main/resources/static/index.html` para separar limpiamente "Celulares" y "Accesorios" utilizando sus endpoints específicos (`/api/celulares` y `/api/accesorios`).
- [ ] T3: Añadir el botón de pestaña "👥 Clientes" y la sección `#adminClientesSection` dentro del `adminModal` en `src/main/resources/static/index.html`.
- [ ] T4: Implementar la función `cambiarAdminTab('clientes')` y la función `cargarClientesAdmin()` en JavaScript para consumir `GET /api/clientes` con autenticación JWT.
- [ ] T5: Implementar la verificación de seguridad en el frontend asegurando que los componentes de administración y la pestaña de clientes estén ocultos para usuarios con rol `CLIENTE` o sin sesión.
- [ ] T6: Ejecutar los tests de backend con `./mvnw test` para verificar que la API y los controladores de clientes y productos sigan respondiendo correctamente.
- [ ] T7: Actualizar el archivo `MEMORY.md` con el resumen de la implementación completada.
