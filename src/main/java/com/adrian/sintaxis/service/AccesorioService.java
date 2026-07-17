package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.AccesorioRequestDTO;
import com.adrian.sintaxis.dto.AccesorioResponseDTO;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.model.Accesorio;
import com.adrian.sintaxis.repository.AccesorioRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccesorioService implements IAccesorioService {

    private final AccesorioRepository accesorioRepository;

    private AccesorioResponseDTO toDTO(Accesorio accesorio) {
        AccesorioResponseDTO dto = new AccesorioResponseDTO();
        dto.setIdProducto(accesorio.getIdProducto());
        dto.setNombre(accesorio.getNombre());
        dto.setDescripcion(accesorio.getDescripcion());
        dto.setPrecio(accesorio.getPrecio());
        dto.setStock(accesorio.getStock());
        dto.setStockMinimo(accesorio.getStockMinimo());
        dto.setMarca(accesorio.getMarca());
        dto.setCategoria(accesorio.getCategoria());
        dto.setFechaAlta(accesorio.getFechaAlta());
        dto.setActivo(accesorio.isActivo());
        dto.setTipoAccesorio(accesorio.getTipoAccesorio());
        dto.setColor(accesorio.getColor());
        dto.setMaterial(accesorio.getMaterial());
        dto.setEsOriginal(accesorio.isEsOriginal());
        dto.setMarcasCompatibles(accesorio.getMarcasCompatibles());
        dto.setImagenUrl(accesorio.getImagenUrl());
        return dto;
    }

    private Accesorio toEntity(AccesorioRequestDTO dto) {
        Accesorio accesorio = new Accesorio();
        accesorio.setNombre(dto.getNombre());
        accesorio.setDescripcion(dto.getDescripcion());
        accesorio.setPrecio(dto.getPrecio());
        accesorio.setStock(dto.getStock());
        accesorio.setStockMinimo(dto.getStockMinimo());
        accesorio.setMarca(dto.getMarca());
        accesorio.setCategoria("Accesorio");
        accesorio.setTipoAccesorio(dto.getTipoAccesorio());
        accesorio.setColor(dto.getColor());
        accesorio.setMaterial(dto.getMaterial());
        accesorio.setEsOriginal(dto.isEsOriginal());
        accesorio.setMarcasCompatibles(dto.getMarcasCompatibles());

        //  Si el DTO tiene imagenUrl, se la asignamos
        if (dto.getImagenUrl() != null && !dto.getImagenUrl().isEmpty()) {
            accesorio.setImagenUrl(dto.getImagenUrl());
        }
        return accesorio;
    }

    @Override
    public AccesorioResponseDTO guardar(AccesorioRequestDTO dto, MultipartFile imagen) {
        if (dto.getPrecio() == null || dto.getPrecio() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }
        if (dto.getTipoAccesorio() == null || dto.getTipoAccesorio().isBlank()) {
            throw new RuntimeException("El tipo de accesorio es obligatorio");
        }

        Accesorio accesorio = toEntity(dto);
        accesorio.setFechaAlta(LocalDateTime.now());
        accesorio.setActivo(true);

        // ✅ Guardar imagen si viene
        if (imagen != null && !imagen.isEmpty()) {
            String urlImagen = guardarImagen(imagen);
            accesorio.setImagenUrl(urlImagen);
        }

        return toDTO(accesorioRepository.save(accesorio));
    }

    @Override
    public AccesorioResponseDTO guardar(AccesorioRequestDTO dto) {
        return guardar(dto, null);  // ← Llama al método con imagen
    }



    // ✅ Método auxiliar para guardar imagen
    private String guardarImagen(MultipartFile imagen) {
        try {
            if (imagen == null || imagen.isEmpty()) {
                return null;
            }

            String nombreOriginal = imagen.getOriginalFilename();
            String extension = "";
            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            }

            String nombreArchivo = UUID.randomUUID().toString() + "_" +
                    System.currentTimeMillis() + extension;

            Path rutaCompleta = Paths.get("uploads/", nombreArchivo);
            Files.createDirectories(rutaCompleta.getParent());
            Files.write(rutaCompleta, imagen.getBytes());

            return "/api/productos/imagenes/" + nombreArchivo;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage(), e);
        }
    }



    @Override
    public Optional<AccesorioResponseDTO> buscarPorId(Long id) {
        return Optional.of(accesorioRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Accesorio no encontrado con id: " + id)));
    }

    @Override
    public Page<AccesorioResponseDTO> listarTodos(Pageable pageable) {
        return accesorioRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public AccesorioResponseDTO actualizar(Long id, AccesorioRequestDTO dto) {
        Accesorio existente = accesorioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accesorio no encontrado con id: " + id));

        if (dto.getPrecio() == null || dto.getPrecio() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }

        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setPrecio(dto.getPrecio());
        existente.setStock(dto.getStock());
        existente.setStockMinimo(dto.getStockMinimo());
        existente.setTipoAccesorio(dto.getTipoAccesorio());
        existente.setColor(dto.getColor());
        existente.setMaterial(dto.getMaterial());
        existente.setEsOriginal(dto.isEsOriginal());
        existente.setMarcasCompatibles(dto.getMarcasCompatibles());
        return toDTO(accesorioRepository.save(existente));
    }

    @Override
    public void eliminar(Long id) {
        Accesorio accesorio = accesorioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accesorio no encontrado con id: " + id));
        accesorio.setActivo(false);
        accesorioRepository.save(accesorio);
    }

    @Override
    public Page<AccesorioResponseDTO> buscarConFiltros(String marca, String tipoAccesorio, Double precioMin, Double precioMax, Boolean esOriginal, Pageable pageable) {
        Specification<Accesorio> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (marca != null) predicates.add(cb.like(cb.lower(root.get("marca")), "%" + marca.toLowerCase() + "%"));
            if (tipoAccesorio != null) predicates.add(cb.equal(cb.lower(root.get("tipoAccesorio")), tipoAccesorio.toLowerCase()));
            if (precioMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), precioMin));
            if (precioMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
            if (esOriginal != null) predicates.add(cb.equal(root.get("esOriginal"), esOriginal));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return accesorioRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Override
    public AccesorioResponseDTO reponerStock(Long id, int cantidad) {
        if (cantidad <= 0) throw new RuntimeException("La cantidad debe ser mayor a 0");
        Accesorio accesorio = accesorioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accesorio no encontrado con id: " + id));
        accesorio.setStock(accesorio.getStock() + cantidad);
        return toDTO(accesorioRepository.save(accesorio));
    }

    @Override
    public List<AccesorioResponseDTO> stockBajo() {
        return accesorioRepository.findAll().stream()
                .filter(a -> a.isActivo() && a.getStock() <= a.getStockMinimo())
                .map(this::toDTO).toList();
    }

    @Override
    public List<AccesorioResponseDTO> buscarPorTipo(String tipoAccesorio) {
        return accesorioRepository.findByTipoAccesorio(tipoAccesorio).stream().map(this::toDTO).toList();
    }

    @Override
    public List<AccesorioResponseDTO> buscarOriginales() {
        return accesorioRepository.findByEsOriginalTrue().stream().map(this::toDTO).toList();
    }

    @Override
    public List<AccesorioResponseDTO> buscarPorMarcaCompatible(String marca) {
        return accesorioRepository.findByMarcasCompatiblesContaining(marca).stream().map(this::toDTO).toList();
    }
}
