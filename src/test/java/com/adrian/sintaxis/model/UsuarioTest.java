package com.adrian.sintaxis.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    private Usuario usuario;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");

        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");
        usuario.setPassword("encodedPassword");
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
        usuario.setCliente(cliente);
    }

    @Test
    void shouldCreateUsuario() {
        assertThat(usuario).isNotNull();
        assertThat(usuario.getIdUsuario()).isEqualTo(1L);
        assertThat(usuario.getNombre()).isEqualTo("Juan");
        assertThat(usuario.getEmail()).isEqualTo("juan@test.com");
        assertThat(usuario.getPassword()).isEqualTo("encodedPassword");
        assertThat(usuario.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(usuario.isActivo()).isTrue();
        assertThat(usuario.getCliente()).isNotNull();
        assertThat(usuario.getCliente().getNombre()).isEqualTo("Juan");
    }

    @Test
    void shouldSetRolAdmin() {
        usuario.setRol(Rol.ADMIN);
        assertThat(usuario.getRol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    void shouldSetRolEmpleado() {
        usuario.setRol(Rol.EMPLEADO);
        assertThat(usuario.getRol()).isEqualTo(Rol.EMPLEADO);
    }

    @Test
    void shouldSetInactive() {
        usuario.setActivo(false);
        assertThat(usuario.isActivo()).isFalse();
    }
}