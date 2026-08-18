package com.adrian.sintaxis.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracionPuntosTest {

    @Test
    void shouldCreateConfiguracionPuntos() {
        ConfiguracionPuntos config = new ConfiguracionPuntos();
        config.setId(1L);
        config.setPuntosParaVip(100);
        config.setPesosPorPunto(10.0);

        assertThat(config).isNotNull();
        assertThat(config.getId()).isEqualTo(1L);
        assertThat(config.getPuntosParaVip()).isEqualTo(100);
        assertThat(config.getPesosPorPunto()).isEqualTo(10.0);
    }

    @Test
    void shouldUpdateConfiguracionPuntos() {
        ConfiguracionPuntos config = new ConfiguracionPuntos();
        config.setId(1L);
        config.setPuntosParaVip(100);
        config.setPesosPorPunto(10.0);

        config.setPuntosParaVip(200);
        config.setPesosPorPunto(5.0);

        assertThat(config.getPuntosParaVip()).isEqualTo(200);
        assertThat(config.getPesosPorPunto()).isEqualTo(5.0);
    }
}