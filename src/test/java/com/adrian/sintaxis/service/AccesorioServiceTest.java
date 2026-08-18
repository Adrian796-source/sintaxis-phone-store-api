package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.AccesorioRequestDTO;
import com.adrian.sintaxis.dto.AccesorioResponseDTO;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.model.Accesorio;
import com.adrian.sintaxis.repository.AccesorioRepository;
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
class AccesorioServiceTest {

    @Mock
    private AccesorioRepository accesorioRepository;

    @InjectMocks
    private AccesorioService accesorioService;

    private Accesorio accesorio;
    private Accesorio accesorio2;
    private AccesorioRequestDTO accesorioRequestDTO;

    @BeforeEach
    void setUp() {
        // Configurar Accesorio 1 - Funda Original
        accesorio = new Accesorio();
        accesorio.setIdProducto(1L);
        accesorio.setNombre("Funda de Silicona");
        accesorio.setDescripcion("Funda de silicona flexible para iPhone 15 Pro Max");
        accesorio.setPrecio(25.99);
        accesorio.setStock(100);
        accesorio.setStockMinimo(10);
        accesorio.setMarca("Samsung");
        accesorio.setCategoria("Accesorio");
        accesorio.setFechaAlta(LocalDateTime.now());
        accesorio.setActivo(true);
        accesorio.setTipoAccesorio("Funda");
        accesorio.setColor("Negro");
        accesorio.setMaterial("Silicona");
        accesorio.setEsOriginal(true);
        accesorio.setMarcasCompatibles(Arrays.asList("iPhone 15 Pro Max", "Samsung Galaxy S24 Ultra", "Google Pixel 8"));

        // Configurar Accesorio 2 - Cargador Compatible
        accesorio2 = new Accesorio();
        accesorio2.setIdProducto(2L);
        accesorio2.setNombre("Cargador USB-C");
        accesorio2.setDescripcion("Cargador rápido de 20W con cable USB-C");
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

        // Configurar DTO de Request
        accesorioRequestDTO = new AccesorioRequestDTO();
        accesorioRequestDTO.setNombre("Funda de Silicona");
        accesorioRequestDTO.setDescripcion("Funda de silicona flexible para iPhone 15 Pro Max");
        accesorioRequestDTO.setPrecio(25.99);
        accesorioRequestDTO.setStock(100);
        accesorioRequestDTO.setStockMinimo(10);
        accesorioRequestDTO.setMarca("Samsung");
        accesorioRequestDTO.setTipoAccesorio("Funda");
        accesorioRequestDTO.setColor("Negro");
        accesorioRequestDTO.setMaterial("Silicona");
        accesorioRequestDTO.setEsOriginal(true);
        accesorioRequestDTO.setMarcasCompatibles(Arrays.asList("iPhone 15 Pro Max", "Samsung Galaxy S24 Ultra", "Google Pixel 8"));
    }

    // ==================== TESTS GUARDAR ====================

