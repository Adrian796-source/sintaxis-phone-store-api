package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.SubcategoriaDTO;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.exception.ValoresInvalidosException;
import com.adrian.sintaxis.model.Accesorio;
import com.adrian.sintaxis.model.Subcategoria;
import com.adrian.sintaxis.repository.AccesorioRepository;
import com.adrian.sintaxis.repository.SubcategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubcategoriaServiceTest {

    @Mock
    private SubcategoriaRepository subcategoriaRepository;

    @Mock
    private AccesorioRepository accesorioRepository;

    @InjectMocks
    private SubcategoriaService subcategoriaService;

    private Subcategoria fundas;

    @BeforeEach
    void setUp() {
        fundas = new Subcategoria();
        fundas.setIdSubcategoria(1L);
        fundas.setNombre("Fundas");
    }

    @Test
    void listarTodas_devuelveTodasLasSubcategoriasMapeadas() {
        Subcategoria cargadores = new Subcategoria();
        cargadores.setIdSubcategoria(2L);
        cargadores.setNombre("Cargadores");

        when(subcategoriaRepository.findAll()).thenReturn(Arrays.asList(fundas, cargadores));

        List<SubcategoriaDTO> resultado = subcategoriaService.listarTodas();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getIdSubcategoria()).isEqualTo(1L);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Fundas");
        assertThat(resultado.get(1).getNombre()).isEqualTo("Cargadores");
    }

    @Test
    void guardar_creaSubcategoriaCuandoElNombreNoExiste() {
        when(subcategoriaRepository.existsByNombreIgnoreCase("Templados")).thenReturn(false);
        when(subcategoriaRepository.save(any(Subcategoria.class))).thenAnswer(inv -> {
            Subcategoria s = inv.getArgument(0);
            s.setIdSubcategoria(10L);
            return s;
        });

        SubcategoriaDTO dto = new SubcategoriaDTO(null, "  Templados  ");
        SubcategoriaDTO resultado = subcategoriaService.guardar(dto);

        assertThat(resultado.getIdSubcategoria()).isEqualTo(10L);
        assertThat(resultado.getNombre()).isEqualTo("Templados");
    }

    @Test
    void guardar_lanzaExcepcionSiElNombreYaExisteIgnorandoMayusculas() {
        when(subcategoriaRepository.existsByNombreIgnoreCase("fundas")).thenReturn(true);

        SubcategoriaDTO dto = new SubcategoriaDTO(null, "fundas");

        assertThatThrownBy(() -> subcategoriaService.guardar(dto))
                .isInstanceOf(ValoresInvalidosException.class)
                .hasMessageContaining("Ya existe una subcategoría");

        verify(subcategoriaRepository, never()).save(any());
    }

    @Test
    void guardar_lanzaExcepcionSiElNombreEstaVacio() {
        SubcategoriaDTO dto = new SubcategoriaDTO(null, "   ");

        assertThatThrownBy(() -> subcategoriaService.guardar(dto))
                .isInstanceOf(ValoresInvalidosException.class)
                .hasMessageContaining("obligatorio");
    }

    @Test
    void actualizar_modificaElNombreDeLaSubcategoria() {
        when(subcategoriaRepository.findById(1L)).thenReturn(Optional.of(fundas));
        when(subcategoriaRepository.existsByNombreIgnoreCaseAndIdSubcategoriaNot("Fundas Premium", 1L))
                .thenReturn(false);
        when(subcategoriaRepository.save(any(Subcategoria.class))).thenAnswer(inv -> inv.getArgument(0));

        SubcategoriaDTO resultado = subcategoriaService.actualizar(1L, new SubcategoriaDTO(1L, "Fundas Premium"));

        assertThat(resultado.getNombre()).isEqualTo("Fundas Premium");
        assertThat(fundas.getNombre()).isEqualTo("Fundas Premium");
    }

    @Test
    void actualizar_lanzaExcepcionSiNoExisteLaSubcategoria() {
        when(subcategoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subcategoriaService.actualizar(99L, new SubcategoriaDTO(99L, "Otra")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void actualizar_lanzaExcepcionSiElNuevoNombreYaPerteneceAOtraSubcategoria() {
        when(subcategoriaRepository.findById(1L)).thenReturn(Optional.of(fundas));
        when(subcategoriaRepository.existsByNombreIgnoreCaseAndIdSubcategoriaNot("Cargadores", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> subcategoriaService.actualizar(1L, new SubcategoriaDTO(1L, "Cargadores")))
                .isInstanceOf(ValoresInvalidosException.class)
                .hasMessageContaining("Ya existe otra subcategoría");
    }

    @Test
    void eliminar_desvinculaAccesoriosYEliminaLaSubcategoria() {
        Accesorio fundaIphone = new Accesorio();
        fundaIphone.setSubcategoria(fundas);
        Accesorio fundaSamsung = new Accesorio();
        fundaSamsung.setSubcategoria(fundas);

        when(subcategoriaRepository.findById(1L)).thenReturn(Optional.of(fundas));
        when(accesorioRepository.findBySubcategoriaIdSubcategoria(1L))
                .thenReturn(Arrays.asList(fundaIphone, fundaSamsung));

        subcategoriaService.eliminar(1L);

        assertThat(fundaIphone.getSubcategoria()).isNull();
        assertThat(fundaSamsung.getSubcategoria()).isNull();
        verify(accesorioRepository).saveAll(Arrays.asList(fundaIphone, fundaSamsung));
        verify(subcategoriaRepository).delete(fundas);
    }

    @Test
    void eliminar_sinAccesoriosAsociados_eliminaLaSubcategoria() {
        when(subcategoriaRepository.findById(1L)).thenReturn(Optional.of(fundas));
        when(accesorioRepository.findBySubcategoriaIdSubcategoria(1L)).thenReturn(List.of());

        subcategoriaService.eliminar(1L);

        verify(accesorioRepository, never()).saveAll(any());
        verify(subcategoriaRepository).delete(fundas);
    }

    @Test
    void eliminar_lanzaExcepcionSiNoExisteLaSubcategoria() {
        when(subcategoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subcategoriaService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no encontrada");

        verify(subcategoriaRepository, never()).delete(any());
    }
}
