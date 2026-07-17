package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.ReporteVentaDTO;
import com.adrian.sintaxis.dto.VentaRequestDTO;
import com.adrian.sintaxis.dto.VentaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IVentaService {

    VentaResponseDTO guardar(VentaRequestDTO dto);
    Optional<VentaResponseDTO> buscarPorId(Long id);
    Page<VentaResponseDTO> listarTodos(Pageable pageable);
    VentaResponseDTO actualizar(Long id, VentaRequestDTO dto);
    void eliminar(Long id);

    List<VentaResponseDTO> buscarPorCliente(Long idCliente);
    List<VentaResponseDTO> misVentas(String email);
    List<VentaResponseDTO> buscarPorEstado(String estado);
    List<VentaResponseDTO> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta);
    VentaResponseDTO cambiarEstado(Long id, String nuevoEstado);

    Double totalRecaudado(LocalDateTime desde, LocalDateTime hasta);
    Map<String, Long> ventasPorEstado();
    List<ReporteVentaDTO> topClientes();
}
