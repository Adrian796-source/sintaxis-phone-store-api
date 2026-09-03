package com.adrian.sintaxis.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class AccesorioTest {

    private Accesorio accesorio;

    @BeforeEach
    void setUp() {
        accesorio = new Accesorio();
        accesorio.setIdProducto(1L);
        accesorio.setNombre("Funda de Silicona");
        accesorio.setDescripcion("Funda de silicona flexible para iPhone 15 Pro Max");
        accesorio.setPrecio(25.99);
        accesorio.setStock(100);
        accesorio.setMarca("Samsung");
        accesorio.setCategoria("Accesorio");
        accesorio.setFechaAlta(LocalDateTime.now());
        accesorio.setActivo(true);
        accesorio.setStockMinimo(10);
        accesorio.setTipoAccesorio("Funda");
        accesorio.setColor("Negro");
        accesorio.setMaterial("Silicona");
        accesorio.setEsOriginal(true);
        accesorio.setMarcasCompatibles(Arrays.asList("iPhone 15 Pro Max", "Samsung Galaxy S24 Ultra", "Google Pixel 8"));
    }

    @Test
    void shouldCreateAccesorio() {
        assertThat(accesorio).isNotNull();
        assertThat(accesorio.getIdProducto()).isEqualTo(1L);
        assertThat(accesorio.getNombre()).isEqualTo("Funda de Silicona");
        assertThat(accesorio.getPrecio()).isEqualTo(25.99);
        assertThat(accesorio.getStock()).isEqualTo(100);
        assertThat(accesorio.getMarca()).isEqualTo("Samsung");
        assertThat(accesorio.getTipoAccesorio()).isEqualTo("Funda");
        assertThat(accesorio.getColor()).isEqualTo("Negro");
        assertThat(accesorio.getMaterial()).isEqualTo("Silicona");
        assertThat(accesorio.isEsOriginal()).isTrue();
        assertThat(accesorio.getMarcasCompatibles()).hasSize(3);
    }

    @Test
    void shouldShowDetails() {
        String detalles = accesorio.mostrarDetalles();
        assertThat(detalles)
                .contains("Funda")
                .contains("Silicona")
                .contains("Original")
                .contains("iPhone 15 Pro Max");
    }

}