package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.AccesorioRequestDTO;
import com.adrian.sintaxis.dto.AccesorioResponseDTO;
import com.adrian.sintaxis.service.IAccesorioService;
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
@RequestMapping("/api/accesorios")
@RequiredArgsConstructor
@Tag(name = "Accesorios", description = "Gestión de accesorios. Los GET son públicos, POST/PUT/DELETE requieren rol ADMIN o EMPLEADO.")
public class AccesorioController {

    private final IAccesorioService accesorioService;

    // Acepta imagen
    @Operation(summary = "Crear accesorio con imagen", description = "Registra un nuevo accesorio en el sistema. Requiere rol ADMIN o EMPLEADO.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AccesorioResponseDTO> guardarConImagen(
            @RequestPart("accesorio") @Valid AccesorioRequestDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accesorioService.guardar(dto, imagen));
    }

    //  POST sin imagen (para compatibilidad)
    @Operation(summary = "Crear accesorio sin imagen", description = "Registra un nuevo accesorio en el sistema. Requiere rol ADMIN o EMPLEADO.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccesorioResponseDTO> guardar(@Valid @RequestBody AccesorioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accesorioService.guardar(dto, null));
    }

    @Operation(summary = "Buscar accesorio por ID", description = "Devuelve los datos completos de un accesorio por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<AccesorioResponseDTO> buscarPorId(@PathVariable Long id) {
        return accesorioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los accesorios", description = "Devuelve todos los accesorios paginados, ordenados por nombre.")
    @GetMapping
    public ResponseEntity<Page<AccesorioResponseDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(accesorioService.listarTodos(pageable));
    }

    @Operation(summary = "Actualizar accesorio", description = "Modifica los datos de un accesorio existente. Requiere rol ADMIN o EMPLEADO.")
    @PutMapping("/{id}")
    public ResponseEntity<AccesorioResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody AccesorioRequestDTO dto) {
        return ResponseEntity.ok(accesorioService.actualizar(id, dto));
    }

    @Operation(summary = "Eliminar accesorio", description = "Borrado lógico: marca el accesorio como inactivo sin eliminarlo de la base de datos. Requiere rol ADMIN o EMPLEADO.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        accesorioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar accesorios con filtros", description = "Filtra accesorios por marca, tipo, rango de precio y si es original. Todos los parámetros son opcionales y combinables.")
    @GetMapping("/buscar")
    public ResponseEntity<Page<AccesorioResponseDTO>> buscarConFiltros(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String tipoAccesorio,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Boolean esOriginal,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(accesorioService.buscarConFiltros(marca, tipoAccesorio, precioMin, precioMax, esOriginal, pageable));
    }

    @Operation(summary = "Reponer stock", description = "Suma la cantidad indicada al stock actual del accesorio. Requiere rol ADMIN o EMPLEADO.")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<AccesorioResponseDTO> reponerStock(@PathVariable Long id, @RequestParam int cantidad) {
        return ResponseEntity.ok(accesorioService.reponerStock(id, cantidad));
    }

    @Operation(summary = "Accesorios con stock bajo", description = "Devuelve los accesorios cuyo stock actual es menor o igual al stock mínimo configurado.")
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<AccesorioResponseDTO>> stockBajo() {
        return ResponseEntity.ok(accesorioService.stockBajo());
    }

    @Operation(summary = "Buscar por tipo", description = "Devuelve todos los accesorios de un tipo específico. Ej: Cable de carga, Funda, Auriculares.")
    @GetMapping("/tipo")
    public ResponseEntity<List<AccesorioResponseDTO>> buscarPorTipo(@RequestParam String tipo) {
        return ResponseEntity.ok(accesorioService.buscarPorTipo(tipo));
    }

    @Operation(summary = "Listar accesorios originales", description = "Devuelve todos los accesorios que son originales del fabricante.")
    @GetMapping("/originales")
    public ResponseEntity<List<AccesorioResponseDTO>> buscarOriginales() {
        return ResponseEntity.ok(accesorioService.buscarOriginales());
    }

    @Operation(summary = "Buscar por marca compatible", description = "Devuelve accesorios compatibles con una marca específica. Ej: Samsung, Apple, Motorola.")
    @GetMapping("/marca-compatible")
    public ResponseEntity<List<AccesorioResponseDTO>> buscarPorMarcaCompatible(@RequestParam String marca) {
        return ResponseEntity.ok(accesorioService.buscarPorMarcaCompatible(marca));
    }
}
