package com.adrian.sintaxis.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class VentaTest {

    private Venta venta;
    private Cliente cliente;
    private DetalleVenta detalle1;
    private DetalleVenta detalle2;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");

        venta = new Venta();
        venta.setIdVenta(1L);
        venta.setCliente(cliente);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado("Pagada");
        venta.setMetodoPago("TARJETA_CREDITO");
        venta.setSubtotal(2429.97);
        venta.setDescuento(10.0);
        venta.setTotal(2419.97);
        venta.setActivo(true);

        detalle1 = new DetalleVenta();
        detalle1.setIdDetalleVenta(1L);
        detalle1.setCantidad(2);
        detalle1.setPrecioUnitario(1199.99);
        detalle1.setVenta(venta);

        detalle2 = new DetalleVenta();
        detalle2.setIdDetalleVenta(2L);
        detalle2.setCantidad(1);
        detalle2.setPrecioUnitario(29.99);
        detalle2.setVenta(venta);

        venta.setDetalles(Arrays.asList(detalle1, detalle2));
    }

    @Test
    void shouldCreateVenta() {
        assertThat(venta).isNotNull();
        assertThat(venta.getIdVenta()).isEqualTo(1L);
        assertThat(venta.getCliente()).isNotNull();
        assertThat(venta.getCliente().getNombre()).isEqualTo("Juan");
        assertThat(venta.getEstado()).isEqualTo("Pagada");
        assertThat(venta.getTotal()).isEqualTo(2419.97);
        assertThat(venta.getDetalles()).hasSize(2);
        assertThat(venta.isActivo()).isTrue();
    }

    @Test
    void shouldHaveDetalles() {
        assertThat(venta.getDetalles()).isNotEmpty();
        assertThat(venta.getDetalles()).hasSize(2);
        assertThat(venta.getDetalles().get(0).getCantidad()).isEqualTo(2);
        assertThat(venta.getDetalles().get(1).getCantidad()).isEqualTo(1);
    }
}