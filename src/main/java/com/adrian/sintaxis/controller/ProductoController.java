package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.ProductoResponseDTO;
import com.adrian.sintaxis.service.IProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final IProductoService productoService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/categoria")
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorCategoria(@RequestParam String categoria) {
        return ResponseEntity.ok(productoService.buscarPorCategoria(categoria));
    }

    @GetMapping("/marca")
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorMarca(@RequestParam String marca) {
        return ResponseEntity.ok(productoService.buscarPorMarca(marca));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ProductoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(productoService.listarActivos());
    }

    @GetMapping("/con-stock")
    public ResponseEntity<List<ProductoResponseDTO>> listarConStock() {
        return ResponseEntity.ok(productoService.listarConStock());
    }
}