# Especificación de Requerimientos: Interfaz Frontend Kyū Dark Mode

## 1. Objetivo y Contexto
Implementar una interfaz web visual y moderna para la API REST de productos de la aplicación `Sintaxis`, siguiendo estrictamente el diseño de **Kyū Dark Mode** (Dark Mode inmersivo con superficies oscuras y acentos neón).

## 2. Requisitos Funcionales
- **RF-01:** Consumir el endpoint `/api/productos` para listar el catálogo completo de productos basado en el `ProductoResponseDTO`.
- **RF-02:** Mostrar tarjetas de métricas en tiempo real con el total de productos, productos activos y alertas de stock bajo (`stock <= stockMinimo`).
- **RF-03:** Proveer barra de búsqueda en tiempo real por nombre, marca o categoría.
- **RF-04:** Incluir selector de filtro por estado (Todos, Solo activos, Stock bajo / crítico).
- **RF-05:** Visualizar imágenes de producto con fallback y formateo adecuado de moneda y fechas.

## 3. Requisitos No Funcionales (Diseño Kyū Dark)
- **RNF-01:** Fondo canvas principal `#090A0F`, tarjetas `#12141C` y bordes `#1F2430`.
- **RNF-02:** Acentos neón cyan (`#38BDF8`) y rosa coral (`#F43F5E`) para alertas de stock crítico.
- **RNF-03:** Tipografía limpia combinando *Inter* y *JetBrains Mono*.
