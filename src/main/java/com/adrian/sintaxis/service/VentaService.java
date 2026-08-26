package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.DetalleVentaRequestDTO;
import com.adrian.sintaxis.dto.DetalleVentaResponseDTO;
import com.adrian.sintaxis.dto.ReporteVentaDTO;
import com.adrian.sintaxis.dto.VentaRequestDTO;
import com.adrian.sintaxis.dto.VentaResponseDTO;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.model.Cliente;
import com.adrian.sintaxis.model.DetalleVenta;
import com.adrian.sintaxis.model.Producto;
import com.adrian.sintaxis.model.Venta;
import com.adrian.sintaxis.repository.ClienteRepository;
import com.adrian.sintaxis.repository.ProductoRepository;
import com.adrian.sintaxis.repository.UsuarioRepository;
import com.adrian.sintaxis.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.adrian.sintaxis.exception.*;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VentaService implements IVentaService {

    private static final int PUNTOS_POR_VENTA = 10;
    private static final List<String> ESTADOS_VALIDOS = List.of("Pendiente", "Pagada", "Entregada", "Cancelada");
    private static final double DESCUENTO_VIP = 0.10;

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteService clienteService;

    private DetalleVentaResponseDTO toDetalleDTO(DetalleVenta detalle) {
        DetalleVentaResponseDTO dto = new DetalleVentaResponseDTO();
        dto.setIdDetalleVenta(detalle.getIdDetalleVenta());
        dto.setIdProducto(detalle.getProducto().getIdProducto());
        dto.setNombreProducto(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotalDetalle(detalle.getPrecioUnitario() * detalle.getCantidad());
        return dto;
    }

    private VentaResponseDTO toDTO(Venta venta) {
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
            dto.setDetalles(venta.getDetalles().stream().map(this::toDetalleDTO).toList());
        }
        return dto;
    }

    @Override
    public VentaResponseDTO guardar(VentaRequestDTO dto) {
        if (!ESTADOS_VALIDOS.contains(dto.getEstado())) {
            throw new EstadoInvalidoException("Estado inválido. Los estados válidos son: " + ESTADOS_VALIDOS);
        }
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new VentaSinDetallesException("La venta debe tener al menos un producto");
        }

        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + dto.getIdCliente()));

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setEstado(dto.getEstado());
        venta.setMetodoPago(dto.getMetodoPago());
        venta.setFecha(LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")));
        venta.setActivo(true);

        // ✅ VALIDAR STOCK ANTES DE PROCESAR LA VENTA
        for (DetalleVentaRequestDTO d : dto.getDetalles()) {
            Producto producto = productoRepository.findById(d.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + d.getIdProducto()));
            
            if (producto.getStock() < d.getCantidad()) {
                throw new StockInsuficienteException(
                    String.format("Stock insuficiente para '%s'. Disponible: %d, Solicitado: %d",
                        producto.getNombre(), producto.getStock(), d.getCantidad())
                );
            }
        }

        List<DetalleVenta> detalles = dto.getDetalles().stream().map(d -> {
            Producto producto = productoRepository.findById(d.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + d.getIdProducto()));
            producto.reducirStock(d.getCantidad());
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(d.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setVenta(venta);
            return detalle;
        }).toList();

        double subtotal = detalles.stream()
                .mapToDouble(d -> d.getPrecioUnitario() * d.getCantidad())
                .sum();
        double descuento = dto.getDescuento() != null ? dto.getDescuento() : 0.0;
        if (cliente.isEsVip()) {
            descuento += subtotal * DESCUENTO_VIP;
        }

        venta.setDetalles(detalles);
        venta.setSubtotal(subtotal);
        venta.setDescuento(descuento);
        venta.setTotal(subtotal - descuento);

        Venta ventaGuardada = ventaRepository.save(venta);

        if ("Pagada".equals(dto.getEstado())) {
            int puntos = clienteService.calcularPuntosPorMonto(ventaGuardada.getTotal());
            clienteService.agregarPuntos(cliente.getIdCliente(), puntos);
        }

        return toDTO(ventaGuardada);
    }

    @Override
    public Optional<VentaResponseDTO> buscarPorId(Long id) {
        return Optional.of(ventaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id)));
    }

    @Override
    public Page<VentaResponseDTO> listarTodos(Pageable pageable) {
        return ventaRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public VentaResponseDTO actualizar(Long id, VentaRequestDTO dto) {
        Venta existente = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        if ("Cancelada".equals(existente.getEstado())) {
            throw new VentaCanceladaException("No se puede modificar una venta cancelada");
        }

        existente.setMetodoPago(dto.getMetodoPago());
        double descuento = dto.getDescuento() != null ? dto.getDescuento() : 0.0;
        existente.setDescuento(descuento);
        existente.setTotal(existente.getSubtotal() - descuento);
        return toDTO(ventaRepository.save(existente));
    }


    @Override
    public void eliminar(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
        venta.setActivo(false);
        ventaRepository.save(venta);
    }


    @Override
    public List<VentaResponseDTO> misVentas(String email) {
        return usuarioRepository.findByEmail(email)
                .map(usuario -> {
                    if (usuario.getCliente() == null) {
                        throw new UsuarioSinClienteException("El usuario no tiene un cliente asociado");
                    }
                    return ventaRepository.findByClienteIdCliente(usuario.getCliente().getIdCliente())
                            .stream().map(this::toDTO).toList();
                })
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Override
    public Double totalRecaudado(LocalDateTime desde, LocalDateTime hasta) {
        if (desde.isAfter(hasta)) throw new FechasInvalidasException("La fecha 'desde' no puede ser posterior a 'hasta'");
        return ventaRepository.totalRecaudadoEntreFechas(desde, hasta);
    }

    @Override
    public Map<String, Long> ventasPorEstado() {
        Map<String, Long> resultado = new LinkedHashMap<>();
        ventaRepository.contarPorEstado().forEach(row -> resultado.put((String) row[0], (Long) row[1]));
        return resultado;
    }

    @Override
    public List<ReporteVentaDTO> topClientes() {
        return ventaRepository.topClientes().stream()
                .map(row -> new ReporteVentaDTO(
                        row[1] + " " + row[2],
                        Map.of("cantidadVentas", row[3], "totalGastado", row[4])
                )).toList();
    }

    @Override
    public List<VentaResponseDTO> buscarPorCliente(Long idCliente) {
        return ventaRepository.findByClienteIdCliente(idCliente).stream().map(this::toDTO).toList();
    }

    @Override
    public List<VentaResponseDTO> buscarPorEstado(String estado) {
        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new EstadoInvalidoException("Estado inválido. Los estados válidos son: " + ESTADOS_VALIDOS);
        }
        return ventaRepository.findByEstado(estado).stream().map(this::toDTO).toList();
    }

    @Override
    public List<VentaResponseDTO> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde.isAfter(hasta)) {
            throw new FechasInvalidasException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        return ventaRepository.findByFechaBetween(desde, hasta).stream().map(this::toDTO).toList();
    }

    @Override
    public VentaResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new EstadoInvalidoException("Estado inválido. Los estados válidos son: " + ESTADOS_VALIDOS);
        }
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        if ("Cancelada".equals(venta.getEstado())) {
            throw new VentaCanceladaException("No se puede cambiar el estado de una venta cancelada");
        }

        String estadoAnterior = venta.getEstado();
        venta.setEstado(nuevoEstado);

        if ("Cancelada".equals(nuevoEstado)) {
            venta.getDetalles().forEach(d -> {
                d.getProducto().devolverStock(d.getCantidad());
                productoRepository.save(d.getProducto());
            });
            if ("Pagada".equals(estadoAnterior)) {
                int puntos = clienteService.calcularPuntosPorMonto(venta.getTotal());
                clienteService.restarPuntos(venta.getCliente().getIdCliente(), puntos);
            }
        }

        if ("Pagada".equals(nuevoEstado) && !"Pagada".equals(estadoAnterior)) {
            int puntos = clienteService.calcularPuntosPorMonto(venta.getTotal());
            clienteService.agregarPuntos(venta.getCliente().getIdCliente(), puntos);
        }

        return toDTO(ventaRepository.save(venta));
    }
}
