# Contexto del Proyecto: Sintaxis Phone Store API

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
- **Documentación:** Documentar cada endpoint con Swagger/OpenAPI.
- **Testing:** Escribir tests unitarios con JUnit 5 y Mockito para la capa de Servicio.
- Actualización de Memoria: Cada vez que completes una tarea o implementes una nueva funcionalidad de forma exitosa, DEBES actualizar el archivo MEMORY.md marcando la tarea como completada y agregando un resumen de lo que hiciste. No se considera una tarea terminada hasta que el MEMORY.md esté actualizado.