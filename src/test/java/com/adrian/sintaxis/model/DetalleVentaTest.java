package com.adrian.sintaxis.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DetalleVentaTest {

    private DetalleVenta detalle;
    private Producto producto;
    private Venta venta;

    @BeforeEach
    void setUp() {
        producto = new Celular();
        producto.setIdProducto(1L);
        producto.setNombre("iPhone 15 Pro");
        producto.setPrecio(1199.99);

        venta = new Venta();
        venta.setIdVenta(1L);

        detalle = new DetalleVenta();
        detalle.setIdDetalleVenta(1L);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(1199.99);
        detalle.setProducto(producto);
        detalle.setVenta(venta);
    }

    @Test
    void shouldCreateDetalleVenta() {
        assertThat(detalle).isNotNull();
        assertThat(detalle.getIdDetalleVenta()).isEqualTo(1L);
        assertThat(detalle.getCantidad()).isEqualTo(2);
        assertThat(detalle.getPrecioUnitario()).isEqualTo(1199.99);
        assertThat(detalle.getProducto()).isNotNull();
        assertThat(detalle.getProducto().getNombre()).isEqualTo("iPhone 15 Pro");
        assertThat(detalle.getVenta()).isNotNull();
    }

    @Test
    void shouldCalculateSubtotal() {
        double subtotal = detalle.getPrecioUnitario() * detalle.getCantidad();
        assertThat(subtotal).isEqualTo(2399.98);
    }
}