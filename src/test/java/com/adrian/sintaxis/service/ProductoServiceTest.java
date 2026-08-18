package com.adrian.sintaxis.service;

import com.adrian.sintaxis.model.Producto;
import com.adrian.sintaxis.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private String tempUploadPath;

    // ✅ Clase concreta para testing (extiende Producto)
    static class ProductoTest extends Producto {
        @Override
        public String mostrarDetalles() {
            return "Producto Test: " + getNombre();
        }
    }

    private ProductoTest producto;
    private ProductoTest producto2;

    @BeforeEach
    void setUp() {

        // ✅ Obtener el directorio temporal del sistema y asegurar que tenga separador
        String tempDir = System.getProperty("java.io.tmpdir");
        // ✅ Asegurar que termine con separador
        if (!tempDir.endsWith("/") && !tempDir.endsWith("\\")) {
            tempDir += "/";
        }
        tempUploadPath = tempDir + "uploads/";

        // ✅ Inyectar el path en el servicio
        ReflectionTestUtils.setField(productoService, "uploadPath", tempUploadPath);

        // ✅ Crear directorio con permisos
        try {
            Path uploadPath = Paths.get(tempUploadPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            System.out.println("✅ Upload path creado: " + tempUploadPath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads: " + e.getMessage(), e);
        }


        producto = new ProductoTest();
        producto.setIdProducto(1L);
        producto.setNombre("Producto Test 1");
        producto.setDescripcion("Descripción del producto 1");
        producto.setPrecio(100.0);
        producto.setStock(10);
        producto.setMarca("Marca Test");
        producto.setCategoria("Categoria Test");
        producto.setActivo(true);
        producto.setFechaAlta(LocalDateTime.now());

        producto2 = new ProductoTest();
        producto2.setIdProducto(2L);
        producto2.setNombre("Producto Test 2");
        producto2.setPrecio(200.0);
        producto2.setStock(20);
        producto2.setActivo(true);
    }

    // ==================== TESTS GUARDAR ====================

    @Test
    void guardar_ShouldCreateProducto_WhenValid() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        Producto result = productoService.guardar(producto);

        assertThat(result).isNotNull();
        assertThat(result.getIdProducto()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Producto Test 1");
        assertThat(result.isActivo()).isTrue();
        assertThat(result.getFechaAlta()).isNotNull();

        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenPrecioIsNull() {
        producto.setPrecio(null);

        assertThatThrownBy(() -> productoService.guardar(producto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El precio debe ser mayor a 0");

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenPrecioIsZeroOrNegative() {
        producto.setPrecio(0.0);

        assertThatThrownBy(() -> productoService.guardar(producto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El precio debe ser mayor a 0");

        producto.setPrecio(-10.0);
        assertThatThrownBy(() -> productoService.guardar(producto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El precio debe ser mayor a 0");

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenStockIsNegative() {
        producto.setStock(-5);

        assertThatThrownBy(() -> productoService.guardar(producto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El stock no puede ser negativo");

        verify(productoRepository, never()).save(any(Producto.class));
    }

    // ==================== TESTS BUSCAR POR ID ====================

    @Test
    void buscarPorId_ShouldReturnProducto_WhenExists() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Optional<Producto> result = productoService.buscarPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getIdProducto()).isEqualTo(1L);
        assertThat(result.get().getNombre()).isEqualTo("Producto Test 1");
    }

    @Test
    void buscarPorId_ShouldReturnEmpty_WhenNotFound() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Producto> result = productoService.buscarPorId(99L);

        assertThat(result).isEmpty();
    }

    // ==================== TESTS LISTAR TODOS ====================

    @Test
    void listarTodos_ShouldReturnAllProductos() {
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto, producto2));

        List<Producto> result = productoService.listarTodos();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIdProducto()).isEqualTo(1L);
        assertThat(result.get(1).getIdProducto()).isEqualTo(2L);
    }

    @Test
    void listarTodos_ShouldReturnEmpty_WhenNoProductos() {
        when(productoRepository.findAll()).thenReturn(List.of());

        List<Producto> result = productoService.listarTodos();

        assertThat(result).isEmpty();
    }

    // ==================== TESTS ACTUALIZAR ====================

    @Test
    void actualizar_ShouldUpdateProducto_WhenValid() {
        ProductoTest productoActualizado = new ProductoTest();
        productoActualizado.setNombre("Producto Actualizado");
        productoActualizado.setDescripcion("Nueva descripción");
        productoActualizado.setPrecio(150.0);
        productoActualizado.setStock(15);
        productoActualizado.setMarca("Nueva Marca");
        productoActualizado.setCategoria("Nueva Categoria");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        Producto result = productoService.actualizar(1L, productoActualizado);

        assertThat(result).isNotNull();
        assertThat(result.getIdProducto()).isEqualTo(1L);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void actualizar_ShouldThrowException_WhenProductoNotFound() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.actualizar(99L, producto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Producto no encontrado");

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void actualizar_ShouldThrowException_WhenPrecioIsInvalid() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        producto.setPrecio(-10.0);

        assertThatThrownBy(() -> productoService.actualizar(1L, producto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El precio debe ser mayor a 0");

        verify(productoRepository, never()).save(any(Producto.class));
    }

    // ==================== TESTS ELIMINAR ====================

    @Test
    void eliminar_ShouldSoftDeleteProducto_WhenExists() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        productoService.eliminar(1L);

        assertThat(producto.isActivo()).isFalse();
        verify(productoRepository).save(producto);
    }

    @Test
    void eliminar_ShouldThrowException_WhenProductoNotFound() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.eliminar(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Producto no encontrado");

        verify(productoRepository, never()).save(any(Producto.class));
    }

    // ==================== TESTS BUSCAR POR CATEGORIA ====================

    @Test
    void buscarPorCategoria_ShouldReturnProductos() {
        when(productoRepository.findByCategoria("Categoria Test")).thenReturn(Arrays.asList(producto));

        List<Producto> result = productoService.buscarPorCategoria("Categoria Test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoria()).isEqualTo("Categoria Test");
    }

    @Test
    void buscarPorCategoria_ShouldReturnEmpty_WhenNoMatch() {
        when(productoRepository.findByCategoria("Inexistente")).thenReturn(List.of());

        List<Producto> result = productoService.buscarPorCategoria("Inexistente");

        assertThat(result).isEmpty();
    }

    // ==================== TESTS BUSCAR POR MARCA ====================

    @Test
    void buscarPorMarca_ShouldReturnProductos() {
        when(productoRepository.findByMarca("Marca Test")).thenReturn(Arrays.asList(producto));

        List<Producto> result = productoService.buscarPorMarca("Marca Test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMarca()).isEqualTo("Marca Test");
    }

    @Test
    void buscarPorMarca_ShouldReturnEmpty_WhenNoMatch() {
        when(productoRepository.findByMarca("Inexistente")).thenReturn(List.of());

        List<Producto> result = productoService.buscarPorMarca("Inexistente");

        assertThat(result).isEmpty();
    }

    // ==================== TESTS LISTAR ACTIVOS ====================

    @Test
    void listarActivos_ShouldReturnOnlyActiveProductos() {
        when(productoRepository.findByActivoTrue()).thenReturn(Arrays.asList(producto));

        List<Producto> result = productoService.listarActivos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActivo()).isTrue();
    }

    @Test
    void listarActivos_ShouldReturnEmpty_WhenNoActiveProductos() {
        when(productoRepository.findByActivoTrue()).thenReturn(List.of());

        List<Producto> result = productoService.listarActivos();

        assertThat(result).isEmpty();
    }

    // ==================== TESTS LISTAR CON STOCK ====================

    @Test
    void listarConStock_ShouldReturnProductosWithStock() {
        when(productoRepository.findByStockGreaterThan(0)).thenReturn(Arrays.asList(producto, producto2));

        List<Producto> result = productoService.listarConStock();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStock()).isGreaterThan(0);
    }

    @Test
    void listarConStock_ShouldReturnEmpty_WhenNoStock() {
        when(productoRepository.findByStockGreaterThan(0)).thenReturn(List.of());

        List<Producto> result = productoService.listarConStock();

        assertThat(result).isEmpty();
    }

    // ==================== TESTS REDUCIR STOCK ====================

    @Test
    void reducirStock_ShouldReduceStock_WhenValid() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        productoService.reducirStock(1L, 3);

        assertThat(producto.getStock()).isEqualTo(7); // 10 - 3
        verify(productoRepository).save(producto);
    }

    @Test
    void reducirStock_ShouldThrowException_WhenCantidadIsZeroOrNegative() {
        assertThatThrownBy(() -> productoService.reducirStock(1L, 0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La cantidad debe ser mayor a 0");

        assertThatThrownBy(() -> productoService.reducirStock(1L, -5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La cantidad debe ser mayor a 0");

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void reducirStock_ShouldThrowException_WhenProductoNotFound() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.reducirStock(99L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Producto no encontrado");

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void reducirStock_ShouldThrowException_WhenStockInsufficient() {
        producto.setStock(2);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> productoService.reducirStock(1L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(productoRepository, never()).save(any(Producto.class));
    }

    // ==================== TESTS GUARDAR IMAGEN ====================

    @Test
    void guardarImagen_ShouldSaveImage_WhenValid() throws Exception {
        // ✅ Verificar que uploadPath existe
        String uploadPath = (String) ReflectionTestUtils.getField(productoService, "uploadPath");
        System.out.println("Upload path: " + uploadPath);
        assertThat(Files.exists(Paths.get(uploadPath))).isTrue();

        // ✅ Crear MockMultipartFile
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        String result = productoService.guardarImagen(imagen);

        assertThat(result).isNotNull();
        assertThat(result).startsWith("/api/productos/imagenes/");
        assertThat(result).endsWith(".jpg");

        // ✅ Verificar que el archivo se creó
        String nombreArchivo = result.substring(result.lastIndexOf("/") + 1);
        Path path = Paths.get(uploadPath, nombreArchivo);
        assertThat(Files.exists(path)).isTrue();

        // ✅ Limpiar
        Files.deleteIfExists(path);
    }

    @Test
    void guardarImagen_ShouldReturnNull_WhenImageIsNull() {
        String result = productoService.guardarImagen(null);
        assertThat(result).isNull();
    }

    @Test
    void guardarImagen_ShouldReturnNull_WhenImageIsEmpty() {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen",
                "",
                "image/jpeg",
                new byte[0]
        );
        String result = productoService.guardarImagen(imagen);
        assertThat(result).isNull();
    }

    @Test
    void guardarImagen_ShouldThrowException_WhenNotImage() {
        MockMultipartFile imagen = new MockMultipartFile(
                "archivo",
                "test.txt",
                "text/plain",
                "text content".getBytes()
        );
        assertThatThrownBy(() -> productoService.guardarImagen(imagen))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El archivo debe ser una imagen");
    }

    // ==================== TESTS OBTENER IMAGEN ====================

    @Test
    void obtenerImagen_ShouldReturnImage_WhenExists() throws Exception {
        String uploadPath = (String) ReflectionTestUtils.getField(productoService, "uploadPath");

        // Primero guardar una imagen
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        String url = productoService.guardarImagen(imagen);
        String nombreArchivo = url.substring(url.lastIndexOf("/") + 1);

        // Luego obtenerla
        ResponseEntity<Resource> result = productoService.obtenerImagen(nombreArchivo);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCodeValue()).isEqualTo(200);

        // Limpiar
        Path path = Paths.get(uploadPath, nombreArchivo);
        Files.deleteIfExists(path);
    }

    @Test
    void obtenerImagen_ShouldReturnNotFound_WhenNotExists() {
        ResponseEntity<Resource> result = productoService.obtenerImagen("noexiste.jpg");
        assertThat(result.getStatusCodeValue()).isEqualTo(404);
    }

    }
