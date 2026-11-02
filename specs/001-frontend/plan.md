# Plan de Implementación: Interfaz Frontend Kyū Dark Mode

## 1. Enfoque Técnico
- Utilizar una arquitectura basada en un archivo estático autónomo servido por Spring Boot (`src/main/resources/static/index.html`).
- Estilos CSS nativos avanzados utilizando variables CSS (`:root`) para cumplir con el sistema de diseño Kyū Dark Mode sin dependencias externas pesadas.
- JavaScript Vanilla asíncrono (`fetch`) para conectar directamente con los endpoints de la API de Spring Boot.

## 2. Pasos de Ejecución
1. Crear la especificación de requerimientos (`specs/001-frontend/spec.md`).
2. Diseñar la estructura de ficheros y componentes visuales en el plan (`specs/001-frontend/plan.md`).
3. Definir las tareas detalladas y marcar la implementación como completada (`specs/001-frontend/tasks.md`).
4. Implementar el código en `src/main/resources/static/index.html`.
