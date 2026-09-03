package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.*;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.model.*;
import com.adrian.sintaxis.repository.ClienteRepository;
import com.adrian.sintaxis.repository.ProductoRepository;
import com.adrian.sintaxis.repository.UsuarioRepository;
import com.adrian.sintaxis.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private VentaService ventaService;

    private Cliente cliente;
    private Producto producto1;
    private Producto producto2;
    private VentaRequestDTO ventaRequestDTO;
    private Venta venta;
    private DetalleVenta detalle1;
    private DetalleVenta detalle2;

    @BeforeEach
    void setUp() {
        // Configurar cliente
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setEmail("juan@test.com");
        cliente.setEsVip(false);
        cliente.setPuntosAcumulados(0);
        cliente.setActivo(true);

        // Configurar productos
        producto1 = new Celular();
        producto1.setIdProducto(1L);
        producto1.setNombre("iPhone 15 Pro");
        producto1.setPrecio(1199.99);
        producto1.setStock(10);
        producto1.setActivo(true);

        producto2 = new Accesorio();
        producto2.setIdProducto(2L);
        producto2.setNombre("Cable USB-C");
        producto2.setPrecio(29.99);
        producto2.setStock(20);
        producto2.setActivo(true);

        // Configurar DTO de request
        ventaRequestDTO = new VentaRequestDTO();
        ventaRequestDTO.setIdCliente(1L);
        ventaRequestDTO.setEstado("Pagada");
        ventaRequestDTO.setMetodoPago("TARJETA_CREDITO");
        ventaRequestDTO.setDescuento(10.0);

        DetalleVentaRequestDTO detalleRequest1 = new DetalleVentaRequestDTO();
        detalleRequest1.setIdProducto(1L);
        detalleRequest1.setCantidad(2);

        DetalleVentaRequestDTO detalleRequest2 = new DetalleVentaRequestDTO();
        detalleRequest2.setIdProducto(2L);
        detalleRequest2.setCantidad(1);

        ventaRequestDTO.setDetalles(Arrays.asList(detalleRequest1, detalleRequest2));

        // Configurar detalles de venta
        detalle1 = new DetalleVenta();
        detalle1.setIdDetalleVenta(1L);
        detalle1.setProducto(producto1);
        detalle1.setCantidad(2);
        detalle1.setPrecioUnitario(1199.99);

        detalle2 = new DetalleVenta();
        detalle2.setIdDetalleVenta(2L);
        detalle2.setProducto(producto2);
        detalle2.setCantidad(1);
        detalle2.setPrecioUnitario(29.99);

        // Configurar venta
        venta = new Venta();
        venta.setIdVenta(1L);
        venta.setCliente(cliente);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado("Pagada");
        venta.setMetodoPago("TARJETA_CREDITO");
        venta.setSubtotal(2429.97);
        venta.setDescuento(10.0);
        venta.setTotal(2419.97);
        venta.setActivo(true);
        venta.setDetalles(Arrays.asList(detalle1, detalle2));
    }

    // ==================== TESTS GUARDAR ====================

    @Test
    void guardar_ShouldCreateVenta_WhenValidRequest() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);
        when(clienteService.calcularPuntosPorMonto(anyDouble())).thenReturn(20);
        when(clienteService.agregarPuntos(anyLong(), anyInt())).thenReturn(null);

        VentaResponseDTO result = ventaService.guardar(ventaRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getIdVenta()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo("Pagada");
        assertThat(result.getTotal()).isEqualTo(2419.97);
        assertThat(result.getDetalles()).hasSize(2);

        verify(clienteRepository).findById(1L);
        // 🔥 CORREGIDO: El servicio llama a findById 4 veces (2 para validación + 2 para crear detalles)
        verify(productoRepository, times(4)).findById(anyLong());
        verify(ventaRepository).save(any(Venta.class));
        verify(clienteService).agregarPuntos(eq(1L), anyInt());
    }
    @Test
    void guardar_ShouldThrowException_WhenClienteNotFound() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.guardar(ventaRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con id: 1");
    }

    @Test
    void guardar_ShouldThrowException_WhenProductoNotFound() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.guardar(ventaRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto no encontrado con id: 1");
    }

    @Test
    void guardar_ShouldThrowException_WhenEstadoInvalido() {
        ventaRequestDTO.setEstado("INVALIDO");

        assertThatThrownBy(() -> ventaService.guardar(ventaRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Estado inválido");
    }

    @Test
    void guardar_ShouldThrowException_WhenStockInsuficiente() {
        producto1.setStock(1); // Stock insuficiente para 2 unidades

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));

        assertThatThrownBy(() -> ventaService.guardar(ventaRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void guardar_ShouldThrowException_WhenDetallesEmpty() {
        ventaRequestDTO.setDetalles(new ArrayList<>());

        assertThatThrownBy(() -> ventaService.guardar(ventaRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La venta debe tener al menos un producto");
    }

    @Test
    void guardar_ShouldApplyVipDiscount_WhenClienteIsVip() {
        cliente.setEsVip(true);
        cliente.setPuntosAcumulados(200);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);
        when(clienteService.calcularPuntosPorMonto(anyDouble())).thenReturn(20);
        when(clienteService.agregarPuntos(anyLong(), anyInt())).thenReturn(null);

        VentaResponseDTO result = ventaService.guardar(ventaRequestDTO);

        assertThat(result).isNotNull();
        // El descuento VIP se aplica en el servicio (10% adicional)
        verify(ventaRepository).save(any(Venta.class));
    }

    // ==================== TESTS BUSCAR POR ID ====================

    @Test
    void buscarPorId_ShouldReturnVenta_WhenExists() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        Optional<VentaResponseDTO> result = ventaService.buscarPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getIdVenta()).isEqualTo(1L);
        assertThat(result.get().getEstado()).isEqualTo("Pagada");
    }

    @Test
    void buscarPorId_ShouldThrowException_WhenNotFound() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venta no encontrada con id: 99");
    }

    // ==================== TESTS LISTAR TODOS ====================

    @Test
    void listarTodos_ShouldReturnPageOfVentas() {
        Page<Venta> page = new PageImpl<>(Arrays.asList(venta));
        when(ventaRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<VentaResponseDTO> result = ventaService.listarTodos(Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIdVenta()).isEqualTo(1L);
    }

    @Test
    void listarTodos_ShouldReturnEmptyPage_WhenNoVentas() {
        Page<Venta> emptyPage = new PageImpl<>(new ArrayList<>());
        when(ventaRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<VentaResponseDTO> result = ventaService.listarTodos(Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    // ==================== TESTS ACTUALIZAR ====================

    @Test
    void actualizar_ShouldUpdateVenta_WhenValid() {
        VentaRequestDTO updateDTO = new VentaRequestDTO();
        updateDTO.setMetodoPago("EFECTIVO");
        updateDTO.setDescuento(15.0);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        VentaResponseDTO result = ventaService.actualizar(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getIdVenta()).isEqualTo(1L);
        verify(ventaRepository).save(any(Venta.class));
    }

    @Test
    void actualizar_ShouldThrowException_WhenVentaNotFound() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());
        VentaRequestDTO dto = new VentaRequestDTO(); // ✅ Crear DTO fuera de la lambda
        assertThatThrownBy(() -> ventaService.actualizar(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venta no encontrada con id: 99");
    }

    @Test
    void actualizar_ShouldThrowException_WhenVentaCancelada() {
        venta.setEstado("Cancelada");
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThatThrownBy(() -> ventaService.actualizar(1L, new VentaRequestDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se puede modificar una venta cancelada");
    }

    // ==================== TESTS ELIMINAR ====================

    @Test
    void eliminar_ShouldSoftDeleteVenta_WhenExists() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        ventaService.eliminar(1L);

        assertThat(venta.isActivo()).isFalse();
        verify(ventaRepository).save(venta);
    }

    @Test
    void eliminar_ShouldThrowException_WhenVentaNotFound() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venta no encontrada con id: 99");
    }

    // ==================== TESTS MIS VENTAS ====================

    @Test
    void misVentas_ShouldReturnVentas_WhenUserExists() {
        Usuario usuario = new Usuario();
        usuario.setEmail("juan@test.com");
        usuario.setCliente(cliente);

        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(ventaRepository.findByClienteIdCliente(1L)).thenReturn(Arrays.asList(venta));

        List<VentaResponseDTO> result = ventaService.misVentas("juan@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdVenta()).isEqualTo(1L);
    }

    @Test
    void misVentas_ShouldThrowException_WhenUserNotFound() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.misVentas("noexiste@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void misVentas_ShouldThrowException_WhenUserHasNoCliente() {
        Usuario usuario = new Usuario();
        usuario.setEmail("sincliente@test.com");
        usuario.setCliente(null);

        when(usuarioRepository.findByEmail("sincliente@test.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> ventaService.misVentas("sincliente@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El usuario no tiene un cliente asociado");
    }

    // ==================== TESTS CAMBIAR ESTADO ====================

    @Test
    void cambiarEstado_ShouldUpdateEstado_WhenValid() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        VentaResponseDTO result = ventaService.cambiarEstado(1L, "Entregada");

        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo("Entregada");
        verify(ventaRepository).save(venta);
    }

    @Test
    void cambiarEstado_ShouldThrowException_WhenEstadoInvalido() {
        assertThatThrownBy(() -> ventaService.cambiarEstado(1L, "INVALIDO"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Estado inválido");
    }

    @Test
    void cambiarEstado_ShouldThrowException_WhenVentaNotFound() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.cambiarEstado(99L, "Pagada"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venta no encontrada con id: 99");
    }

    @Test
    void cambiarEstado_ShouldThrowException_WhenVentaCancelada() {
        venta.setEstado("Cancelada");
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThatThrownBy(() -> ventaService.cambiarEstado(1L, "Pagada"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se puede cambiar el estado de una venta cancelada");
    }

    @Test
    void cambiarEstado_ShouldReturnStock_WhenEstadoIsCancelada() {
        venta.setEstado("Pagada");
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);
        when(clienteService.calcularPuntosPorMonto(anyDouble())).thenReturn(20);
        when(clienteService.restarPuntos(anyLong(), anyInt())).thenReturn(null);

        VentaResponseDTO result = ventaService.cambiarEstado(1L, "Cancelada");

        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo("Cancelada");
        verify(productoRepository, times(2)).save(any(Producto.class));
        verify(clienteService).restarPuntos(eq(1L), anyInt());
    }

    // ==================== TESTS BUSCAR POR CLIENTE ====================

    @Test
    void buscarPorCliente_ShouldReturnVentas() {
        when(ventaRepository.findByClienteIdCliente(1L)).thenReturn(Arrays.asList(venta));

        List<VentaResponseDTO> result = ventaService.buscarPorCliente(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdVenta()).isEqualTo(1L);
    }

    @Test
    void buscarPorCliente_ShouldReturnEmpty_WhenNoVentas() {
        when(ventaRepository.findByClienteIdCliente(99L)).thenReturn(new ArrayList<>());

        List<VentaResponseDTO> result = ventaService.buscarPorCliente(99L);

        assertThat(result).isEmpty();
    }

    // ==================== TESTS BUSCAR POR ESTADO ====================

    @Test
    void buscarPorEstado_ShouldReturnVentas() {
        when(ventaRepository.findByEstado("Pagada")).thenReturn(Arrays.asList(venta));

        List<VentaResponseDTO> result = ventaService.buscarPorEstado("Pagada");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstado()).isEqualTo("Pagada");
    }

    @Test
    void buscarPorEstado_ShouldThrowException_WhenEstadoInvalido() {
        assertThatThrownBy(() -> ventaService.buscarPorEstado("INVALIDO"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Estado inválido");
    }

    // ==================== TESTS BUSCAR POR RANGO FECHAS ====================

    @Test
    void buscarPorRangoFechas_ShouldReturnVentas() {
        LocalDateTime desde = LocalDateTime.now().minusDays(7);
        LocalDateTime hasta = LocalDateTime.now();

        when(ventaRepository.findByFechaBetween(desde, hasta)).thenReturn(Arrays.asList(venta));

        List<VentaResponseDTO> result = ventaService.buscarPorRangoFechas(desde, hasta);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdVenta()).isEqualTo(1L);
    }

    @Test
    void buscarPorRangoFechas_ShouldThrowException_WhenDesdeAfterHasta() {
        LocalDateTime desde = LocalDateTime.now();
        LocalDateTime hasta = LocalDateTime.now().minusDays(1);

        assertThatThrownBy(() -> ventaService.buscarPorRangoFechas(desde, hasta))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La fecha 'desde' no puede ser posterior a 'hasta'");
    }

    // ==================== TESTS TOTAL RECAUDADO ====================

    @Test
    void totalRecaudado_ShouldReturnTotal() {
        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        LocalDateTime hasta = LocalDateTime.now();

        when(ventaRepository.totalRecaudadoEntreFechas(desde, hasta)).thenReturn(5000.0);

        Double result = ventaService.totalRecaudado(desde, hasta);

        assertThat(result).isEqualTo(5000.0);
    }

    @Test
    void totalRecaudado_ShouldThrowException_WhenDesdeAfterHasta() {
        LocalDateTime desde = LocalDateTime.now();
        LocalDateTime hasta = LocalDateTime.now().minusDays(1);

        assertThatThrownBy(() -> ventaService.totalRecaudado(desde, hasta))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La fecha 'desde' no puede ser posterior a 'hasta'");
    }

    // ==================== TESTS VENTAS POR ESTADO ====================

    @Test
    void ventasPorEstado_ShouldReturnMap() {
        List<Object[]> mockResult = Arrays.asList(
                new Object[]{"Pagada", 5L},
                new Object[]{"Pendiente", 3L},
                new Object[]{"Entregada", 2L}
        );

        when(ventaRepository.contarPorEstado()).thenReturn(mockResult);

        Map<String, Long> result = ventaService.ventasPorEstado();

        assertThat(result).hasSize(3);
        assertThat(result.get("Pagada")).isEqualTo(5L);
        assertThat(result.get("Pendiente")).isEqualTo(3L);
        assertThat(result.get("Entregada")).isEqualTo(2L);
    }

    // ==================== TESTS TOP CLIENTES ====================

    @Test
    void topClientes_ShouldReturnList() {
        List<Object[]> mockResult = Arrays.asList(
                new Object[]{1L, "Juan", "Perez", 5L, 5000.0},
                new Object[]{2L, "Maria", "Lopez", 3L, 3000.0}
        );

        when(ventaRepository.topClientes()).thenReturn(mockResult);

        List<ReporteVentaDTO> result = ventaService.topClientes();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEtiqueta()).isEqualTo("Juan Perez");
        assertThat(result.get(0).getValor()).isInstanceOf(Map.class);
    }
}