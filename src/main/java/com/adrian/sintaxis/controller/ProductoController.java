package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.model.Producto;
import com.adrian.sintaxis.service.IProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final IProductoService productoService;

    @PostMapping
    public ResponseEntity<Producto> guardar(@RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.guardar(producto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizar(id, producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categoria")
    public ResponseEntity<List<Producto>> buscarPorCategoria(@RequestParam String categoria) {
        return ResponseEntity.ok(productoService.buscarPorCategoria(categoria));
    }

    @GetMapping("/marca")
    public ResponseEntity<List<Producto>> buscarPorMarca(@RequestParam String marca) {
        return ResponseEntity.ok(productoService.buscarPorMarca(marca));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> listarActivos() {
        return ResponseEntity.ok(productoService.listarActivos());
    }

    @GetMapping("/con-stock")
    public ResponseEntity<List<Producto>> listarConStock() {
        return ResponseEntity.ok(productoService.listarConStock());
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> reducirStock(@PathVariable Long id, @RequestParam int cantidad) {
        productoService.reducirStock(id, cantidad);
        return ResponseEntity.noContent().build();
    }
}
