package com.adrian.sintaxis.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CelularTest {

    private Celular celular;

    @BeforeEach
    void setUp() {
        celular = new Celular();
        celular.setIdProducto(1L);
        celular.setNombre("Samsung Galaxy S24 Ultra");
        celular.setDescripcion("Smartphone de alta gama");
        celular.setPrecio(1499.99);
        celular.setStock(50);
        celular.setMarca("Samsung");
        celular.setCategoria("Celular");
        celular.setFechaAlta(LocalDateTime.now());
        celular.setActivo(true);
        celular.setStockMinimo(5);
        celular.setModelo("Galaxy S24 Ultra");
        celular.setAlmacenamientoGB(256);
        celular.setRamGB(12);
        celular.setColor("Negro");
        celular.setProcesador("Snapdragon 8 Gen 3");
        celular.setPantallaPulgadas(6.8);
        celular.setBateriaMAh(5000);
        celular.setSistemaOperativo("Android 14");
        celular.setEsLibre(true);
    }

    @Test
    void shouldCreateCelular() {
        assertThat(celular).isNotNull();
        assertThat(celular.getIdProducto()).isEqualTo(1L);
        assertThat(celular.getNombre()).isEqualTo("Samsung Galaxy S24 Ultra");
        assertThat(celular.getPrecio()).isEqualTo(1499.99);
        assertThat(celular.getStock()).isEqualTo(50);
        assertThat(celular.getMarca()).isEqualTo("Samsung");
        assertThat(celular.getModelo()).isEqualTo("Galaxy S24 Ultra");
        assertThat(celular.getAlmacenamientoGB()).isEqualTo(256);
        assertThat(celular.getRamGB()).isEqualTo(12);
        assertThat(celular.getProcesador()).isEqualTo("Snapdragon 8 Gen 3");
        assertThat(celular.getSistemaOperativo()).isEqualTo("Android 14");
        assertThat(celular.isEsLibre()).isTrue();
    }

    @Test
    void shouldShowDetails() {
        String detalles = celular.mostrarDetalles();
        assertThat(detalles).contains("Samsung");
        assertThat(detalles).contains("Android 14");
        assertThat(detalles).contains("12 GB RAM");
        assertThat(detalles).contains("256 GB Storage");
        assertThat(detalles).contains("5000 mAh");
    }
}