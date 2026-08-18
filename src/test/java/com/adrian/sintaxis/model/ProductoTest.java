package com.adrian.sintaxis.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoTest {

    // Clase concreta para testing
    static class ProductoTestImpl extends Producto {
        @Override
        public String mostrarDetalles() {
            return "Producto: " + getNombre();
        }
    }

    private ProductoTestImpl producto;

    @BeforeEach
    void setUp() {
        producto = new ProductoTestImpl();
        producto.setIdProducto(1L);
        producto.setNombre("Producto Test");
        producto.setDescripcion("Descripción del producto");
        producto.setPrecio(100.0);
        producto.setStock(10);
        producto.setMarca("Marca Test");
        producto.setCategoria("Categoria Test");
        producto.setFechaAlta(LocalDateTime.now());
        producto.setActivo(true);
        producto.setStockMinimo(5);
    }

    @Test
    void shouldCreateProducto() {
        assertThat(producto).isNotNull();
        assertThat(producto.getIdProducto()).isEqualTo(1L);
        assertThat(producto.getNombre()).isEqualTo("Producto Test");
        assertThat(producto.getPrecio()).isEqualTo(100.0);
        assertThat(producto.getStock()).isEqualTo(10);
        assertThat(producto.getMarca()).isEqualTo("Marca Test");
        assertThat(producto.getCategoria()).isEqualTo("Categoria Test");
        assertThat(producto.getFechaAlta()).isNotNull();
        assertThat(producto.isActivo()).isTrue();
        assertThat(producto.getStockMinimo()).isEqualTo(5);
    }

    @Test
    void shouldHaveStock() {
        assertThat(producto.tieneStock()).isTrue();
        producto.setStock(0);
        assertThat(producto.tieneStock()).isFalse();
    }

    @Test
    void shouldReduceStock() {
        producto.reducirStock(3);
        assertThat(producto.getStock()).isEqualTo(7);
    }

    @Test
    void shouldThrowExceptionWhenReducingStockInsufficient() {
        assertThatThrownBy(() -> producto.reducirStock(15))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void shouldReturnStock() {
        producto.devolverStock(5);
        assertThat(producto.getStock()).isEqualTo(15);
    }

    @Test
    void shouldShowDetails() {
        String detalles = producto.mostrarDetalles();
        assertThat(detalles).contains("Producto");
        assertThat(detalles).contains(producto.getNombre());
    }
}