package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.ProductoResponseDTO;
import com.adrian.sintaxis.service.IProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Imágenes", description = "Endpoint para servir recursos de imágenes")
public class ImagenController {

    private final IProductoService productoService;

    @GetMapping("/imagenes/{nombreArchivo:.+}")
    @Operation(summary = "Obtener imagen", description = "Devuelve la imagen asociada al nombre de archivo.")
    public ResponseEntity<Resource> obtenerImagen(@PathVariable String nombreArchivo) {
        return productoService.obtenerImagen(nombreArchivo);
    }
}
