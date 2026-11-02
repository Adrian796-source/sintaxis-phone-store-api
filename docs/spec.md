# Especificación de Diseño Frontend: Kyū Dark Mode

## 1. Contexto y Propósito
Interfaz visual de alta gama para la API de productos y gestión de inventario de `Sintaxis`, adaptada bajo un concepto de **Dark Mode inmersivo inspirado en la app Kyū**, caracterizado por superficies profundamente oscuras, acentos neón vibrantes, tipografía ultra-limpia y micro-interacciones pulidas.

## 2. Sistema de Diseño & Tokens (Kyū Dark Theme)
- **Fondo Canvas Principal:** `#090A0F` (Negro abisal profundo)
- **Superficies / Tarjetas:** `#12141C` con bordes sutiles en `#1F2430`
- **Acento Principal (Neón Cyan/Electric):** `#38BDF8` (Cyan vibrante para elementos interactivos principales y destacados)
- **Acento Secundario / Alerta (Neon Magenta/Crimson):** `#F43F5E` (Rosa neón / Coral para advertencias de stock crítico y estados de alerta)
- **Texto Principal:** `#F3F4F6` (Blanco suave)
- **Texto Secundario:** `#9CA3AF` (Gris frío)
- **Tipografía:** *Inter* para interfaz general, *JetBrains Mono* para códigos, precios y métricas de stock.

## 3. Estructura y Componentes de la Interfaz
- **Header Minimalista con Glow:** Título de la aplicación con viñeta neón y botón de recarga con transición suave.
- **Barra de Métricas (KPI Cards):** Tarjetas de resumen con bordes sutiles y valores numéricos resaltados en tipografía monoespaciada con sutil resplandor.
- **Barra de Herramientas Operativa:** Campo de búsqueda con efecto focus en cyan neón y selector de filtro de estado.
- **Tabla de Inventario Avanzada:** Filas con hover reactivo, avatares de producto con bordes iluminados, etiquetas de estado y stock con códigos de color neón.
- **Integración con Endpoints:** Conexión directa asíncrona con `/api/productos`.
