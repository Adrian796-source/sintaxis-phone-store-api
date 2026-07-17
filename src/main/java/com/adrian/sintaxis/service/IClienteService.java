package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.ClienteRequestDTO;
import com.adrian.sintaxis.dto.ClienteResponseDTO;
import com.adrian.sintaxis.dto.ConfiguracionPuntosDTO;
import com.adrian.sintaxis.dto.PerfilConHistorialDTO;

import java.util.List;
import java.util.Optional;

public interface IClienteService {

    ClienteResponseDTO guardar(ClienteRequestDTO dto);
    Optional<ClienteResponseDTO> buscarPorId(Long id);
    List<ClienteResponseDTO> listarTodos();
    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto);
    void eliminar(Long id);

    List<ClienteResponseDTO> buscarPorNombre(String nombre);
    Optional<ClienteResponseDTO> buscarPorEmail(String email);
    List<ClienteResponseDTO> listarVip();
    ClienteResponseDTO agregarPuntos(Long id, int puntos);
    ClienteResponseDTO restarPuntos(Long id, int puntos);

    ConfiguracionPuntosDTO obtenerConfiguracion();
    ConfiguracionPuntosDTO actualizarConfiguracion(ConfiguracionPuntosDTO dto);
    PerfilConHistorialDTO obtenerPerfilConHistorial(String email);
}
