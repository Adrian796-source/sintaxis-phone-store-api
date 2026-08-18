package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.ConfiguracionPuntos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ConfiguracionPuntosRepositoryTest {

    @Autowired
    private ConfiguracionPuntosRepository configuracionPuntosRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_ShouldPersistConfiguracionPuntos() {
        ConfiguracionPuntos config = new ConfiguracionPuntos();
        config.setId(1L);
        config.setPuntosParaVip(100);
        config.setPesosPorPunto(10.0);

        ConfiguracionPuntos saved = configuracionPuntosRepository.save(config);
        entityManager.flush();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getPuntosParaVip()).isEqualTo(100);
        assertThat(saved.getPesosPorPunto()).isEqualTo(10.0);
    }

    @Test
    void findById_ShouldReturnConfiguracion_WhenExists() {
        ConfiguracionPuntos config = new ConfiguracionPuntos();
        config.setId(1L);
        config.setPuntosParaVip(150);
        config.setPesosPorPunto(8.0);
        entityManager.persist(config);
        entityManager.flush();

        Optional<ConfiguracionPuntos> result = configuracionPuntosRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getPuntosParaVip()).isEqualTo(150);
        assertThat(result.get().getPesosPorPunto()).isEqualTo(8.0);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        Optional<ConfiguracionPuntos> result = configuracionPuntosRepository.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void update_ShouldUpdateConfiguracionPuntos() {
        ConfiguracionPuntos config = new ConfiguracionPuntos();
        config.setId(1L);
        config.setPuntosParaVip(100);
        config.setPesosPorPunto(10.0);
        entityManager.persist(config);
        entityManager.flush();

        // Actualizar
        config.setPuntosParaVip(200);
        config.setPesosPorPunto(5.0);
        ConfiguracionPuntos updated = configuracionPuntosRepository.save(config);
        entityManager.flush();

        assertThat(updated.getPuntosParaVip()).isEqualTo(200);
        assertThat(updated.getPesosPorPunto()).isEqualTo(5.0);
    }
}