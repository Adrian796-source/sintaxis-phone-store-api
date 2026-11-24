package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {
    Optional<Subcategoria> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdSubcategoriaNot(String nombre, Long idSubcategoria);
}
