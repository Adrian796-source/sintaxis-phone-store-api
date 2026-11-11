package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.SubcategoriaDTO;
import com.adrian.sintaxis.model.Subcategoria;
import com.adrian.sintaxis.repository.SubcategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubcategoriaService {

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Transactional(readOnly = true)
    public List<SubcategoriaDTO> listarTodas() {
        return subcategoriaRepository.findAll().stream()
                .map(this.subcategoriaToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubcategoriaDTO guardar(SubcategoriaDTO dto) {
        if (subcategoriaRepository.existsByNombre(dto.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe una subcategoría con el nombre: " + dto.getNombre());
        }
        Subcategoria sub = new Subcategoria();
        sub.setNombre(dto.getNombre().trim());
        Subcategoria guardada = subcategoriaRepository.save(sub);
        return subcategoriaToDTO.apply(guardada);
    }

    private final java.util.function.Function<Subcategoria, SubcategoriaDTO> subcategoriaToDTO = sub -> {
        SubcategoriaDTO dto = new SubcategoriaDTO();
        dto.setIdSubcategoria(sub.getIdSubcategoria());
        dto.setNombre(sub.getNombre());
        return dto;
    };
}
