package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Accesorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccesorioRepositoryTest {

    @Autowired
    private AccesorioRepository accesorioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Accesorio accesorio1;
    private Accesorio accesorio2;
    private Accesorio accesorio3;

    @BeforeEach
    void setUp() {
        // Accesorio 1 - Funda Original (compatible con iPhone 15 Pro Max)
        accesorio1 = new Accesorio();
        accesorio1.setNombre("Funda de Silicona");
        accesorio1.setPrecio(25.99);
        accesorio1.setStock(100);
        accesorio1.setStockMinimo(10);
        accesorio1.setMarca("Samsung");
        accesorio1.setCategoria("Accesorio");
        accesorio1.setFechaAlta(LocalDateTime.now());
        accesorio1.setActivo(true);
        accesorio1.setTipoAccesorio("Funda");
        accesorio1.setColor("Negro");
        accesorio1.setMaterial("Silicona");
        accesorio1.setEsOriginal(true);
        accesorio1.setMarcasCompatibles(Arrays.asList("iPhone", "Samsung", "Google"));
        entityManager.persist(accesorio1);

        // Accesorio 2 - Cargador Compatible (compatible con iPhone)
        accesorio2 = new Accesorio();
        accesorio2.setNombre("Cargador USB-C");
        accesorio2.setPrecio(15.99);
        accesorio2.setStock(50);
        accesorio2.setStockMinimo(5);
        accesorio2.setMarca("Xiaomi");
        accesorio2.setCategoria("Accesorio");
        accesorio2.setFechaAlta(LocalDateTime.now());
        accesorio2.setActivo(true);
        accesorio2.setTipoAccesorio("Cargador");
        accesorio2.setColor("Blanco");
        accesorio2.setMaterial("Plástico");
        accesorio2.setEsOriginal(false);
        accesorio2.setMarcasCompatibles(Arrays.asList("iPhone", "Samsung", "Xiaomi", "Google"));
        entityManager.persist(accesorio2);

        // Accesorio 3 - Auriculares Originales (compatible con iPhone también)
        accesorio3 = new Accesorio();
        accesorio3.setNombre("Auriculares Bluetooth");
        accesorio3.setPrecio(89.99);
        accesorio3.setStock(25);
        accesorio3.setStockMinimo(5);
        accesorio3.setMarca("Sony");
        accesorio3.setCategoria("Accesorio");
        accesorio3.setFechaAlta(LocalDateTime.now());
        accesorio3.setActivo(true);
        accesorio3.setTipoAccesorio("Auriculares");
        accesorio3.setColor("Negro");
        accesorio3.setMaterial("Plástico");
        accesorio3.setEsOriginal(true);
        accesorio3.setMarcasCompatibles(Arrays.asList("iPhone", "Samsung", "Google", "Sony"));
        entityManager.persist(accesorio3);

        entityManager.flush();
    }

    @Test
    void findByTipoAccesorio_ShouldReturnAccesorios() {
        List<Accesorio> result = accesorioRepository.findByTipoAccesorio("Funda");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTipoAccesorio()).isEqualTo("Funda");
    }

    @Test
    void findByTipoAccesorio_ShouldReturnEmpty_WhenNotFound() {
        List<Accesorio> result = accesorioRepository.findByTipoAccesorio("Inexistente");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEsOriginalTrue_ShouldReturnOnlyOriginalAccesorios() {
        List<Accesorio> result = accesorioRepository.findByEsOriginalTrue();
        assertThat(result)
                .hasSize(2)
                .allMatch(Accesorio::isEsOriginal)
                .extracting("nombre")
                .containsExactlyInAnyOrder("Funda de Silicona", "Auriculares Bluetooth");
    }

    @Test
    void findByEsOriginalTrue_ShouldReturnEmpty_WhenNoOriginales() {
        accesorio1.setEsOriginal(false);
        accesorio3.setEsOriginal(false);
        entityManager.persist(accesorio1);
        entityManager.persist(accesorio3);
        entityManager.flush();

        List<Accesorio> result = accesorioRepository.findByEsOriginalTrue();

        assertThat(result).isEmpty();
    }

    @Test
    void findByMarcasCompatiblesContaining_ShouldReturnAccesorios() {
        // ✅ CORREGIDO: Usar "iPhone" que está presente en los 3 accesorios
        List<Accesorio> result = accesorioRepository.findByMarcasCompatiblesContaining("iPhone");

        // ✅ Ahora todos los 3 accesorios tienen "iPhone" en sus marcas compatibles
        assertThat(result).hasSize(3);
        assertThat(result).extracting("nombre").containsExactlyInAnyOrder(
                "Funda de Silicona",
                "Cargador USB-C",
                "Auriculares Bluetooth"
        );
    }

    @Test
    void findByMarcasCompatiblesContaining_ShouldReturnEmpty_WhenNotFound() {
        List<Accesorio> result = accesorioRepository.findByMarcasCompatiblesContaining("MarcaInexistente");

        assertThat(result).isEmpty();
    }

    @Test
    void findByMarcasCompatiblesContaining_ShouldReturnSpecificBrand() {
        // ✅ Buscar una marca que solo esté en un accesorio
        List<Accesorio> result = accesorioRepository.findByMarcasCompatiblesContaining("Xiaomi");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Cargador USB-C");
    }
}