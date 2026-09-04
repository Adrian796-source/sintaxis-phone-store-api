package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.ProductoResponseDTO;
import com.adrian.sintaxis.model.Producto;
import com.adrian.sintaxis.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.adrian.sintaxis.exception.ProductoNoEncontradoException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto() {
            @Override
            public String mostrarDetalles() {
                return "Test Producto";
            }
        };
        producto.setIdProducto(1L);
        producto.setNombre("iPhone 15");
        producto.setDescripcion("Smartphone");
        producto.setPrecio(1000.0);
        producto.setStock(10);
        producto.setStockMinimo(2);
        producto.setMarca("Apple");
        producto.setCategoria("Celular");
        producto.setFechaAlta(LocalDateTime.now());
        producto.setActivo(true);
    }

    @Test
    void buscarPorId_DeberiaRetornarDTO() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        Optional<ProductoResponseDTO> resultado = productoService.buscarPorId(1L);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("iPhone 15");
        assertThat(resultado.get().getPrecio()).isEqualTo(1000.0);
    }

    @Test
    void listarTodos_DeberiaRetornarListaDTO() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        // Act
        List<ProductoResponseDTO> resultado = productoService.listarTodos();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("iPhone 15");
    }

    @Test
    void buscarPorCategoria_DeberiaRetornarListaDTO() {
        // Arrange
        when(productoRepository.findByCategoria("Celular")).thenReturn(List.of(producto));

        // Act
        List<ProductoResponseDTO> resultado = productoService.buscarPorCategoria("Celular");

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCategoria()).isEqualTo("Celular");
    }

    @Test
    void buscarPorMarca_DeberiaRetornarListaDTO() {
        // Arrange
        when(productoRepository.findByMarca("Apple")).thenReturn(List.of(producto));

        // Act
        List<ProductoResponseDTO> resultado = productoService.buscarPorMarca("Apple");

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getMarca()).isEqualTo("Apple");
    }

    @Test
    void listarActivos_DeberiaRetornarListaDTO() {
        // Arrange
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(producto));

        // Act
        List<ProductoResponseDTO> resultado = productoService.listarActivos();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).isActivo()).isTrue();
    }

    @Test
    void listarConStock_DeberiaRetornarListaDTO() {
        // Arrange
        when(productoRepository.findByStockGreaterThan(0)).thenReturn(List.of(producto));

        // Act
        List<ProductoResponseDTO> resultado = productoService.listarConStock();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getStock()).isEqualTo(10);
    }

    @Test
    void reducirStock_DeberiaReducirCantidad() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        productoService.reducirStock(1L, 3);

        // Assert
        assertThat(producto.getStock()).isEqualTo(7);
    }

    @Test
    void buscarPorId_NoEncontrado_DeberiaLanzarExcepcion() {
        // Arrange
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductoNoEncontradoException.class, () -> productoService.buscarPorId(99L));
    }

}