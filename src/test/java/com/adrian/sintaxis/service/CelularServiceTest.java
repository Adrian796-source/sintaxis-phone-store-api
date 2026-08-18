package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.CelularRequestDTO;
import com.adrian.sintaxis.dto.CelularResponseDTO;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.model.Celular;
import com.adrian.sintaxis.repository.CelularRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CelularServiceTest {

    @Mock
    private CelularRepository celularRepository;

    @InjectMocks
    private CelularService celularService;

    private Celular celular;
    private Celular celular2;
    private CelularRequestDTO celularRequestDTO;

    @BeforeEach
    void setUp() {
        // ✅ NO hay campo uploadPath en CelularService, así que no lo configuramos

        // Configurar Celular 1
        celular = new Celular();
        celular.setIdProducto(1L);
        celular.setNombre("Samsung Galaxy S24 Ultra");
        celular.setDescripcion("Smartphone de alta gama");
        celular.setPrecio(1499.99);
        celular.setStock(50);
        celular.setStockMinimo(5);
        celular.setMarca("Samsung");
        celular.setCategoria("Celular");
        celular.setFechaAlta(LocalDateTime.now());
        celular.setActivo(true);
        celular.setModelo("Galaxy S24 Ultra");
        celular.setAlmacenamientoGB(256);
        celular.setRamGB(12);
        celular.setColor("Negro");
        celular.setProcesador("Snapdragon 8 Gen 3");
        celular.setPantallaPulgadas(6.8);
        celular.setBateriaMAh(5000);
        celular.setSistemaOperativo("Android 14");
        celular.setEsLibre(true);

        // Configurar Celular 2
        celular2 = new Celular();
        celular2.setIdProducto(2L);
        celular2.setNombre("iPhone 15 Pro Max");
        celular2.setDescripcion("iPhone de última generación");
        celular2.setPrecio(1799.99);
        celular2.setStock(30);
        celular2.setStockMinimo(5);
        celular2.setMarca("Apple");
        celular2.setCategoria("Celular");
        celular2.setFechaAlta(LocalDateTime.now());
        celular2.setActivo(true);
        celular2.setModelo("iPhone 15 Pro Max");
        celular2.setAlmacenamientoGB(512);
        celular2.setRamGB(8);
        celular2.setColor("Titanio");
        celular2.setProcesador("A17 Pro");
        celular2.setPantallaPulgadas(6.7);
        celular2.setBateriaMAh(4422);
        celular2.setSistemaOperativo("iOS 17");
        celular2.setEsLibre(false);

        // Configurar DTO de Request
        celularRequestDTO = new CelularRequestDTO();
        celularRequestDTO.setNombre("Samsung Galaxy S24 Ultra");
        celularRequestDTO.setDescripcion("Smartphone de alta gama");
        celularRequestDTO.setPrecio(1499.99);
        celularRequestDTO.setStock(50);
        celularRequestDTO.setStockMinimo(5);
        celularRequestDTO.setMarca("Samsung");
        celularRequestDTO.setModelo("Galaxy S24 Ultra");
        celularRequestDTO.setAlmacenamientoGB(256);
        celularRequestDTO.setRamGB(12);
        celularRequestDTO.setColor("Negro");
        celularRequestDTO.setProcesador("Snapdragon 8 Gen 3");
        celularRequestDTO.setPantallaPulgadas(6.8);
        celularRequestDTO.setBateriaMAh(5000);
        celularRequestDTO.setSistemaOperativo("Android 14");
        celularRequestDTO.setEsLibre(true);
    }

    // ==================== TESTS GUARDAR ====================

    @Test
    void guardar_ShouldCreateCelular_WhenValid() {
        when(celularRepository.save(any(Celular.class))).thenReturn(celular);

        CelularResponseDTO result = celularService.guardar(celularRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getIdProducto()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Samsung Galaxy S24 Ultra");
        assertThat(result.getPrecio()).isEqualTo(1499.99);
        assertThat(result.getStock()).isEqualTo(50);
        assertThat(result.getMarca()).isEqualTo("Samsung");
        assertThat(result.getModelo()).isEqualTo("Galaxy S24 Ultra");
        assertThat(result.getAlmacenamientoGB()).isEqualTo(256);
        assertThat(result.getRamGB()).isEqualTo(12);
        assertThat(result.getSistemaOperativo()).isEqualTo("Android 14");
        assertThat(result.isEsLibre()).isTrue();
        assertThat(result.isActivo()).isTrue();

        verify(celularRepository).save(any(Celular.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenPrecioIsInvalid() {
        celularRequestDTO.setPrecio(0.0);

        assertThatThrownBy(() -> celularService.guardar(celularRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El precio debe ser mayor a 0");

        verify(celularRepository, never()).save(any(Celular.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenAlmacenamientoIsInvalid() {
        celularRequestDTO.setAlmacenamientoGB(0);

        assertThatThrownBy(() -> celularService.guardar(celularRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El almacenamiento debe ser mayor a 0");

        verify(celularRepository, never()).save(any(Celular.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenBateriaIsInvalid() {
        celularRequestDTO.setBateriaMAh(0);

        assertThatThrownBy(() -> celularService.guardar(celularRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La batería debe ser mayor a 0");

        verify(celularRepository, never()).save(any(Celular.class));
    }

    // ==================== TESTS BUSCAR POR ID ====================

    @Test
    void buscarPorId_ShouldReturnCelular_WhenExists() {
        when(celularRepository.findById(1L)).thenReturn(Optional.of(celular));

        Optional<CelularResponseDTO> result = celularService.buscarPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getIdProducto()).isEqualTo(1L);
        assertThat(result.get().getNombre()).isEqualTo("Samsung Galaxy S24 Ultra");
    }

    @Test
    void buscarPorId_ShouldThrowException_WhenNotFound() {
        when(celularRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> celularService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Celular no encontrado con id: 99");
    }

    // ==================== TESTS LISTAR TODOS ====================

    @Test
    void listarTodos_ShouldReturnPageOfCelulares() {
        Page<Celular> page = new PageImpl<>(Arrays.asList(celular, celular2));
        when(celularRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<CelularResponseDTO> result = celularService.listarTodos(Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getIdProducto()).isEqualTo(1L);
        assertThat(result.getContent().get(1).getIdProducto()).isEqualTo(2L);
    }

    @Test
    void listarTodos_ShouldReturnEmptyPage_WhenNoCelulares() {
        Page<Celular> emptyPage = new PageImpl<>(Arrays.asList());
        when(celularRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<CelularResponseDTO> result = celularService.listarTodos(Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    // ==================== TESTS ACTUALIZAR ====================

    @Test
    void actualizar_ShouldUpdateCelular_WhenValid() {
        when(celularRepository.findById(1L)).thenReturn(Optional.of(celular));
        when(celularRepository.save(any(Celular.class))).thenReturn(celular);

        CelularResponseDTO result = celularService.actualizar(1L, celularRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getIdProducto()).isEqualTo(1L);
        verify(celularRepository).save(any(Celular.class));
    }

    @Test
    void actualizar_ShouldThrowException_WhenCelularNotFound() {
        when(celularRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> celularService.actualizar(99L, celularRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Celular no encontrado con id: 99");

        verify(celularRepository, never()).save(any(Celular.class));
    }

    @Test
    void actualizar_ShouldThrowException_WhenPrecioIsInvalid() {
        when(celularRepository.findById(1L)).thenReturn(Optional.of(celular));
        celularRequestDTO.setPrecio(0.0);

        assertThatThrownBy(() -> celularService.actualizar(1L, celularRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El precio debe ser mayor a 0");

        verify(celularRepository, never()).save(any(Celular.class));
    }

    // ==================== TESTS ELIMINAR ====================

    @Test
    void eliminar_ShouldSoftDeleteCelular_WhenExists() {
        when(celularRepository.findById(1L)).thenReturn(Optional.of(celular));
        when(celularRepository.save(any(Celular.class))).thenReturn(celular);

        celularService.eliminar(1L);

        assertThat(celular.isActivo()).isFalse();
        verify(celularRepository).save(celular);
    }

    @Test
    void eliminar_ShouldThrowException_WhenCelularNotFound() {
        when(celularRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> celularService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Celular no encontrado con id: 99");

        verify(celularRepository, never()).save(any(Celular.class));
    }

    // ==================== TESTS BUSCAR POR MODELO ====================

    @Test
    void buscarPorModelo_ShouldReturnCelulares() {
        when(celularRepository.findByModeloContainingIgnoreCase("Galaxy")).thenReturn(Arrays.asList(celular));

        List<CelularResponseDTO> result = celularService.buscarPorModelo("Galaxy");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModelo()).isEqualTo("Galaxy S24 Ultra");
    }

    @Test
    void buscarPorModelo_ShouldReturnEmpty_WhenNoMatch() {
        when(celularRepository.findByModeloContainingIgnoreCase("Inexistente")).thenReturn(List.of());

        List<CelularResponseDTO> result = celularService.buscarPorModelo("Inexistente");

        assertThat(result).isEmpty();
    }

    // ==================== TESTS BUSCAR POR SISTEMA OPERATIVO ====================

    @Test
    void buscarPorSistemaOperativo_ShouldReturnCelulares() {
        when(celularRepository.findBySistemaOperativo("Android 14")).thenReturn(Arrays.asList(celular));

        List<CelularResponseDTO> result = celularService.buscarPorSistemaOperativo("Android 14");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSistemaOperativo()).isEqualTo("Android 14");
    }

    @Test
    void buscarPorSistemaOperativo_ShouldReturnEmpty_WhenNoMatch() {
        when(celularRepository.findBySistemaOperativo("iOS 99")).thenReturn(List.of());

        List<CelularResponseDTO> result = celularService.buscarPorSistemaOperativo("iOS 99");

        assertThat(result).isEmpty();
    }

    // ==================== TESTS BUSCAR LIBRES ====================

    @Test
    void buscarLibres_ShouldReturnOnlyLibreCelulares() {
        when(celularRepository.findByEsLibreTrue()).thenReturn(Arrays.asList(celular));

        List<CelularResponseDTO> result = celularService.buscarLibres();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isEsLibre()).isTrue();
    }

    @Test
    void buscarLibres_ShouldReturnEmpty_WhenNoLibres() {
        when(celularRepository.findByEsLibreTrue()).thenReturn(List.of());

        List<CelularResponseDTO> result = celularService.buscarLibres();

        assertThat(result).isEmpty();
    }

    // ==================== TESTS BUSCAR POR RANGO ALMACENAMIENTO ====================

    @Test
    void buscarPorRangoAlmacenamiento_ShouldReturnCelulares() {
        when(celularRepository.findByAlmacenamientoGBBetween(200, 300)).thenReturn(Arrays.asList(celular));

        List<CelularResponseDTO> result = celularService.buscarPorRangoAlmacenamiento(200, 300);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAlmacenamientoGB()).isEqualTo(256);
    }

    @Test
    void buscarPorRangoAlmacenamiento_ShouldThrowException_WhenMinGreaterThanMax() {
        assertThatThrownBy(() -> celularService.buscarPorRangoAlmacenamiento(300, 200))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El mínimo no puede ser mayor al máximo");
    }

    // ==================== TESTS BUSCAR CON FILTROS ====================

    @Test
    void buscarConFiltros_ShouldReturnFilteredCelulares() {
        Page<Celular> page = new PageImpl<>(Arrays.asList(celular));
        when(celularRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<CelularResponseDTO> result = celularService.buscarConFiltros(
                "Samsung", "Android 14", 1000.0, 2000.0, true, Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMarca()).isEqualTo("Samsung");
    }

    // ==================== TESTS REPONER STOCK ====================

    @Test
    void reponerStock_ShouldIncreaseStock_WhenValid() {
        celular.setStock(10);
        when(celularRepository.findById(1L)).thenReturn(Optional.of(celular));
        when(celularRepository.save(any(Celular.class))).thenReturn(celular);

        CelularResponseDTO result = celularService.reponerStock(1L, 5);

        assertThat(result).isNotNull();
        assertThat(result.getStock()).isEqualTo(15);
        verify(celularRepository).save(celular);
    }

    @Test
    void reponerStock_ShouldThrowException_WhenCantidadIsZeroOrNegative() {
        assertThatThrownBy(() -> celularService.reponerStock(1L, 0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La cantidad debe ser mayor a 0");

        assertThatThrownBy(() -> celularService.reponerStock(1L, -5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La cantidad debe ser mayor a 0");

        verify(celularRepository, never()).save(any(Celular.class));
    }

    @Test
    void reponerStock_ShouldThrowException_WhenCelularNotFound() {
        when(celularRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> celularService.reponerStock(99L, 10))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Celular no encontrado con id: 99");
    }

    // ==================== TESTS STOCK BAJO ====================

    @Test
    void stockBajo_ShouldReturnCelularesWithLowStock() {
        celular.setStock(2);
        celular.setStockMinimo(5);
        when(celularRepository.findAll()).thenReturn(Arrays.asList(celular, celular2));

        List<CelularResponseDTO> result = celularService.stockBajo();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdProducto()).isEqualTo(1L);
        assertThat(result.get(0).getStock()).isEqualTo(2);
    }

    @Test
    void stockBajo_ShouldReturnEmpty_WhenNoLowStock() {
        when(celularRepository.findAll()).thenReturn(Arrays.asList(celular, celular2));

        List<CelularResponseDTO> result = celularService.stockBajo();

        assertThat(result).isEmpty();
    }
}