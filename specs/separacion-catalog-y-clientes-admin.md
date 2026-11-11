# Especificación Funcional y Técnica (Spec)
## Separación de Celulares y Accesorios en el Catálogo y Pestaña de Clientes en el Panel de Administración

### 1. Resumen Ejecutivo
El objetivo de esta especificación es actualizar la interfaz de usuario frontend (`src/main/resources/static/index.html`) para:
1. Reestructurar las pestañas (tabs) del catálogo principal, garantizando una separación clara y directa entre **Celulares** y **Accesorios** (además de la vista "Todos").
2. Incorporar una nueva pestaña **"Clientes"** dentro del Panel de Administración (`adminModal`), la cual permitirá listar, buscar y visualizar la información de los clientes registrados en el sistema consumiendo el endpoint correspondiente de la API REST (`/api/clientes`).

---

### 2. Requisitos Funcionales

#### A. Catálogo Principal (Frontend)
- **Pestañas disponibles:**
  - `Todos`: Muestra la lista combinada de celulares y accesorios activos/disponibles.
  - `Celulares`: Filtra y renderiza exclusivamente los productos de tipo celular (`celularesGlobal`).
  - `Accesorios`: Filtra y renderiza exclusivamente los productos de tipo accesorio (`accesoriosGlobal`).
- **Comportamiento visual:** Cambio dinámico de estado activo en los botones de pestañas siguiendo el diseño Kyū Dark Mode (`--accent-cyan`).

#### B. Panel de Administración (Admin Modal)
- **Nuevas Pestañas de Administración:**
  - `Todos` (Productos)
  - `Celulares` (Productos)
  - `Accesorios` (Productos)
  - `Usuarios` (Gestión de usuarios del sistema)
  - `Clientes` (Nuevo: Listado y gestión de clientes registrados vía `/api/clientes`).
- **Pestaña Clientes:**
  - Al hacer clic en la pestaña "Clientes" dentro del panel admin, se ocultan las secciones de productos y usuarios, y se muestra la tabla de clientes.
  - Se realiza una petición HTTP `GET` a `/api/clientes` incluyendo el token JWT (`Authorization: Bearer <token>`) almacenado en `localStorage`.
  - Se renderiza una tabla con columnas claras: ID, Nombre y Apellido, Email, Teléfono, Dirección y Acciones.
  - Control de errores y seguridad: Si la sesión expira o el usuario no tiene permisos (`401`/`403`), se redirige al login con su respectiva alerta.

---

### 3. Plan de Implementación

1. **Fase 1: Catálogo Principal (Pestañas de Celulares y Accesorios)**
   - Revisar la función `cambiarTab(tab)` en `src/main/resources/static/index.html`.
   - Asegurar que los botones en el HTML correspondan exactamente a los identificadores `todos`, `celulares` y `accesorios`.

2. **Fase 2: Pestaña de Clientes en el Panel de Administración**
   - Agregar el botón de pestaña `Clientes` en el menú de navegación superior del `adminModal`.
   - Crear el contenedor HTML `<div id="adminClientesSection">` con su respectiva tabla (`adminClienteTableBody`).
   - Implementar las funciones JavaScript:
     - `cambiarAdminTab('clientes')` para alternar la visibilidad de las secciones.
     - `cargarClientesAdmin()` para consumir el endpoint protegido `/api/clientes`.
     - `renderizarTablaClientes(clientes)` para formatear y mostrar los datos en la tabla respetando el diseño Kyū Dark Mode.

3. **Fase 3: Verificación y Pruebas**
   - Ejecutar `mvn test` para garantizar que todos los tests automatizados del backend sigan pasando exitosamente.
   - Probar de forma interactiva en el navegador que el filtrado del catálogo funcione y que la nueva pestaña de Clientes cargue correctamente para roles `ADMIN` y `EMPLEADO`.

---

### 4. Tareas (Tasks)
- [ ] **Tarea 1:** Actualizar el HTML del catálogo principal en `src/main/resources/static/index.html` para asegurar la correcta separación y funcionamiento de las pestañas de Celulares y Accesorios.
- [ ] **Tarea 2:** Agregar el botón de la pestaña "Clientes" y su sección de tabla correspondiente en el HTML del Panel de Administración (`adminModal`).
- [ ] **Tarea 3:** Implementar las funciones JavaScript `cargarClientesAdmin()` y `renderizarTablaClientes()` para consumir el endpoint `/api/clientes` con autenticación JWT.
- [ ] **Tarea 4:** Actualizar la lógica de `cambiarAdminTab()` para gestionar la visibilidad de la sección de clientes.
- [ ] **Tarea 5:** Ejecutar `mvn test` y verificar el correcto funcionamiento general del sistema.
- [ ] **Tarea 6:** Actualizar el archivo `MEMORY.md` con el resumen de la nueva funcionalidad implementada.
