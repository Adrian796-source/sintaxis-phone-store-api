# Constitución del Proyecto: Sintaxis Phone Store API

Este documento define los principios innegociables y las reglas arquitectónicas del proyecto. Cualquier agente de IA (OpenCode) debe leer y respetar estrictamente estas normas antes de escribir código.

## 1. Arquitectura Backend
- Uso estricto del patrón **MVC** (Controller -> Service -> Repository) con Spring Boot 3.x.
- **No se permite** lógica de negocio en los controladores; toda la lógica debe residir en la capa de Servicio.
- Uso de DTOs (`RequestDTO` y `ResponseDTO`) para la transferencia de datos. **Nunca** exponer las Entidades de la base de datos directamente.

## 2. Seguridad y Encriptación
- Todos los endpoints de escritura (POST, PUT, DELETE) deben estar protegidos con JWT y roles (`ADMIN`, `EMPLEADO`).
- Solo los GET de productos (catálogo) son públicos.
- **Cifrado de Credenciales:** El único método de encriptación permitido y soportado para las contraseñas de los usuarios es **BCrypt (`BCryptPasswordEncoder`)** a través de Spring Security. Está prohibido el uso de texto plano o algoritmos no seguros.
- El backend tiene la última palabra: Si un usuario no tiene el rol adecuado, la petición debe ser rechazada con un error `403 Forbidden` usando `@PreAuthorize`.

## 3. Base de Datos
- Uso de PostgreSQL en producción y desarrollo.
- Las entidades deben usar JPA/Hibernate.
- Las relaciones deben ser explícitas (`@OneToMany`, `@ManyToOne`) y documentadas.

## 4. Testing
- No se acepta código sin tests. Se debe usar JUnit 5 y Mockito para la capa de servicios y controladores.
- Antes de dar una tarea por terminada, el agente debe ejecutar `mvn test` y verificar que todos los tests sigan pasando (`BUILD SUCCESS`).

## 5. Frontend y Diseño Visual
- Interfaz en **HTML, CSS y JS Vanilla**. Está prohibido usar frameworks (React, Angular) o librerías de CSS (Bootstrap, Tailwind).
- Todo el frontend debe residir en `src/main/resources/static/`.
- El diseño debe seguir estrictamente el estilo **"Kyū Dark Mode"** (fondo oscuro profundo, acentos neón en cyan `#38BDF8` y rosa `#F43F5E`, responsive para móviles).

## 6. Uso de IA y Flujo de Trabajo (SDD)
- Todo desarrollo debe seguir el flujo **SDD** (Spec -> Plan -> Tasks -> Implement). La IA no escribe código sin una especificación aprobada.
- Al terminar una tarea, la IA debe actualizar el archivo `MEMORY.md` marcando lo completado y lo pendiente.

## 7. Integridad Referencial y Protección contra Regresiones (Cero Rupturas Indirectas)
- Está estrictamente prohibido introducir código, refactorizaciones o cambios en clases fuertemente acopladas o relacionadas bidireccionalmente (como `Usuario`, `Cliente`, `Venta`) que puedan romper indirectamente funcionalidades ya logradas.
- Antes de modificar métodos críticos de eliminación o persistencia en cascada, se deben auditar todas las referencias cruzadas y optar por diseños seguros (como borrado lógico o desvinculación explícita de referencias) para garantizar la integridad referencial, el historial de ventas y la estabilidad del sistema.

## 8. Seguridad Visual (Frontend)
- La interfaz de usuario **NUNCA** debe mostrar componentes de administración (botones de panel, formularios de creación/edición) a menos que el `localStorage` contenga un token válido y el rol sea explícitamente `ADMIN` o `EMPLEADO`.
- Las validaciones de visibilidad en JavaScript deben ser estrictas: `if (rol !== 'ADMIN' && rol !== 'EMPLEADO') { ocultar(); return; }`.
- **Prohibido** usar variables intermedias que puedan fallar o quedar indefinidas al evaluar los roles (esto evita que el botón de "Panel Admin" se muestre por error a los clientes).