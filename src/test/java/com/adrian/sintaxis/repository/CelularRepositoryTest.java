package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Celular;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CelularRepositoryTest {

    @Autowired
    private CelularRepository celularRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Celular celular1;
    private Celular celular2;
    private Celular celular3;

    @BeforeEach
    void setUp() {
        // Celular 1 - Samsung, Android, Libre
        celular1 = new Celular();
        celular1.setNombre("Samsung Galaxy S24 Ultra");
        celular1.setPrecio(1499.99);
        celular1.setStock(50);
        celular1.setStockMinimo(5);
        celular1.setMarca("Samsung");
        celular1.setCategoria("Celular");
        celular1.setFechaAlta(LocalDateTime.now());
        celular1.setActivo(true);
        celular1.setModelo("Galaxy S24 Ultra");
        celular1.setAlmacenamientoGB(512);
        celular1.setRamGB(12);
        celular1.setColor("Negro");
        celular1.setProcesador("Snapdragon 8 Gen 3");
        celular1.setPantallaPulgadas(6.8);
        celular1.setBateriaMAh(5000);
        celular1.setSistemaOperativo("Android 14");
        celular1.setEsLibre(true);
        entityManager.persist(celular1);

        // Celular 2 - Apple, iOS, No Libre
        celular2 = new Celular();
        celular2.setNombre("iPhone 15 Pro Max");
        celular2.setPrecio(1799.99);
        celular2.setStock(30);
        celular2.setStockMinimo(5);
        celular2.setMarca("Apple");
        celular2.setCategoria("Celular");
        celular2.setFechaAlta(LocalDateTime.now());
        celular2.setActivo(true);
        celular2.setModelo("iPhone 15 Pro Max");
        celular2.setAlmacenamientoGB(256);
        celular2.setRamGB(8);
        celular2.setColor("Titanio");
        celular2.setProcesador("A17 Pro");
        celular2.setPantallaPulgadas(6.7);
        celular2.setBateriaMAh(4422);
        celular2.setSistemaOperativo("iOS 17");
        celular2.setEsLibre(false);
        entityManager.persist(celular2);

        // Celular 3 - Xiaomi, Android, Libre
        celular3 = new Celular();
        celular3.setNombre("Xiaomi 14 Pro");
        celular3.setPrecio(999.99);
        celular3.setStock(20);
        celular3.setStockMinimo(3);
        celular3.setMarca("Xiaomi");
        celular3.setCategoria("Celular");
        celular3.setFechaAlta(LocalDateTime.now());
        celular3.setActivo(true);
        celular3.setModelo("14 Pro");
        celular3.setAlmacenamientoGB(128);
        celular3.setRamGB(8);
        celular3.setColor("Negro");
        celular3.setProcesador("Snapdragon 8 Gen 3");
        celular3.setPantallaPulgadas(6.36);
        celular3.setBateriaMAh(4610);
        celular3.setSistemaOperativo("Android 14");
        celular3.setEsLibre(true);
        entityManager.persist(celular3);

        entityManager.flush();
    }

    @Test
    void findByModeloContainingIgnoreCase_ShouldReturnCelulares() {
        List<Celular> result = celularRepository.findByModeloContainingIgnoreCase("galaxy");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModelo()).isEqualTo("Galaxy S24 Ultra");
    }

    @Test
    void findByModeloContainingIgnoreCase_ShouldReturnEmpty_WhenNotFound() {
        List<Celular> result = celularRepository.findByModeloContainingIgnoreCase("inexistente");

        assertThat(result).isEmpty();
    }

    @Test
    void findBySistemaOperativo_ShouldReturnCelulares() {
        List<Celular> result = celularRepository.findBySistemaOperativo("Android 14");
        assertThat(result)
                .hasSize(2)
                .allMatch(c -> c.getSistemaOperativo().equals("Android 14"));
    }

    @Test
    void findBySistemaOperativo_ShouldReturnEmpty_WhenNotFound() {
        List<Celular> result = celularRepository.findBySistemaOperativo("Windows");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEsLibreTrue_ShouldReturnOnlyLibreCelulares() {
        List<Celular> result = celularRepository.findByEsLibreTrue();
        assertThat(result)
                .hasSize(2)
                .allMatch(Celular::isEsLibre);
    }

    @Test
    void findByAlmacenamientoGBBetween_ShouldReturnCelularesInRange() {
        List<Celular> result = celularRepository.findByAlmacenamientoGBBetween(200, 400);
        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(Celular::getAlmacenamientoGB)
                .isEqualTo(256);
    }

    @Test
    void findByAlmacenamientoGBBetween_ShouldReturnEmpty_WhenNoCelulares() {
        List<Celular> result = celularRepository.findByAlmacenamientoGBBetween(1000, 2000);

        assertThat(result).isEmpty();
    }

    @Test
    void findByMarca_ShouldReturnCelulares() {
        List<Celular> result = celularRepository.findByMarca("Apple");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMarca()).isEqualTo("Apple");
    }
}