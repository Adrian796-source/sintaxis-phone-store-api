package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.CelularRequestDTO;
import com.adrian.sintaxis.dto.CelularResponseDTO;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.model.Celular;
import com.adrian.sintaxis.repository.CelularRepository;
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
public class CelularService implements ICelularService {

    private final CelularRepository celularRepository;

    private CelularResponseDTO toDTO(Celular celular) {
        CelularResponseDTO dto = new CelularResponseDTO();
        dto.setIdProducto(celular.getIdProducto());
        dto.setNombre(celular.getNombre());
        dto.setDescripcion(celular.getDescripcion());
        dto.setPrecio(celular.getPrecio());
        dto.setStock(celular.getStock());
        dto.setStockMinimo(celular.getStockMinimo());
        dto.setMarca(celular.getMarca());
        dto.setCategoria(celular.getCategoria());
        dto.setFechaAlta(celular.getFechaAlta());
        dto.setActivo(celular.isActivo());
        dto.setModelo(celular.getModelo());
        dto.setAlmacenamientoGB(celular.getAlmacenamientoGB());
        dto.setRamGB(celular.getRamGB());
        dto.setColor(celular.getColor());
        dto.setProcesador(celular.getProcesador());
        dto.setPantallaPulgadas(celular.getPantallaPulgadas());
        dto.setBateriaMAh(celular.getBateriaMAh());
        dto.setSistemaOperativo(celular.getSistemaOperativo());
        dto.setEsLibre(celular.isEsLibre());
        dto.setImagenUrl(celular.getImagenUrl());
        return dto;
    }

    private Celular toEntity(CelularRequestDTO dto) {
        Celular celular = new Celular();
        celular.setNombre(dto.getNombre());
        celular.setDescripcion(dto.getDescripcion());
        celular.setPrecio(dto.getPrecio());
        celular.setStock(dto.getStock());
        celular.setStockMinimo(dto.getStockMinimo());
        celular.setMarca(dto.getMarca());
        celular.setCategoria("Celular");
        celular.setModelo(dto.getModelo());
        celular.setAlmacenamientoGB(dto.getAlmacenamientoGB());
        celular.setRamGB(dto.getRamGB());
        celular.setColor(dto.getColor());
        celular.setProcesador(dto.getProcesador());
        celular.setPantallaPulgadas(dto.getPantallaPulgadas());
        celular.setBateriaMAh(dto.getBateriaMAh());
        celular.setSistemaOperativo(dto.getSistemaOperativo());
        celular.setEsLibre(dto.isEsLibre());

        //  Si el DTO tiene imagenUrl, se la asignamos
        if (dto.getImagenUrl() != null && !dto.getImagenUrl().isEmpty()) {
            celular.setImagenUrl(dto.getImagenUrl());
        }
        return celular;
    }

    // ✅ MÉTODO 1: Guardar CON imagen
    @Override
    public CelularResponseDTO guardar(CelularRequestDTO dto, MultipartFile imagen) {
        // Validaciones
        if (dto.getPrecio() == null || dto.getPrecio() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }
        if (dto.getAlmacenamientoGB() <= 0) {
            throw new RuntimeException("El almacenamiento debe ser mayor a 0");
        }
        if (dto.getBateriaMAh() <= 0) {
            throw new RuntimeException("La batería debe ser mayor a 0");
        }

        Celular celular = toEntity(dto);
        celular.setFechaAlta(LocalDateTime.now());
        celular.setActivo(true);

        // ✅ GUARDAR LA IMAGEN SI VIENE
        if (imagen != null && !imagen.isEmpty()) {
            String urlImagen = guardarImagen(imagen);
            celular.setImagenUrl(urlImagen);
        }

        return toDTO(celularRepository.save(celular));
    }

    // ✅ MÉTODO 2: Guardar SIN imagen (llama al método con imagen)
    @Override
    public CelularResponseDTO guardar(CelularRequestDTO dto) {
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
    public Optional<CelularResponseDTO> buscarPorId(Long id) {
        return Optional.of(celularRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Celular no encontrado con id: " + id)));
    }

    @Override
    public Page<CelularResponseDTO> listarTodos(Pageable pageable) {
        return celularRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public CelularResponseDTO actualizar(Long id, CelularRequestDTO dto) {
        Celular existente = celularRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Celular no encontrado con id: " + id));

        if (dto.getPrecio() == null || dto.getPrecio() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }

        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setPrecio(dto.getPrecio());
        existente.setStock(dto.getStock());
        existente.setStockMinimo(dto.getStockMinimo());
        existente.setModelo(dto.getModelo());
        existente.setColor(dto.getColor());
        existente.setAlmacenamientoGB(dto.getAlmacenamientoGB());
        existente.setRamGB(dto.getRamGB());
        existente.setProcesador(dto.getProcesador());
        existente.setPantallaPulgadas(dto.getPantallaPulgadas());
        existente.setBateriaMAh(dto.getBateriaMAh());
        existente.setSistemaOperativo(dto.getSistemaOperativo());
        existente.setEsLibre(dto.isEsLibre());
        return toDTO(celularRepository.save(existente));
    }

    @Override
    public void eliminar(Long id) {
        Celular celular = celularRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Celular no encontrado con id: " + id));
        celular.setActivo(false);
        celularRepository.save(celular);
    }

    @Override
    public Page<CelularResponseDTO> buscarConFiltros(String marca, String sistemaOperativo, Double precioMin, Double precioMax, Boolean esLibre, Pageable pageable) {
        Specification<Celular> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (marca != null) predicates.add(cb.like(cb.lower(root.get("marca")), "%" + marca.toLowerCase() + "%"));
            if (sistemaOperativo != null) predicates.add(cb.equal(cb.lower(root.get("sistemaOperativo")), sistemaOperativo.toLowerCase()));
            if (precioMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), precioMin));
            if (precioMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
            if (esLibre != null) predicates.add(cb.equal(root.get("esLibre"), esLibre));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return celularRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Override
    public CelularResponseDTO reponerStock(Long id, int cantidad) {
        if (cantidad <= 0) throw new RuntimeException("La cantidad debe ser mayor a 0");
        Celular celular = celularRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Celular no encontrado con id: " + id));
        celular.setStock(celular.getStock() + cantidad);
        return toDTO(celularRepository.save(celular));
    }

    @Override
    public List<CelularResponseDTO> stockBajo() {
        return celularRepository.findAll().stream()
                .filter(c -> c.isActivo() && c.getStock() <= c.getStockMinimo())
                .map(this::toDTO).toList();
    }

    @Override
    public List<CelularResponseDTO> buscarPorModelo(String modelo) {
        return celularRepository.findByModeloContainingIgnoreCase(modelo).stream().map(this::toDTO).toList();
    }

    @Override
    public List<CelularResponseDTO> buscarPorSistemaOperativo(String sistemaOperativo) {
        return celularRepository.findBySistemaOperativo(sistemaOperativo).stream().map(this::toDTO).toList();
    }

    @Override
    public List<CelularResponseDTO> buscarLibres() {
        return celularRepository.findByEsLibreTrue().stream().map(this::toDTO).toList();
    }

    @Override
    public List<CelularResponseDTO> buscarPorRangoAlmacenamiento(int minGB, int maxGB) {
        if (minGB > maxGB) {
            throw new RuntimeException("El mínimo no puede ser mayor al máximo");
        }
        return celularRepository.findByAlmacenamientoGBBetween(minGB, maxGB).stream().map(this::toDTO).toList();
    }
}
