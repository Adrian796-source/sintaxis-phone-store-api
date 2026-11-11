package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.SubcategoriaDTO;
import com.adrian.sintaxis.service.SubcategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcategorias")
@Tag(name = "Subcategorías", description = "Gestión de subcategorías para accesorios")
public class SubcategoriaController {

    @Autowired
    private SubcategoriaService subcategoriaService;

    @GetMapping
    @Operation(summary = "Listar todas las subcategorías (Público)")
    public ResponseEntity<List<SubcategoriaDTO>> listar() {
        return ResponseEntity.ok(subcategoriaService.listarTodas());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(summary = "Crear nueva subcategoría (ADMIN o EMPLEADO)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SubcategoriaDTO> crear(@Valid @RequestBody SubcategoriaDTO dto) {
        SubcategoriaDTO creada = subcategoriaService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
