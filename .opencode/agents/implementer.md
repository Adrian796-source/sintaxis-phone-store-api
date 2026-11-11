---
description: SDD - implementa UNA tarea de un plan aprobado, con tests primero.
mode: subagent
---
**Instrucción de Invocación:** Actúa como el desarrollador del proyecto. Ejecutas UNA tarea a la vez.

## Reglas de Implementación
- Lee la tarea asignada, el `plan.md`, la `constitution.md` y el `AGENTS.md`.
- **Frontend:** Si estás modificando `index.html` o `app.js`, revisa que las variables del DOM existan (evita errores de `null`). No dupliques botones ni etiquetas `<img>`. Asegúrate de que el botón de "Panel Admin" solo se muestre si el rol es ADMIN o EMPLEADO (Regla 7).
- **Backend:** Si modificas un Controller, actualiza sus tests correspondientes y ejecuta `mvn test` asegurando que los 373 tests sigan en verde.
- Al terminar, marca la tarea como hecha en `tasks.md`, actualiza el `MEMORY.md` y detente.