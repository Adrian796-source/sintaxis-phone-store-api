package com.adrian.sintaxis.service;

import com.adrian.sintaxis.model.Accesorio;
import com.adrian.sintaxis.model.Subcategoria;
import com.adrian.sintaxis.repository.AccesorioRepository;
import com.adrian.sintaxis.repository.SubcategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración real (H2) del flujo de eliminación de subcategorías:
 * verifica que los accesorios asociados se desvinculan y que la subcategoría
 * se elimina sin romper restricciones de clave foránea.
 */
@DataJpaTest
class SubcategoriaServiceIntegrationTest {

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Autowired
    private AccesorioRepository accesorioRepository;

    private SubcategoriaService subcategoriaService;

    @BeforeEach
    void setUp() {
        subcategoriaService = new SubcategoriaService(subcategoriaRepository, accesorioRepository);
    }

    @Test
    void eliminar_conAccesoriosAsociados_desvinculaAccesoriosYBorraSubcategoria() {
        Subcategoria subcategoria = new Subcategoria();
        subcategoria.setNombre("Templados");
        subcategoriaRepository.save(subcategoria);

        Accesorio accesorio = new Accesorio();
        accesorio.setNombre("Templado Vidrio 9H");
        accesorio.setPrecio(9.99);
        accesorio.setStock(20);
        accesorio.setStockMinimo(3);
        accesorio.setMarca("Genérico");
        accesorio.setCategoria("Accesorio");
        accesorio.setFechaAlta(LocalDateTime.now());
        accesorio.setActivo(true);
        accesorio.setTipoAccesorio("Templado");
        accesorio.setSubcategoria(subcategoria);
        accesorioRepository.save(accesorio);

        accesorioRepository.flush();

        subcategoriaService.eliminar(subcategoria.getIdSubcategoria());

        assertThat(subcategoriaRepository.findById(subcategoria.getIdSubcategoria())).isEmpty();

        Accesorio recargado = accesorioRepository.findById(accesorio.getIdProducto()).orElseThrow();
        assertThat(recargado.getSubcategoria()).isNull();
        assertThat(recargado.getNombre()).isEqualTo("Templado Vidrio 9H");
    }
}
