package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.CelularRequestDTO;
import com.adrian.sintaxis.dto.CelularResponseDTO;
import com.adrian.sintaxis.service.ICelularService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/celulares")
@RequiredArgsConstructor
@Tag(name = "Celulares", description = "Gestión de celulares. Los GET son públicos, POST/PUT/DELETE requieren rol ADMIN o EMPLEADO.")
public class CelularController {

    private final ICelularService celularService;

    // Acepta imagen
    @Operation(summary = "Crear celular con imagen", description = "Registra un nuevo celular en el sistema. Requiere rol ADMIN o EMPLEADO.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CelularResponseDTO> guardarConImagen(
            @RequestPart("celular") @Valid CelularRequestDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(celularService.guardar(dto, imagen));
    }

    // POST sin imagen (para compatibilidad)
    @Operation(summary = "Crear celular sin imagen", description = "Registra un nuevo celular en el sistema. Requiere rol ADMIN o EMPLEADO.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CelularResponseDTO> guardar(@Valid @RequestBody CelularRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(celularService.guardar(dto, null));
    }


    @Operation(summary = "Buscar celular por ID", description = "Devuelve los datos completos de un celular por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<CelularResponseDTO> buscarPorId(@PathVariable Long id) {
        return celularService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los celulares", description = "Devuelve todos los celulares paginados, ordenados por nombre. Usar page y size para navegar.")
    @GetMapping
    public ResponseEntity<Page<CelularResponseDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(celularService.listarTodos(pageable));
    }

    @Operation(summary = "Actualizar celular", description = "Modifica los datos de un celular existente. Requiere rol ADMIN o EMPLEADO.")
    @PutMapping("/{id}")
    public ResponseEntity<CelularResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CelularRequestDTO dto) {
        return ResponseEntity.ok(celularService.actualizar(id, dto));
    }

    @Operation(summary = "Eliminar celular", description = "Borrado lógico: marca el celular como inactivo sin eliminarlo de la base de datos. Requiere rol ADMIN o EMPLEADO.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        celularService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar celulares con filtros", description = "Filtra celulares por marca, sistema operativo, rango de precio y si es libre. Todos los parámetros son opcionales y combinables.")
    @GetMapping("/buscar")
    public ResponseEntity<Page<CelularResponseDTO>> buscarConFiltros(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String sistemaOperativo,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Boolean esLibre,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(celularService.buscarConFiltros(marca, sistemaOperativo, precioMin, precioMax, esLibre, pageable));
    }

    @Operation(summary = "Reponer stock", description = "Suma la cantidad indicada al stock actual del celular. Requiere rol ADMIN o EMPLEADO.")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<CelularResponseDTO> reponerStock(@PathVariable Long id, @RequestParam int cantidad) {
        return ResponseEntity.ok(celularService.reponerStock(id, cantidad));
    }

    @Operation(summary = "Celulares con stock bajo", description = "Devuelve los celulares cuyo stock actual es menor o igual al stock mínimo configurado.")
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<CelularResponseDTO>> stockBajo() {
        return ResponseEntity.ok(celularService.stockBajo());
    }

    @Operation(summary = "Buscar por modelo", description = "Busca celulares cuyo modelo contenga el texto ingresado. No distingue mayúsculas.")
    @GetMapping("/modelo")
    public ResponseEntity<List<CelularResponseDTO>> buscarPorModelo(@RequestParam String modelo) {
        return ResponseEntity.ok(celularService.buscarPorModelo(modelo));
    }

    @Operation(summary = "Buscar por sistema operativo", description = "Devuelve todos los celulares con el sistema operativo indicado. Ej: Android, iOS.")
    @GetMapping("/sistema-operativo")
    public ResponseEntity<List<CelularResponseDTO>> buscarPorSistemaOperativo(@RequestParam String sistemaOperativo) {
        return ResponseEntity.ok(celularService.buscarPorSistemaOperativo(sistemaOperativo));
    }

    @Operation(summary = "Listar celulares libres", description = "Devuelve todos los celulares que no están atados a ningún operador.")
    @GetMapping("/libres")
    public ResponseEntity<List<CelularResponseDTO>> buscarLibres() {
        return ResponseEntity.ok(celularService.buscarLibres());
    }

    @Operation(summary = "Buscar por rango de almacenamiento", description = "Filtra celulares por capacidad de almacenamiento en GB. Ej: min=64&max=256.")
    @GetMapping("/almacenamiento")
    public ResponseEntity<List<CelularResponseDTO>> buscarPorRangoAlmacenamiento(@RequestParam int min, @RequestParam int max) {
        return ResponseEntity.ok(celularService.buscarPorRangoAlmacenamiento(min, max));
    }
}
