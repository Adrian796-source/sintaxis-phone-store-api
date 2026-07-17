package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.CelularRequestDTO;
import com.adrian.sintaxis.dto.CelularResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ICelularService {

    CelularResponseDTO guardar(CelularRequestDTO dto, MultipartFile imagen);
    CelularResponseDTO guardar(CelularRequestDTO dto);
    Optional<CelularResponseDTO> buscarPorId(Long id);
    Page<CelularResponseDTO> listarTodos(Pageable pageable);
    CelularResponseDTO actualizar(Long id, CelularRequestDTO dto);
    void eliminar(Long id);

    List<CelularResponseDTO> buscarPorModelo(String modelo);
    List<CelularResponseDTO> buscarPorSistemaOperativo(String sistemaOperativo);
    List<CelularResponseDTO> buscarLibres();
    List<CelularResponseDTO> buscarPorRangoAlmacenamiento(int minGB, int maxGB);
    Page<CelularResponseDTO> buscarConFiltros(String marca, String sistemaOperativo, Double precioMin, Double precioMax, Boolean esLibre, Pageable pageable);
    CelularResponseDTO reponerStock(Long id, int cantidad);
    List<CelularResponseDTO> stockBajo();
}