    @Test
    void guardar_ShouldCreateAccesorio_WhenValid() {
        when(accesorioRepository.save(any(Accesorio.class))).thenReturn(accesorio);

        AccesorioResponseDTO result = accesorioService.guardar(accesorioRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getIdProducto()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Funda de Silicona");
        assertThat(result.getPrecio()).isEqualTo(25.99);
        assertThat(result.getStock()).isEqualTo(100);
        assertThat(result.getMarca()).isEqualTo("Samsung");
        assertThat(result.getTipoAccesorio()).isEqualTo("Funda");
        assertThat(result.getColor()).isEqualTo("Negro");
        assertThat(result.getMaterial()).isEqualTo("Silicona");
        assertThat(result.isEsOriginal()).isTrue();
        assertThat(result.getMarcasCompatibles()).hasSize(3);
        assertThat(result.isActivo()).isTrue();

        verify(accesorioRepository).save(any(Accesorio.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenPrecioIsInvalid() {
        accesorioRequestDTO.setPrecio(0.0);

        assertThatThrownBy(() -> accesorioService.guardar(accesorioRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El precio debe ser mayor a 0");

        verify(accesorioRepository, never()).save(any(Accesorio.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenPrecioIsNegative() {
        accesorioRequestDTO.setPrecio(-10.0);

        assertThatThrownBy(() -> accesorioService.guardar(accesorioRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El precio debe ser mayor a 0");

        verify(accesorioRepository, never()).save(any(Accesorio.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenTipoAccesorioIsBlank() {
        accesorioRequestDTO.setTipoAccesorio("");

        assertThatThrownBy(() -> accesorioService.guardar(accesorioRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El tipo de accesorio es obligatorio");

        verify(accesorioRepository, never()).save(any(Accesorio.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenTipoAccesorioIsNull() {
        accesorioRequestDTO.setTipoAccesorio(null);

        assertThatThrownBy(() -> accesorioService.guardar(accesorioRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El tipo de accesorio es obligatorio");

        verify(accesorioRepository, never()).save(any(Accesorio.class));
    }

    // ==================== TESTS BUSCAR POR ID ====================

    @Test
    void buscarPorId_ShouldReturnAccesorio_WhenExists() {
        when(accesorioRepository.findById(1L)).thenReturn(Optional.of(accesorio));

        Optional<AccesorioResponseDTO> result = accesorioService.buscarPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getIdProducto()).isEqualTo(1L);
        assertThat(result.get().getNombre()).isEqualTo("Funda de Silicona");
        assertThat(result.get().getTipoAccesorio()).isEqualTo("Funda");
    }

    @Test
    void buscarPorId_ShouldThrowException_WhenNotFound() {
        when(accesorioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accesorioService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Accesorio no encontrado con id: 99");
    }

    // ==================== TESTS LISTAR TODOS ====================

    @Test
    void listarTodos_ShouldReturnPageOfAccesorios() {
        Page<Accesorio> page = new PageImpl<>(Arrays.asList(accesorio, accesorio2));
        when(accesorioRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<AccesorioResponseDTO> result = accesorioService.listarTodos(Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getIdProducto()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getTipoAccesorio()).isEqualTo("Funda");
        assertThat(result.getContent().get(1).getIdProducto()).isEqualTo(2L);
        assertThat(result.getContent().get(1).getTipoAccesorio()).isEqualTo("Cargador");
    }

    @Test
    void listarTodos_ShouldReturnEmptyPage_WhenNoAccesorios() {
        Page<Accesorio> emptyPage = new PageImpl<>(Arrays.asList());
        when(accesorioRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<AccesorioResponseDTO> result = accesorioService.listarTodos(Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    // ==================== TESTS ACTUALIZAR ====================

    @Test
    void actualizar_ShouldUpdateAccesorio_WhenValid() {
        when(accesorioRepository.findById(1L)).thenReturn(Optional.of(accesorio));
        when(accesorioRepository.save(any(Accesorio.class))).thenReturn(accesorio);

        AccesorioResponseDTO result = accesorioService.actualizar(1L, accesorioRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getIdProducto()).isEqualTo(1L);
        verify(accesorioRepository).save(any(Accesorio.class));
    }

    @Test
    void actualizar_ShouldThrowException_WhenAccesorioNotFound() {
        when(accesorioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accesorioService.actualizar(99L, accesorioRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Accesorio no encontrado con id: 99");

        verify(accesorioRepository, never()).save(any(Accesorio.class));
    }

    @Test
    void actualizar_ShouldThrowException_WhenPrecioIsInvalid() {
        when(accesorioRepository.findById(1L)).thenReturn(Optional.of(accesorio));
        accesorioRequestDTO.setPrecio(0.0);

        assertThatThrownBy(() -> accesorioService.actualizar(1L, accesorioRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El precio debe ser mayor a 0");

        verify(accesorioRepository, never()).save(any(Accesorio.class));
    }

    // ==================== TESTS ELIMINAR ====================

    @Test
    void eliminar_ShouldSoftDeleteAccesorio_WhenExists() {
        when(accesorioRepository.findById(1L)).thenReturn(Optional.of(accesorio));
        when(accesorioRepository.save(any(Accesorio.class))).thenReturn(accesorio);

        accesorioService.eliminar(1L);

        assertThat(accesorio.isActivo()).isFalse();
        verify(accesorioRepository).save(accesorio);
    }

    @Test
    void eliminar_ShouldThrowException_WhenAccesorioNotFound() {
        when(accesorioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accesorioService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Accesorio no encontrado con id: 99");

        verify(accesorioRepository, never()).save(any(Accesorio.class));
    }

    // ==================== TESTS BUSCAR POR TIPO ====================

    @Test
    void buscarPorTipo_ShouldReturnAccesorios() {
        when(accesorioRepository.findByTipoAccesorio("Funda")).thenReturn(Arrays.asList(accesorio));

        List<AccesorioResponseDTO> result = accesorioService.buscarPorTipo("Funda");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTipoAccesorio()).isEqualTo("Funda");
    }

    @Test
    void buscarPorTipo_ShouldReturnEmpty_WhenNoMatch() {
        when(accesorioRepository.findByTipoAccesorio("Inexistente")).thenReturn(List.of());

        List<AccesorioResponseDTO> result = accesorioService.buscarPorTipo("Inexistente");

        assertThat(result).isEmpty();
    }

    // ==================== TESTS BUSCAR ORIGINALES ====================

    @Test
    void buscarOriginales_ShouldReturnOnlyOriginalAccesorios() {
        when(accesorioRepository.findByEsOriginalTrue()).thenReturn(Arrays.asList(accesorio));

        List<AccesorioResponseDTO> result = accesorioService.buscarOriginales();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isEsOriginal()).isTrue();
    }

    @Test
    void buscarOriginales_ShouldReturnEmpty_WhenNoOriginales() {
        when(accesorioRepository.findByEsOriginalTrue()).thenReturn(List.of());

        List<AccesorioResponseDTO> result = accesorioService.buscarOriginales();

        assertThat(result).isEmpty();
    }

    // ==================== TESTS BUSCAR POR MARCA COMPATIBLE ====================

    @Test
    void buscarPorMarcaCompatible_ShouldReturnAccesorios() {
        when(accesorioRepository.findByMarcasCompatiblesContaining("iPhone")).thenReturn(Arrays.asList(accesorio, accesorio2));

        List<AccesorioResponseDTO> result = accesorioService.buscarPorMarcaCompatible("iPhone");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMarcasCompatibles()).contains("iPhone 15 Pro Max");
        assertThat(result.get(1).getMarcasCompatibles()).contains("iPhone");
    }

    @Test
    void buscarPorMarcaCompatible_ShouldReturnEmpty_WhenNoMatch() {
        when(accesorioRepository.findByMarcasCompatiblesContaining("MarcaInexistente")).thenReturn(List.of());

        List<AccesorioResponseDTO> result = accesorioService.buscarPorMarcaCompatible("MarcaInexistente");

        assertThat(result).isEmpty();
    }

    // ==================== TESTS BUSCAR CON FILTROS ====================

    @Test
    void buscarConFiltros_ShouldReturnFilteredAccesorios() {
        Page<Accesorio> page = new PageImpl<>(Arrays.asList(accesorio));
        when(accesorioRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<AccesorioResponseDTO> result = accesorioService.buscarConFiltros(
                "Samsung", "Funda", 20.0, 30.0, true, Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMarca()).isEqualTo("Samsung");
        assertThat(result.getContent().get(0).getTipoAccesorio()).isEqualTo("Funda");
        assertThat(result.getContent().get(0).isEsOriginal()).isTrue();
    }

    @Test
    void buscarConFiltros_ShouldReturnEmpty_WhenNoMatch() {
        Page<Accesorio> emptyPage = new PageImpl<>(Arrays.asList());
        when(accesorioRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<AccesorioResponseDTO> result = accesorioService.buscarConFiltros(
                "Inexistente", null, null, null, null, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    // ==================== TESTS REPONER STOCK ====================

    @Test
    void reponerStock_ShouldIncreaseStock_WhenValid() {
        accesorio.setStock(10);
        when(accesorioRepository.findById(1L)).thenReturn(Optional.of(accesorio));
        when(accesorioRepository.save(any(Accesorio.class))).thenReturn(accesorio);

        AccesorioResponseDTO result = accesorioService.reponerStock(1L, 5);

        assertThat(result).isNotNull();
        assertThat(result.getStock()).isEqualTo(15);
        verify(accesorioRepository).save(accesorio);
    }

    @Test
    void reponerStock_ShouldThrowException_WhenCantidadIsZeroOrNegative() {
        assertThatThrownBy(() -> accesorioService.reponerStock(1L, 0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La cantidad debe ser mayor a 0");

        assertThatThrownBy(() -> accesorioService.reponerStock(1L, -5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La cantidad debe ser mayor a 0");

        verify(accesorioRepository, never()).save(any(Accesorio.class));
    }

    @Test
    void reponerStock_ShouldThrowException_WhenAccesorioNotFound() {
        when(accesorioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accesorioService.reponerStock(99L, 10))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Accesorio no encontrado con id: 99");
    }

    // ==================== TESTS STOCK BAJO ====================

    @Test
    void stockBajo_ShouldReturnAccesoriosWithLowStock() {
        accesorio.setStock(2);
        accesorio.setStockMinimo(10);
        when(accesorioRepository.findAll()).thenReturn(Arrays.asList(accesorio, accesorio2));

        List<AccesorioResponseDTO> result = accesorioService.stockBajo();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdProducto()).isEqualTo(1L);
        assertThat(result.get(0).getStock()).isEqualTo(2);
    }

    @Test
    void stockBajo_ShouldReturnEmpty_WhenNoLowStock() {
        when(accesorioRepository.findAll()).thenReturn(Arrays.asList(accesorio, accesorio2));

        List<AccesorioResponseDTO> result = accesorioService.stockBajo();

        assertThat(result).isEmpty();
    }

    // ==================== TESTS GUARDAR CON IMAGEN ====================

    @Test
    void guardar_ShouldCreateAccesorio_WithImagenUrl() {
        // DTO con imagen
        AccesorioRequestDTO dtoConImagen = new AccesorioRequestDTO();
        dtoConImagen.setNombre("Funda Premium");
        dtoConImagen.setDescripcion("Funda de cuero premium");
        dtoConImagen.setPrecio(49.99);
        dtoConImagen.setStock(20);
        dtoConImagen.setStockMinimo(5);
        dtoConImagen.setMarca("Apple");
        dtoConImagen.setTipoAccesorio("Funda");
        dtoConImagen.setColor("Marrón");
        dtoConImagen.setMaterial("Cuero");
        dtoConImagen.setEsOriginal(true);
        dtoConImagen.setMarcasCompatibles(Arrays.asList("iPhone 15 Pro Max"));
        dtoConImagen.setImagenUrl("/api/productos/imagenes/funda-premium.jpg");

        Accesorio accesorioConImagen = new Accesorio();
        accesorioConImagen.setIdProducto(3L);
        accesorioConImagen.setNombre("Funda Premium");
        accesorioConImagen.setDescripcion("Funda de cuero premium");
        accesorioConImagen.setPrecio(49.99);
        accesorioConImagen.setStock(20);
        accesorioConImagen.setStockMinimo(5);
        accesorioConImagen.setMarca("Apple");
        accesorioConImagen.setCategoria("Accesorio");
        accesorioConImagen.setFechaAlta(LocalDateTime.now());
        accesorioConImagen.setActivo(true);
        accesorioConImagen.setTipoAccesorio("Funda");
        accesorioConImagen.setColor("Marrón");
        accesorioConImagen.setMaterial("Cuero");
        accesorioConImagen.setEsOriginal(true);
        accesorioConImagen.setMarcasCompatibles(Arrays.asList("iPhone 15 Pro Max"));
        accesorioConImagen.setImagenUrl("/api/productos/imagenes/funda-premium.jpg");

        when(accesorioRepository.save(any(Accesorio.class))).thenReturn(accesorioConImagen);

        AccesorioResponseDTO result = accesorioService.guardar(dtoConImagen);

        assertThat(result).isNotNull();
        assertThat(result.getIdProducto()).isEqualTo(3L);
        assertThat(result.getNombre()).isEqualTo("Funda Premium");
        assertThat(result.getImagenUrl()).isEqualTo("/api/productos/imagenes/funda-premium.jpg");

        verify(accesorioRepository).save(any(Accesorio.class));
    }
}