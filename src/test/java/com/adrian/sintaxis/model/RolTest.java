package com.adrian.sintaxis.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RolTest {

    @Test
    void shouldHaveCorrectValues() {
        assertThat(Rol.values()).hasSize(3);
        assertThat(Rol.ADMIN).isEqualTo(Rol.ADMIN);
        assertThat(Rol.EMPLEADO).isEqualTo(Rol.EMPLEADO);
        assertThat(Rol.CLIENTE).isEqualTo(Rol.CLIENTE);
    }

    @Test
    void shouldConvertToString() {
        assertThat(Rol.ADMIN.name()).isEqualTo("ADMIN");
        assertThat(Rol.EMPLEADO.name()).isEqualTo("EMPLEADO");
        assertThat(Rol.CLIENTE.name()).isEqualTo("CLIENTE");
    }

    @Test
    void shouldGetValueOf() {
        assertThat(Rol.valueOf("ADMIN")).isEqualTo(Rol.ADMIN);
        assertThat(Rol.valueOf("EMPLEADO")).isEqualTo(Rol.EMPLEADO);
        assertThat(Rol.valueOf("CLIENTE")).isEqualTo(Rol.CLIENTE);
    }
}