# Especificación de Requerimientos: Separación de Catálogo (Celulares y Accesorios) y Pestaña de Clientes en Admin

## 1. Resumen Ejecutivo
Esta especificación detalla los cambios requeridos en el archivo frontend `src/main/resources/static/index.html` para cumplir con dos solicitudes clave:
1. **Pestañas dedicadas en el catálogo principal**: Asegurar que las pestañas "Celulares" y "Accesorios" presenten catálogos limpios y separados de forma precisa sin solapamientos confusos con la pestaña "Todos".
2. **Pestaña de "Clientes" en el panel de administración (`adminModal`)**: Integrar una nueva pestaña en el modal de administración que permita a los administradores y empleados visualizar, buscar y gestionar los clientes registrados consumiendo los endpoints de `/api/clientes`. Además, aplicar verificación de seguridad estricta para garantizar que las herramientas de administración estén ocultas para los clientes o usuarios no autorizados.

---

## 2. Requerimientos Funcionales

### 2.1 Pestañas del Catálogo Principal
- **Pestaña "Todos"**: Muestra tanto celulares como accesorios combinados.
- **Pestaña "Celulares"**: Filtra y muestra exclusivamente productos de la categoría celular (consumiendo `/api/celulares` o filtrando correctamente por tipo/endpoint).
- **Pestaña "Accesorios"**: Filtra y muestra exclusivamente productos de la categoría accesorio (consumiendo `/api/accesorios` o filtrando correctamente por tipo/endpoint).
- Las pestañas deben alternar su estilo activo con claridad visual (tema oscuro Kyū con acento cyan).

### 2.2 Pestaña de Clientes en el Panel de Administración (`adminModal`)
- Agregar un nuevo botón de pestaña en el menú de navegación del `adminModal`: `<button class="btn" id="admin-tab-clientes" ... onclick="cambiarAdminTab('clientes')">👥 Clientes</button>`.
- Crear la sección correspondiente `#adminClientesSection` en el HTML que contenga una tabla estructurada con columnas:
  - ID / Nombre (Nombre y Apellido)
  - Email y Teléfono
  - Dirección
  - Puntos Acumulados / Estado VIP
  - Estado (Activo / Inactivo)
  - Acciones (Editar / Ver / Eliminar o Agregar Puntos).
- Consumir el endpoint `GET /api/clientes` usando `fetch()` con el token JWT almacenado en `localStorage`.

### 2.3 Seguridad y Ocultamiento Frontend
- Verificar que las opciones de administración (`adminModal`, botón de panel admin, pestaña de clientes y de usuarios) se oculten estrictamente para usuarios con rol `CLIENTE` o cuando no haya sesión iniciada.
- Los clientes solo deben ver su propio perfil, catálogo y carrito.

---

## 3. Requerimientos No Funcionales
- **Stack Tecnológico**: HTML5, CSS3 Vanilla y JavaScript Vanilla (respetando la regla de oro: sin librerías externas ni frameworks de JS).
- **Diseño**: Mantener el diseño estético de tipo oscuro (Dark Mode) con paleta Kyū (`--bg-canvas`, `--surface`, `--accent-cyan`).
- **Manejo de Errores**: Manejar adecuadamente errores 401/403 (redirección o mensaje claro) y errores de red al listar clientes o productos.
