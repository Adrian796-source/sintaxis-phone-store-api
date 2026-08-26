package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.*;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.model.Cliente;
import com.adrian.sintaxis.model.ConfiguracionPuntos;
import com.adrian.sintaxis.model.DetalleVenta;
import com.adrian.sintaxis.model.Venta;
import com.adrian.sintaxis.repository.ClienteRepository;
import com.adrian.sintaxis.repository.ConfiguracionPuntosRepository;
import com.adrian.sintaxis.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.adrian.sintaxis.exception.*;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService implements IClienteService {

    private static final long CONFIG_ID = 1L;

    private final ClienteRepository clienteRepository;
    private final ConfiguracionPuntosRepository configuracionPuntosRepository;
    private final VentaRepository ventaRepository;

    private ClienteResponseDTO toDTO(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setIdCliente(cliente.getIdCliente());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setDireccion(cliente.getDireccion());
        dto.setFechaRegistro(cliente.getFechaRegistro());
        dto.setEsVip(cliente.isEsVip());
        dto.setPuntosAcumulados(cliente.getPuntosAcumulados());
        dto.setActivo(cliente.isActivo());

        return dto;
    }

    private Cliente toEntity(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
        return cliente;
    }

    public ConfiguracionPuntosDTO obtenerConfiguracion() {
        ConfiguracionPuntos config = getConfig();
        ConfiguracionPuntosDTO dto = new ConfiguracionPuntosDTO();
        dto.setPuntosParaVip(config.getPuntosParaVip());
        dto.setPesosPorPunto(config.getPesosPorPunto());
        return dto;
    }

    public ConfiguracionPuntosDTO actualizarConfiguracion(ConfiguracionPuntosDTO dto) {
        ConfiguracionPuntos config = getConfig();
        config.setPuntosParaVip(dto.getPuntosParaVip());
        config.setPesosPorPunto(dto.getPesosPorPunto());
        configuracionPuntosRepository.save(config);
        return dto;
    }

    @Override
    public PerfilConHistorialDTO obtenerPerfilConHistorial(String email) {
        // 1. Buscar cliente por email
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con email: " + email));

        // 2. Buscar todas sus ventas
        List<Venta> ventas = ventaRepository.findByClienteIdCliente(cliente.getIdCliente());

        // 3. Convertir ventas a DTOs (reutilizando la lógica de VentaService)
        List<VentaResponseDTO> historial = ventas.stream()
                .map(this::convertirVentaADTO)
                .collect(Collectors.toList());

        // 4. Construir respuesta
        PerfilConHistorialDTO perfil = new PerfilConHistorialDTO();
        perfil.setIdCliente(cliente.getIdCliente());
        perfil.setNombre(cliente.getNombre());
        perfil.setApellido(cliente.getApellido());
        perfil.setEmail(cliente.getEmail());
        perfil.setTelefono(cliente.getTelefono());
        perfil.setDireccion(cliente.getDireccion());
        perfil.setEsVip(cliente.isEsVip());
        perfil.setPuntosAcumulados(cliente.getPuntosAcumulados());
        perfil.setFechaRegistro(cliente.getFechaRegistro());
        perfil.setHistorialCompras(historial);

        return perfil;
    }

    // ✅ Método auxiliar para convertir Venta a VentaResponseDTO
    private VentaResponseDTO convertirVentaADTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setIdVenta(venta.getIdVenta());
        dto.setFecha(venta.getFecha());
        dto.setEstado(venta.getEstado());
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setSubtotal(venta.getSubtotal());
        dto.setDescuento(venta.getDescuento());
        dto.setTotal(venta.getTotal());

        if (venta.getCliente() != null) {
            dto.setIdCliente(venta.getCliente().getIdCliente());
            dto.setNombreCliente(venta.getCliente().getNombre());
            dto.setApellidoCliente(venta.getCliente().getApellido());
        }

        if (venta.getDetalles() != null) {
            dto.setDetalles(venta.getDetalles().stream()
                    .map(this::convertirDetalleADTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    // ✅ Método auxiliar para convertir DetalleVenta a DetalleVentaResponseDTO
    private DetalleVentaResponseDTO convertirDetalleADTO(DetalleVenta detalle) {
        DetalleVentaResponseDTO dto = new DetalleVentaResponseDTO();
        dto.setIdDetalleVenta(detalle.getIdDetalleVenta());
        dto.setIdProducto(detalle.getProducto().getIdProducto());
        dto.setNombreProducto(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotalDetalle(detalle.getPrecioUnitario() * detalle.getCantidad());
        return dto;
    }



    private ConfiguracionPuntos getConfig() {
        return configuracionPuntosRepository.findById(CONFIG_ID).orElseGet(() -> {
            ConfiguracionPuntos config = new ConfiguracionPuntos();
            config.setId(CONFIG_ID);
            config.setPuntosParaVip(100);
            config.setPesosPorPunto(10.0);
            return configuracionPuntosRepository.save(config);
        });
    }

    public int calcularPuntosPorMonto(double total) {
        return (int) (total / getConfig().getPesosPorPunto());
    }

    @Override
    public ClienteResponseDTO guardar(ClienteRequestDTO dto) {
        if (clienteRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EmailYaExistenteException("Ya existe un cliente con el email: " + dto.getEmail());
        }
        Cliente cliente = toEntity(dto);
        cliente.setFechaRegistro(LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")));
        cliente.setEsVip(false);
        cliente.setPuntosAcumulados(0);
        cliente.setActivo(true);
        return toDTO(clienteRepository.save(cliente));
    }

    @Override
    public Optional<ClienteResponseDTO> buscarPorId(Long id) {
        return Optional.of(clienteRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id)));
    }

    @Override
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        Optional<Cliente> clienteConEmail = clienteRepository.findByEmail(dto.getEmail());
        if (clienteConEmail.isPresent() && !clienteConEmail.get().getIdCliente().equals(id)) {
            throw new EmailEnUsoException("El email ya está en uso por otro cliente");
        }

        existente.setNombre(dto.getNombre());
        existente.setApellido(dto.getApellido());
        existente.setEmail(dto.getEmail());
        existente.setTelefono(dto.getTelefono());
        existente.setDireccion(dto.getDireccion());
        return toDTO(clienteRepository.save(existente));
    }

    @Override

    public void eliminar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }


    @Override
    public List<ClienteResponseDTO> buscarPorNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre).stream().map(this::toDTO).toList();
    }

    @Override
    public Optional<ClienteResponseDTO> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email).map(this::toDTO);
    }

    @Override
    public List<ClienteResponseDTO> listarVip() {
        return clienteRepository.findByEsVipTrue().stream().map(this::toDTO).toList();
    }

    @Override
    public ClienteResponseDTO agregarPuntos(Long id, int puntos) {
        if (puntos <= 0) {
            throw new PuntosInvalidosException("Los puntos deben ser un valor positivo");
        }
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        cliente.setPuntosAcumulados(cliente.getPuntosAcumulados() + puntos);

        if (!cliente.isEsVip() && cliente.getPuntosAcumulados() >= getConfig().getPuntosParaVip()) {
            cliente.setEsVip(true);
        }

        return toDTO(clienteRepository.save(cliente));
    }

    @Override
    public ClienteResponseDTO restarPuntos(Long id, int puntos) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        int nuevosPuntos = Math.max(0, cliente.getPuntosAcumulados() - puntos);
        cliente.setPuntosAcumulados(nuevosPuntos);

        if (cliente.isEsVip() && nuevosPuntos < getConfig().getPuntosParaVip()) {
            cliente.setEsVip(false);
        }

        return toDTO(clienteRepository.save(cliente));
    }
}
