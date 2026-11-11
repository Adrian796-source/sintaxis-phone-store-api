# Plan de Implementación: Separación de Catálogo y Pestaña de Clientes en Admin

## Fases de Implementación

### Fase 1: Análisis y Refactorización del Catálogo Principal (`index.html`)
- **Objetivo**: Revisar las funciones `cambiarTab(tipo)` y `cargarProductos()` para asegurar la separación limpia de las pestañas "Celulares" y "Accesorios".
- **Tareas**:
  - Asegurar que `cambiarTab('celulares')` cargue y renderice exclusivamente la lista obtenida de `/api/celulares`.
  - Asegurar que `cambiarTab('accesorios')` cargue y renderice exclusivamente la lista obtenida de `/api/accesorios`.
  - Asegurar que `cambiarTab('todos')` fusione y muestre ambos catálogos correctamente.

### Fase 2: Integración de la Pestaña de Clientes en el Panel de Administración
- **Objetivo**: Añadir la pestaña de clientes en el `adminModal` y conectar con `/api/clientes`.
- **Tareas**:
  - Modificar la barra de pestañas del `adminModal` para incluir el botón `#admin-tab-clientes`.
  - Agregar la sección HTML `#adminClientesSection` con la estructura de tabla para listar los clientes.
  - Implementar la función JavaScript `cambiarAdminTab('clientes')` que oculte las demás secciones (`adminProductsSection`, `adminUsersSection`, `adminVentasSection`) y muestre la sección de clientes.
  - Implementar la función `cargarClientesAdmin()` que realice una petición `GET /api/clientes` con el token JWT de autorización.
  - Añadir soporte para búsqueda de clientes en el panel (`filtrarClientesAdmin()`).

### Fase 3: Verificación de Seguridad y Ocultamiento Frontend
- **Objetivo**: Asegurar que los componentes de administración estén ocultos para los clientes.
- **Tareas**:
  - Revisar la función de renderizado de botones de autenticación y rol en la cabecera (`actualizarUIAutenticacion`).
  - Asegurar que el botón del Panel de Administración y el acceso al modal solo aparezcan si el usuario autenticado tiene rol `ADMIN` o `EMPLEADO`.

### Fase 4: Pruebas y Validación Final
- **Objetivo**: Verificar el correcto funcionamiento mediante tests unitarios de controlador/servicio existentes y pruebas de integración en el navegador (si aplica).
