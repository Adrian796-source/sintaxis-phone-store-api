package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.AccesorioRequestDTO;
import com.adrian.sintaxis.dto.AccesorioResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IAccesorioService {

    AccesorioResponseDTO guardar(AccesorioRequestDTO dto, MultipartFile imagen);
    AccesorioResponseDTO guardar(AccesorioRequestDTO dto);
    Optional<AccesorioResponseDTO> buscarPorId(Long id);
    Page<AccesorioResponseDTO> listarTodos(Pageable pageable);
    AccesorioResponseDTO actualizar(Long id, AccesorioRequestDTO dto);
    void eliminar(Long id);

    List<AccesorioResponseDTO> buscarPorTipo(String tipoAccesorio);
    List<AccesorioResponseDTO> buscarOriginales();
    List<AccesorioResponseDTO> buscarPorMarcaCompatible(String marca);
    Page<AccesorioResponseDTO> buscarConFiltros(String marca, String tipoAccesorio, Double precioMin, Double precioMax, Boolean esOriginal, Pageable pageable);
    AccesorioResponseDTO reponerStock(Long id, int cantidad);
    List<AccesorioResponseDTO> stockBajo();
}
