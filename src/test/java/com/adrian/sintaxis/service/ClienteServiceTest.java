package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.*;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.model.Cliente;
import com.adrian.sintaxis.model.ConfiguracionPuntos;
import com.adrian.sintaxis.model.Venta;
import com.adrian.sintaxis.repository.ClienteRepository;
import com.adrian.sintaxis.repository.ConfiguracionPuntosRepository;
import com.adrian.sintaxis.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ConfiguracionPuntosRepository configuracionPuntosRepository;

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private ClienteRequestDTO clienteRequestDTO;
    private ConfiguracionPuntos configuracionPuntos;
    private Venta venta;

    @BeforeEach
    void setUp() {
        // Configurar cliente
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setEmail("juan@test.com");
        cliente.setTelefono("123456789");
        cliente.setDireccion("Calle Falsa 123");
        cliente.setFechaRegistro(LocalDateTime.now());
        cliente.setEsVip(false);
        cliente.setPuntosAcumulados(0);
        cliente.setActivo(true);

        // Configurar DTO de request
        clienteRequestDTO = new ClienteRequestDTO();
        clienteRequestDTO.setNombre("Juan");
        clienteRequestDTO.setApellido("Perez");
        clienteRequestDTO.setEmail("juan@test.com");
        clienteRequestDTO.setTelefono("123456789");
        clienteRequestDTO.setDireccion("Calle Falsa 123");

        // Configurar configuración de puntos
        configuracionPuntos = new ConfiguracionPuntos();
        configuracionPuntos.setId(1L);
        configuracionPuntos.setPuntosParaVip(100);
        configuracionPuntos.setPesosPorPunto(10.0);

        // Configurar venta para historial
        venta = new Venta();
        venta.setIdVenta(1L);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado("Pagada");
        venta.setMetodoPago("TARJETA_CREDITO");
        venta.setSubtotal(1000.0);
        venta.setDescuento(0.0);
        venta.setTotal(1000.0);
        venta.setActivo(true);
        venta.setCliente(cliente);
    }

    // ==================== TESTS GUARDAR ====================

    @Test
    void guardar_ShouldCreateCliente_WhenValid() {
        when(clienteRepository.findByEmail("juan@test.com")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteResponseDTO result = clienteService.guardar(clienteRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getIdCliente()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Juan");
        assertThat(result.getApellido()).isEqualTo("Perez");
        assertThat(result.getEmail()).isEqualTo("juan@test.com");
        assertThat(result.getTelefono()).isEqualTo("123456789");
        assertThat(result.getDireccion()).isEqualTo("Calle Falsa 123");
        assertThat(result.isEsVip()).isFalse();
        assertThat(result.getPuntosAcumulados()).isZero();
        assertThat(result.isActivo()).isTrue();
        assertThat(result.getFechaRegistro()).isNotNull();

        verify(clienteRepository).findByEmail("juan@test.com");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void guardar_ShouldThrowException_WhenEmailExists() {
        when(clienteRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> clienteService.guardar(clienteRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un cliente con el email: juan@test.com");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    // ==================== TESTS BUSCAR POR ID ====================

    @Test
    void buscarPorId_ShouldReturnCliente_WhenExists() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Optional<ClienteResponseDTO> result = clienteService.buscarPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getIdCliente()).isEqualTo(1L);
        assertThat(result.get().getNombre()).isEqualTo("Juan");
        assertThat(result.get().getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    void buscarPorId_ShouldThrowException_WhenNotFound() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con id: 99");
    }

    // ==================== TESTS LISTAR TODOS ====================

    @Test
    void listarTodos_ShouldReturnAllClientes() {
        Cliente cliente2 = new Cliente();
        cliente2.setIdCliente(2L);
        cliente2.setNombre("Maria");
        cliente2.setApellido("Lopez");
        cliente2.setEmail("maria@test.com");
        cliente2.setTelefono("987654321");
        cliente2.setDireccion("Avenida Siempre Viva 742");
        cliente2.setFechaRegistro(LocalDateTime.now());
        cliente2.setEsVip(false);
        cliente2.setPuntosAcumulados(0);
        cliente2.setActivo(true);

        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente, cliente2));

        List<ClienteResponseDTO> result = clienteService.listarTodos();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIdCliente()).isEqualTo(1L);
        assertThat(result.get(0).getNombre()).isEqualTo("Juan");
        assertThat(result.get(1).getIdCliente()).isEqualTo(2L);
        assertThat(result.get(1).getNombre()).isEqualTo("Maria");

        verify(clienteRepository).findAll();
    }

    @Test
    void listarTodos_ShouldReturnEmpty_WhenNoClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of());

        List<ClienteResponseDTO> result = clienteService.listarTodos();

        assertThat(result).isEmpty();
        verify(clienteRepository).findAll();
    }

    // ==================== TESTS ACTUALIZAR ====================

    @Test
    void actualizar_ShouldUpdateCliente_WhenValid() {
        ClienteRequestDTO updateDTO = new ClienteRequestDTO();
        updateDTO.setNombre("Juan Carlos");
        updateDTO.setApellido("Perez Garcia");
        updateDTO.setEmail("juan@test.com");
        updateDTO.setTelefono("987654321");
        updateDTO.setDireccion("Nueva Direccion 456");

        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setIdCliente(1L);
        clienteActualizado.setNombre("Juan Carlos");
        clienteActualizado.setApellido("Perez Garcia");
        clienteActualizado.setEmail("juan@test.com");
        clienteActualizado.setTelefono("987654321");
        clienteActualizado.setDireccion("Nueva Direccion 456");
        clienteActualizado.setFechaRegistro(cliente.getFechaRegistro());
        clienteActualizado.setEsVip(false);
        clienteActualizado.setPuntosAcumulados(0);
        clienteActualizado.setActivo(true);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        ClienteResponseDTO result = clienteService.actualizar(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getIdCliente()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Juan Carlos");
        assertThat(result.getApellido()).isEqualTo("Perez Garcia");
        assertThat(result.getTelefono()).isEqualTo("987654321");
        assertThat(result.getDireccion()).isEqualTo("Nueva Direccion 456");

        verify(clienteRepository).findById(1L);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void actualizar_ShouldThrowException_WhenClienteNotFound() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.actualizar(99L, clienteRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con id: 99");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void actualizar_ShouldThrowException_WhenEmailInUse() {
        Cliente otroCliente = new Cliente();
        otroCliente.setIdCliente(2L);
        otroCliente.setNombre("Otro");
        otroCliente.setApellido("Usuario");
        otroCliente.setEmail("otro@test.com");

        ClienteRequestDTO updateDTO = new ClienteRequestDTO();
        updateDTO.setNombre("Juan");
        updateDTO.setApellido("Perez");
        updateDTO.setEmail("otro@test.com");
        updateDTO.setTelefono("123456789");
        updateDTO.setDireccion("Calle Falsa 123");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByEmail("otro@test.com")).thenReturn(Optional.of(otroCliente));

        assertThatThrownBy(() -> clienteService.actualizar(1L, updateDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El email ya está en uso por otro cliente");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void actualizar_ShouldAllowSameEmail_WhenUpdatingSameCliente() {
        ClienteRequestDTO updateDTO = new ClienteRequestDTO();
        updateDTO.setNombre("Juan Carlos");
        updateDTO.setApellido("Perez");
        updateDTO.setEmail("juan@test.com");
        updateDTO.setTelefono("123456789");
        updateDTO.setDireccion("Calle Falsa 123");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteResponseDTO result = clienteService.actualizar(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getIdCliente()).isEqualTo(1L);

        verify(clienteRepository).save(any(Cliente.class));
    }

    // ==================== TESTS ELIMINAR ====================

    @Test
    void eliminar_ShouldSoftDeleteCliente_WhenExists() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        clienteService.eliminar(1L);

        assertThat(cliente.isActivo()).isFalse();
        verify(clienteRepository).save(cliente);
    }

    @Test
    void eliminar_ShouldThrowException_WhenClienteNotFound() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con id: 99");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    // ==================== TESTS BUSCAR POR NOMBRE ====================

    @Test
    void buscarPorNombre_ShouldReturnClientes() {
        when(clienteRepository.findByNombreContainingIgnoreCase("Juan")).thenReturn(Arrays.asList(cliente));

        List<ClienteResponseDTO> result = clienteService.buscarPorNombre("Juan");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdCliente()).isEqualTo(1L);
        assertThat(result.get(0).getNombre()).isEqualTo("Juan");

        verify(clienteRepository).findByNombreContainingIgnoreCase("Juan");
    }

    @Test
    void buscarPorNombre_ShouldReturnMultipleClientes_WhenMatches() {
        Cliente cliente2 = new Cliente();
        cliente2.setIdCliente(2L);
        cliente2.setNombre("Juana");
        cliente2.setApellido("Garcia");
        cliente2.setEmail("juana@test.com");

        when(clienteRepository.findByNombreContainingIgnoreCase("Juan")).thenReturn(Arrays.asList(cliente, cliente2));

        List<ClienteResponseDTO> result = clienteService.buscarPorNombre("Juan");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNombre()).isEqualTo("Juan");
        assertThat(result.get(1).getNombre()).isEqualTo("Juana");
    }

    @Test
    void buscarPorNombre_ShouldReturnEmpty_WhenNoMatch() {
        when(clienteRepository.findByNombreContainingIgnoreCase("Inexistente")).thenReturn(List.of());

        List<ClienteResponseDTO> result = clienteService.buscarPorNombre("Inexistente");

        assertThat(result).isEmpty();
        verify(clienteRepository).findByNombreContainingIgnoreCase("Inexistente");
    }

    // ==================== TESTS BUSCAR POR EMAIL ====================

    @Test
    void buscarPorEmail_ShouldReturnCliente_WhenExists() {
        when(clienteRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(cliente));

        Optional<ClienteResponseDTO> result = clienteService.buscarPorEmail("juan@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getIdCliente()).isEqualTo(1L);
        assertThat(result.get().getEmail()).isEqualTo("juan@test.com");

        verify(clienteRepository).findByEmail("juan@test.com");
    }

    @Test
    void buscarPorEmail_ShouldReturnEmpty_WhenNotFound() {
        when(clienteRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        Optional<ClienteResponseDTO> result = clienteService.buscarPorEmail("noexiste@test.com");

        assertThat(result).isEmpty();
        verify(clienteRepository).findByEmail("noexiste@test.com");
    }

    // ==================== TESTS LISTAR VIP ====================

    @Test
    void listarVip_ShouldReturnVipClientes() {
        cliente.setEsVip(true);
        Cliente cliente2 = new Cliente();
        cliente2.setIdCliente(2L);
        cliente2.setNombre("Maria");
        cliente2.setApellido("Lopez");
        cliente2.setEmail("maria@test.com");
        cliente2.setEsVip(false);

        when(clienteRepository.findByEsVipTrue()).thenReturn(Arrays.asList(cliente));

        List<ClienteResponseDTO> result = clienteService.listarVip();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdCliente()).isEqualTo(1L);
        assertThat(result.get(0).isEsVip()).isTrue();

        verify(clienteRepository).findByEsVipTrue();
    }

    @Test
    void listarVip_ShouldReturnEmpty_WhenNoVipClientes() {
        when(clienteRepository.findByEsVipTrue()).thenReturn(List.of());

        List<ClienteResponseDTO> result = clienteService.listarVip();

        assertThat(result).isEmpty();
        verify(clienteRepository).findByEsVipTrue();
    }

    // ==================== TESTS AGREGAR PUNTOS ====================

    @Test
    void agregarPuntos_ShouldAddPoints_WhenValid() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(configuracionPuntosRepository.findById(1L)).thenReturn(Optional.of(configuracionPuntos));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteResponseDTO result = clienteService.agregarPuntos(1L, 50);

        assertThat(result).isNotNull();
        assertThat(result.getPuntosAcumulados()).isEqualTo(50);
        assertThat(result.isEsVip()).isFalse();

        verify(clienteRepository).findById(1L);
        verify(configuracionPuntosRepository).findById(1L);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void agregarPuntos_ShouldMakeVip_WhenReachesThreshold() {
        cliente.setPuntosAcumulados(80);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(configuracionPuntosRepository.findById(1L)).thenReturn(Optional.of(configuracionPuntos));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteResponseDTO result = clienteService.agregarPuntos(1L, 30);

        assertThat(result).isNotNull();
        assertThat(result.getPuntosAcumulados()).isEqualTo(110);
        assertThat(result.isEsVip()).isTrue();

        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void agregarPuntos_ShouldThrowException_WhenPointsZero() {
        assertThatThrownBy(() -> clienteService.agregarPuntos(1L, 0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Los puntos deben ser un valor positivo");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void agregarPuntos_ShouldThrowException_WhenPointsNegative() {
        assertThatThrownBy(() -> clienteService.agregarPuntos(1L, -10))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Los puntos deben ser un valor positivo");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void agregarPuntos_ShouldThrowException_WhenClienteNotFound() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.agregarPuntos(99L, 50))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con id: 99");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    // ==================== TESTS RESTAR PUNTOS ====================


    @Test
    void restarPuntos_ShouldRemoveVipStatus_WhenBelowThreshold() {
        cliente.setPuntosAcumulados(90);
        cliente.setEsVip(true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(configuracionPuntosRepository.findById(1L)).thenReturn(Optional.of(configuracionPuntos));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteResponseDTO result = clienteService.restarPuntos(1L, 20);

        assertThat(result).isNotNull();
        assertThat(result.getPuntosAcumulados()).isEqualTo(70);
        assertThat(result.isEsVip()).isFalse();

        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void restarPuntos_ShouldThrowException_WhenClienteNotFound() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.restarPuntos(99L, 10))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con id: 99");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    // ==================== TESTS CALCULAR PUNTOS POR MONTO ====================

    @ParameterizedTest
    @CsvSource({
            "150.0, 15",
            "0.0, 0",
            "9999.99, 999"
    })
    void calcularPuntosPorMonto_ShouldCalculateCorrectly(double monto, int puntosEsperados) {
        when(configuracionPuntosRepository.findById(1L)).thenReturn(Optional.of(configuracionPuntos));
        int puntos = clienteService.calcularPuntosPorMonto(monto);
        assertThat(puntos).isEqualTo(puntosEsperados);
    }

    // ==================== TESTS OBTENER PERFIL CON HISTORIAL ====================

    @Test
    void obtenerPerfilConHistorial_ShouldReturnProfile_WhenClienteExists() {
        when(clienteRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(cliente));
        when(ventaRepository.findByClienteIdCliente(1L)).thenReturn(Arrays.asList(venta));

        PerfilConHistorialDTO result = clienteService.obtenerPerfilConHistorial("juan@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getIdCliente()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Juan");
        assertThat(result.getApellido()).isEqualTo("Perez");
        assertThat(result.getEmail()).isEqualTo("juan@test.com");
        assertThat(result.getTelefono()).isEqualTo("123456789");
        assertThat(result.getDireccion()).isEqualTo("Calle Falsa 123");
        assertThat(result.isEsVip()).isFalse();
        assertThat(result.getPuntosAcumulados()).isZero();
        assertThat(result.getHistorialCompras()).hasSize(1);
        assertThat(result.getHistorialCompras().get(0).getIdVenta()).isEqualTo(1L);

        verify(clienteRepository).findByEmail("juan@test.com");
        verify(ventaRepository).findByClienteIdCliente(1L);
    }

    @Test
    void obtenerPerfilConHistorial_ShouldReturnEmptyHistory_WhenNoVentas() {
        when(clienteRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(cliente));
        when(ventaRepository.findByClienteIdCliente(1L)).thenReturn(List.of());

        PerfilConHistorialDTO result = clienteService.obtenerPerfilConHistorial("juan@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getHistorialCompras()).isEmpty();

        verify(ventaRepository).findByClienteIdCliente(1L);
    }

    @Test
    void obtenerPerfilConHistorial_ShouldThrowException_WhenClienteNotFound() {
        when(clienteRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPerfilConHistorial("noexiste@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con email: noexiste@test.com");

        verify(ventaRepository, never()).findByClienteIdCliente(anyLong());
    }

    // ==================== TESTS OBTENER CONFIGURACION ====================

    @Test
    void obtenerConfiguracion_ShouldReturnConfig_WhenExists() {
        when(configuracionPuntosRepository.findById(1L)).thenReturn(Optional.of(configuracionPuntos));

        ConfiguracionPuntosDTO result = clienteService.obtenerConfiguracion();

        assertThat(result).isNotNull();
        assertThat(result.getPuntosParaVip()).isEqualTo(100);
        assertThat(result.getPesosPorPunto()).isEqualTo(10.0);

        verify(configuracionPuntosRepository).findById(1L);
    }

    @Test
    void obtenerConfiguracion_ShouldCreateDefaultConfig_WhenNotExists() {
        when(configuracionPuntosRepository.findById(1L)).thenReturn(Optional.empty());
        when(configuracionPuntosRepository.save(any(ConfiguracionPuntos.class)))
                .thenReturn(configuracionPuntos);

        ConfiguracionPuntosDTO result = clienteService.obtenerConfiguracion();

        assertThat(result).isNotNull();
        assertThat(result.getPuntosParaVip()).isEqualTo(100);
        assertThat(result.getPesosPorPunto()).isEqualTo(10.0);

        verify(configuracionPuntosRepository).findById(1L);
        verify(configuracionPuntosRepository).save(any(ConfiguracionPuntos.class));
    }

    // ==================== TESTS ACTUALIZAR CONFIGURACION ====================

    @Test
    void actualizarConfiguracion_ShouldUpdateConfig_WhenExists() {
        ConfiguracionPuntosDTO updateDTO = new ConfiguracionPuntosDTO();
        updateDTO.setPuntosParaVip(200);
        updateDTO.setPesosPorPunto(5.0);

        when(configuracionPuntosRepository.findById(1L)).thenReturn(Optional.of(configuracionPuntos));
        when(configuracionPuntosRepository.save(any(ConfiguracionPuntos.class)))
                .thenReturn(configuracionPuntos);

        ConfiguracionPuntosDTO result = clienteService.actualizarConfiguracion(updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getPuntosParaVip()).isEqualTo(200);
        assertThat(result.getPesosPorPunto()).isEqualTo(5.0);

        verify(configuracionPuntosRepository).findById(1L);
        verify(configuracionPuntosRepository).save(any(ConfiguracionPuntos.class));
    }

    @Test
    void restarPuntos_ShouldSubtractPoints_WhenValid() {
        cliente.setPuntosAcumulados(100);
        cliente.setEsVip(true);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(configuracionPuntosRepository.findById(1L)).thenReturn(Optional.of(configuracionPuntos));

        // 🔥 Crear un cliente actualizado con los nuevos valores
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setIdCliente(1L);
        clienteActualizado.setNombre("Juan");
        clienteActualizado.setApellido("Perez");
        clienteActualizado.setEmail("juan@test.com");
        clienteActualizado.setPuntosAcumulados(70);
        clienteActualizado.setEsVip(false); // 🔥 Ya no es VIP porque 70 < 100
        clienteActualizado.setActivo(true);

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        ClienteResponseDTO result = clienteService.restarPuntos(1L, 30);

        assertThat(result).isNotNull();
        assertThat(result.getPuntosAcumulados()).isEqualTo(70);
        assertThat(result.isEsVip()).isFalse(); // 🔥 CORREGIDO: 70 < 100, no es VIP

        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void restarPuntos_ShouldNotGoBelowZero() {
        cliente.setPuntosAcumulados(20);
        cliente.setEsVip(false);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));


        // Crear cliente actualizado
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setIdCliente(1L);
        clienteActualizado.setNombre("Juan");
        clienteActualizado.setApellido("Perez");
        clienteActualizado.setEmail("juan@test.com");
        clienteActualizado.setPuntosAcumulados(0);
        clienteActualizado.setEsVip(false);
        clienteActualizado.setActivo(true);

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        ClienteResponseDTO result = clienteService.restarPuntos(1L, 50);

        assertThat(result).isNotNull();
        assertThat(result.getPuntosAcumulados()).isEqualTo(0);
        assertThat(result.isEsVip()).isFalse();

        // ✅ Verificar que el mock NO fue invocado
        verify(configuracionPuntosRepository, never()).findById(1L);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void actualizarConfiguracion_ShouldCreateNewConfig_WhenNotExists() {
        ConfiguracionPuntosDTO updateDTO = new ConfiguracionPuntosDTO();
        updateDTO.setPuntosParaVip(150);
        updateDTO.setPesosPorPunto(8.0);

        // Configuración que se va a crear
        ConfiguracionPuntos nuevaConfig = new ConfiguracionPuntos();
        nuevaConfig.setId(1L);
        nuevaConfig.setPuntosParaVip(100); // Valores por defecto
        nuevaConfig.setPesosPorPunto(10.0); // Valores por defecto

        // Configuración actualizada
        ConfiguracionPuntos configActualizada = new ConfiguracionPuntos();
        configActualizada.setId(1L);
        configActualizada.setPuntosParaVip(150);
        configActualizada.setPesosPorPunto(8.0);

        // Primera llamada a findById: no existe
        when(configuracionPuntosRepository.findById(1L))
                .thenReturn(Optional.empty())
                // Segunda llamada a findById: ya existe la configuración actualizada
                .thenReturn(Optional.of(configActualizada));

        // Primer save: crear configuración por defecto
        when(configuracionPuntosRepository.save(any(ConfiguracionPuntos.class)))
                .thenReturn(nuevaConfig);

        ConfiguracionPuntosDTO result = clienteService.actualizarConfiguracion(updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getPuntosParaVip()).isEqualTo(150);
        assertThat(result.getPesosPorPunto()).isEqualTo(8.0);

        // ✅ Verificar que se llamó a findById al menos una vez
        verify(configuracionPuntosRepository, atLeastOnce()).findById(1L);
        // ✅ Verificar que save se llamó 2 veces (1 crear + 1 actualizar)
        verify(configuracionPuntosRepository, times(2)).save(any(ConfiguracionPuntos.class));
    }

}