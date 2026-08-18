package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Celular;
import com.adrian.sintaxis.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Celular celular1;
    private Celular celular2;
    private Celular celular3;

    @BeforeEach
    void setUp() {
        // Celular 1 - Activo, con stock
        celular1 = new Celular();
        celular1.setNombre("Samsung Galaxy S24");
        celular1.setDescripcion("Smartphone de alta gama");
        celular1.setPrecio(1499.99);
        celular1.setStock(50);
        celular1.setStockMinimo(5);
        celular1.setMarca("Samsung");
        celular1.setCategoria("Celular");
        celular1.setFechaAlta(LocalDateTime.now());
        celular1.setActivo(true);
        celular1.setModelo("Galaxy S24");
        celular1.setAlmacenamientoGB(256);
        celular1.setRamGB(12);
        celular1.setColor("Negro");
        celular1.setProcesador("Snapdragon 8 Gen 3");
        celular1.setPantallaPulgadas(6.8);
        celular1.setBateriaMAh(5000);
        celular1.setSistemaOperativo("Android 14");
        celular1.setEsLibre(true);
        entityManager.persist(celular1);

        // Celular 2 - Activo, stock bajo
        celular2 = new Celular();
        celular2.setNombre("iPhone 15");
        celular2.setDescripcion("iPhone de última generación");
        celular2.setPrecio(1799.99);
        celular2.setStock(2);
        celular2.setStockMinimo(5);
        celular2.setMarca("Apple");
        celular2.setCategoria("Celular");
        celular2.setFechaAlta(LocalDateTime.now());
        celular2.setActivo(true);
        celular2.setModelo("iPhone 15");
        celular2.setAlmacenamientoGB(128);
        celular2.setRamGB(6);
        celular2.setColor("Blanco");
        celular2.setProcesador("A16 Bionic");
        celular2.setPantallaPulgadas(6.1);
        celular2.setBateriaMAh(3349);
        celular2.setSistemaOperativo("iOS 17");
        celular2.setEsLibre(false);
        entityManager.persist(celular2);

        // Celular 3 - Inactivo
        celular3 = new Celular();
        celular3.setNombre("Xiaomi Mi 13");
        celular3.setDescripcion("Smartphone económico");
        celular3.setPrecio(699.99);
        celular3.setStock(10);
        celular3.setStockMinimo(3);
        celular3.setMarca("Xiaomi");
        celular3.setCategoria("Celular");
        celular3.setFechaAlta(LocalDateTime.now());
        celular3.setActivo(false);
        celular3.setModelo("Mi 13");
        celular3.setAlmacenamientoGB(128);
        celular3.setRamGB(8);
        celular3.setColor("Azul");
        celular3.setProcesador("Snapdragon 8 Gen 2");
        celular3.setPantallaPulgadas(6.36);
        celular3.setBateriaMAh(4500);
        celular3.setSistemaOperativo("Android 13");
        celular3.setEsLibre(true);
        entityManager.persist(celular3);

        entityManager.flush();
    }

    @Test
    void findByCategoria_ShouldReturnProductos() {
        List<Producto> result = productoRepository.findByCategoria("Celular");

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(p -> p.getCategoria().equals("Celular"));
    }

    @Test
    void findByCategoria_ShouldReturnEmpty_WhenNotFound() {
        List<Producto> result = productoRepository.findByCategoria("Accesorio");

        assertThat(result).isEmpty();
    }

    @Test
    void findByMarca_ShouldReturnProductos() {
        List<Producto> result = productoRepository.findByMarca("Samsung");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMarca()).isEqualTo("Samsung");
    }

    @Test
    void findByActivoTrue_ShouldReturnOnlyActiveProductos() {
        List<Producto> result = productoRepository.findByActivoTrue();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Producto::isActivo);
    }

    @Test
    void findByStockGreaterThan_ShouldReturnProductosWithStock() {
        List<Producto> result = productoRepository.findByStockGreaterThan(5);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getStock() > 5);
    }

    @Test
    void findByStockGreaterThan_ShouldReturnEmpty_WhenNoStock() {
        List<Producto> result = productoRepository.findByStockGreaterThan(100);

        assertThat(result).isEmpty();
    }

    @Test
    void findByNombreContaining_ShouldReturnProductos() {
        List<Producto> result = productoRepository.findByNombreContaining("Samsung");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).contains("Samsung");
    }

    @Test
    void findProductosConStockBajo_ShouldReturnProductsWithLowStock() {
        List<Producto> result = productoRepository.findProductosConStockBajo();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdProducto()).isEqualTo(celular2.getIdProducto());
        assertThat(result.get(0).getStock()).isLessThanOrEqualTo(5);
    }
}
