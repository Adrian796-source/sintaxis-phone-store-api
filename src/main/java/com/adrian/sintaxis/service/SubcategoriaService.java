package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.SubcategoriaDTO;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.exception.ValoresInvalidosException;
import com.adrian.sintaxis.model.Accesorio;
import com.adrian.sintaxis.model.Subcategoria;
import com.adrian.sintaxis.repository.AccesorioRepository;
import com.adrian.sintaxis.repository.SubcategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SubcategoriaService {

    private final SubcategoriaRepository subcategoriaRepository;
    private final AccesorioRepository accesorioRepository;

    public SubcategoriaService(SubcategoriaRepository subcategoriaRepository,
                               AccesorioRepository accesorioRepository) {
        this.subcategoriaRepository = subcategoriaRepository;
        this.accesorioRepository = accesorioRepository;
    }

    @Transactional(readOnly = true)
    public List<SubcategoriaDTO> listarTodas() {
        return subcategoriaRepository.findAll().stream()
                .map(this.subcategoriaToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubcategoriaDTO guardar(SubcategoriaDTO dto) {
        String nombre = normalizarNombre(dto.getNombre());

        if (subcategoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ValoresInvalidosException("Ya existe una subcategoría con el nombre: " + nombre);
        }

        Subcategoria sub = new Subcategoria();
        sub.setNombre(nombre);
        return subcategoriaToDTO.apply(subcategoriaRepository.save(sub));
    }

    @Transactional
    public SubcategoriaDTO actualizar(Long id, SubcategoriaDTO dto) {
        Subcategoria sub = buscarPorId(id);
        String nombre = normalizarNombre(dto.getNombre());

        if (subcategoriaRepository.existsByNombreIgnoreCaseAndIdSubcategoriaNot(nombre, id)) {
            throw new ValoresInvalidosException("Ya existe otra subcategoría con el nombre: " + nombre);
        }

        sub.setNombre(nombre);
        return subcategoriaToDTO.apply(subcategoriaRepository.save(sub));
    }

    /**
     * Elimina una subcategoría desvinculando previamente a todos los accesorios asociados.
     * De esta forma no se rompe la integridad referencial ni se pierde el inventario:
     * los accesorios permanecen disponibles y quedan con subcategoría nula (N/A).
     */
    @Transactional
    public void eliminar(Long id) {
        Subcategoria sub = buscarPorId(id);

        // Desvinculamos los accesorios asociados para preservar el inventario y la integridad referencial.
        List<Accesorio> accesorios = accesorioRepository.findBySubcategoriaIdSubcategoria(id);
        if (!accesorios.isEmpty()) {
            accesorios.forEach(accesorio -> accesorio.setSubcategoria(null));
            accesorioRepository.saveAll(accesorios);
        }

        subcategoriaRepository.delete(sub);
    }

    private Subcategoria buscarPorId(Long id) {
        return subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategoría no encontrada con ID: " + id));
    }

    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValoresInvalidosException("El nombre de la subcategoría es obligatorio");
        }
        return nombre.trim();
    }

    private final Function<Subcategoria, SubcategoriaDTO> subcategoriaToDTO = sub -> {
        SubcategoriaDTO dto = new SubcategoriaDTO();
        dto.setIdSubcategoria(sub.getIdSubcategoria());
        dto.setNombre(sub.getNombre());
        return dto;
    };
}
