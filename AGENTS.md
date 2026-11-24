continua# Contexto del Proyecto: Sintaxis Phone Store API

## Stack Tecnológico (Backend)
- **Lenguaje:** Java 17
- **Framework:** Spring Boot 3.x
- **Seguridad:** Spring Security + JWT
- **Persistencia:** JPA / Hibernate
- **Base de Datos:** PostgreSQL (producción) / H2 (test)
- **Testing:** JUnit 5 + Mockito
- **Documentación:** Swagger / OpenAPI

## Estructura del Proyecto
- `/src/main/java/com/adrian/sintaxis/`: Código fuente Java.
- `/src/main/resources/static/`: Aquí se guarda el frontend (HTML, CSS, JS).
- `/docs/`: Documentación y Constitución.
- `/specs/`: Especificaciones y planes generados por IA.

## Endpoints Reales de la API
- **Autenticación:** `/api/auth/login`, `/api/auth/register`
- **Productos:** `/api/celulares`, `/api/accesorios` (GET público, POST/PUT/DELETE protegidos).
- **Clientes y Ventas:** Protegidos por roles de ADMIN y EMPLEADO.

## Reglas para el Frontend (Lo que vas a construir)
- **Stack Frontend:** HTML5, CSS3 y JavaScript Vanilla (puro). 
- **PROHIBIDO:** No usar React, Angular, Vue, Bootstrap, Tailwind ni ninguna librería externa. Todo debe ser CSS puro.
- **Diseño:** Tema oscuro (Dark Mode), moderno, minimalista y responsive. Inspirado en la app "Kyū".
- **Consumo de API:** Usar `fetch()` nativo para comunicarse con el backend en `http://localhost:8080`.
- **Persistencia Local:** El carrito de compras debe guardarse en `localStorage`.

## Reglas para la IA (Backend y Flujo de Trabajo)
- **Estado del Proyecto:** SIEMPRE debes leer el archivo `MEMORY.md` al iniciar una tarea para entender qué está hecho y qué falta. NO intentes volver a implementar funcionalidades que ya figuran como completadas.
- **Patrón de Diseño:** Siempre usar el patrón MVC (Controller -> Service -> Repository).
- **DTOs:** No mezclar Entidades con DTOs. El controlador siempre devuelve DTOs.
- **Seguridad y Encriptación:** Usar exclusivamente **BCrypt (`BCryptPasswordEncoder`)** para las contraseñas. Está prohibido cambiar el algoritmo de hashing o almacenar contraseñas en texto plano para evitar desajustes de permisos en ventas y clientes.
- **Documentación:** Documentar cada endpoint con Swagger/OpenAPI.
- **Testing:** Escribir tests unitarios con JUnit 5 y Mockito para la capa de Servicio.
- Actualización de Memoria: Cada vez que completes una tarea o implementes una nueva funcionalidad de forma exitosa, DEBES actualizar el archivo MEMORY.md marcando la tarea como completada y agregando un resumen de lo que hiciste. No se considera una tarea terminada hasta que el MEMORY.md esté actualizado.
- **Enfoque y Estándar Profesional (E-commerce / App de Escritorio):** Todas las características, flujos de usuario (como compras de invitados, manejo de carritos, modales, validaciones y gestión de inventario) y componentes deben implementarse bajo los estándares más altos de la industria para e-commerce y aplicaciones de escritorio profesionales (robustez, manejo elegante de excepciones, retroalimentación visual clara para el usuario, transacciones seguras y flujos sin fricción).
- **Protección contra Regresiones y Acoplamiento Crítico (Cero Rupturas Indirectas):** Está estrictamente prohibido introducir código, refactorizaciones o cambios en clases fuertemente acopladas o relacionadas bidireccionalmente (como `Usuario`, `Cliente`, `Venta`) que puedan romper indirectamente funcionalidades ya logradas. Antes de modificar métodos de eliminación o persistencia en cascada, se deben auditar todas las referencias cruzadas y optar preferiblemente por un diseño seguro (como borrado lógico o desvinculación explícita de referencias) para garantizar la integridad referencial y el historial del sistema.