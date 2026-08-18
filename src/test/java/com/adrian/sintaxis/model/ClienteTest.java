package com.adrian.sintaxis.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setEmail("juan@test.com");
        cliente.setTelefono("123456789");
        cliente.setDireccion("Calle Falsa 123");
        cliente.setFechaRegistro(LocalDateTime.now());
        cliente.setEsVip(false);
        cliente.setPuntosAcumulados(0);
        cliente.setActivo(true);
    }

    @Test
    void shouldCreateCliente() {
        assertThat(cliente).isNotNull();
        assertThat(cliente.getIdCliente()).isEqualTo(1L);
        assertThat(cliente.getNombre()).isEqualTo("Juan");
        assertThat(cliente.getApellido()).isEqualTo("Perez");
        assertThat(cliente.getEmail()).isEqualTo("juan@test.com");
        assertThat(cliente.getTelefono()).isEqualTo("123456789");
        assertThat(cliente.getDireccion()).isEqualTo("Calle Falsa 123");
        assertThat(cliente.getFechaRegistro()).isNotNull();
        assertThat(cliente.isEsVip()).isFalse();
        assertThat(cliente.getPuntosAcumulados()).isEqualTo(0);
        assertThat(cliente.isActivo()).isTrue();
    }

    @Test
    void shouldSetVipStatus() {
        cliente.setEsVip(true);
        cliente.setPuntosAcumulados(150);

        assertThat(cliente.isEsVip()).isTrue();
        assertThat(cliente.getPuntosAcumulados()).isEqualTo(150);
    }

    @Test
    void shouldSetInactive() {
        cliente.setActivo(false);
        assertThat(cliente.isActivo()).isFalse();
    }
}