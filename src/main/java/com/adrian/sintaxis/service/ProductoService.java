package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.ProductoResponseDTO;
import com.adrian.sintaxis.model.Producto;
import com.adrian.sintaxis.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adrian.sintaxis.exception.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoService {

    private final ProductoRepository productoRepository;

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    //####### Aca agrego anotation para traer las imagenes
    @Value("${upload.path:uploads/}")
    private String uploadPath;

    // Método de conversión Producto -> ProductoResponseDTO
    private ProductoResponseDTO toDTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setIdProducto(producto.getIdProducto());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setMarca(producto.getMarca());
        dto.setCategoria(producto.getCategoria());
        dto.setFechaAlta(producto.getFechaAlta());
        dto.setActivo(producto.isActivo());
        dto.setImagenUrl(producto.getImagenUrl());
        return dto;
    }

    private List<ProductoResponseDTO> toDTOList(List<Producto> productos) {
        return productos.stream().map(this::toDTO).toList();
    }

    @Override
    public Optional<ProductoResponseDTO> buscarPorId(Long id) {
        return Optional.of(productoRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado")));
    }

    @Override
    public List<ProductoResponseDTO> listarTodos() {
        return productoRepository.findAll().stream().map(this::toDTO).toList();
    }

    @SuppressWarnings("Duplicates")
    @Override
    public List<ProductoResponseDTO> buscarPorCategoria(String categoria) {
        return toDTOList(productoRepository.findByCategoria(categoria));
    }

    @SuppressWarnings("Duplicates")
    @Override
    public List<ProductoResponseDTO> buscarPorMarca(String marca) {
        return toDTOList(productoRepository.findByMarca(marca));
    }

    @SuppressWarnings("Duplicates")
    @Override
    public List<ProductoResponseDTO> listarActivos() {
        return toDTOList(productoRepository.findByActivoTrue());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public List<ProductoResponseDTO> listarConStock() {
        return toDTOList(productoRepository.findByStockGreaterThan(0));
    }

    @Override
    public void reducirStock(Long id, int cantidad) {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException("La cantidad debe ser mayor a 0");
        }

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));
        producto.reducirStock(cantidad);
        productoRepository.save(producto);
    }

    //####### Aca agrego un metodo para traer las imagenes
    @Override
    public String guardarImagen(MultipartFile imagen) {
        try {
            if (imagen == null || imagen.isEmpty()) {
                return null;
            }

            // Validar que sea una imagen
            String contentType = imagen.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new TipoArchivoInvalidoException("El archivo debe ser una imagen");
            }

            // Generar nombre único
            String nombreOriginal = imagen.getOriginalFilename();
            String extension = "";
            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            }

            String nombreArchivo = UUID.randomUUID().toString() + "_" +
                    System.currentTimeMillis() + extension;

            // Crear ruta completa
            Path rutaCompleta = Paths.get(uploadPath, nombreArchivo);

            // Crear directorios si no existen
            Files.createDirectories(rutaCompleta.getParent());

            // Guardar archivo
            Files.write(rutaCompleta, imagen.getBytes());

            // Devolver URL para acceder a la imagen
            return "/api/productos/imagenes/" + nombreArchivo;

        } catch (IOException e) {
            throw new ImagenStorageException("Error al guardar la imagen: " + e.getMessage(), e);
        }
    }

    // 📸 OBTENER IMAGEN - CORREGIDO (devuelve ResponseEntity<Resource>)
    @Override
    public ResponseEntity<Resource> obtenerImagen(String nombreArchivo) {
        try {
            Path ruta = Paths.get(uploadPath, nombreArchivo);

            // Verificar si el archivo existe
            if (!Files.exists(ruta)) {
                return ResponseEntity.notFound().build();
            }

            // Crear recurso
            Resource recurso = new UrlResource(ruta.toUri());

            if (recurso.exists() && recurso.isReadable()) {
                // ✅ CORRECTO: Devolver la imagen
                return ResponseEntity.ok(recurso);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error al obtener la imagen: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }
}