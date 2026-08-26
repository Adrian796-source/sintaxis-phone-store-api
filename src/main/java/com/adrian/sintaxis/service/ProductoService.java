package com.adrian.sintaxis.service;

import com.adrian.sintaxis.model.Producto;
import com.adrian.sintaxis.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.adrian.sintaxis.exception.*;
import java.time.ZoneId;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoService {

    private final ProductoRepository productoRepository;

    //####### Aca agrego anotation para traer las imagenes
    @Value("${upload.path:uploads/}")
    private String uploadPath;

    @Override
    public Producto guardar(Producto producto) {
        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            throw new PrecioInvalidoException("El precio debe ser mayor a 0");
        }
        if (producto.getStock() < 0) {
            throw new StockInvalidoException("El stock no puede ser negativo");
        }
        producto.setFechaAlta(LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")));
        producto.setActivo(true);
        return productoRepository.save(producto);
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {

        return productoRepository.findById(id);
    }

    @Override
    public List<Producto> listarTodos() {

        return productoRepository.findAll();
    }

    @Override
    public Producto actualizar(Long id, Producto producto) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));

        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            throw new PrecioInvalidoException("El precio debe ser mayor a 0");
        }

        existente.setNombre(producto.getNombre());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPrecio(producto.getPrecio());
        existente.setStock(producto.getStock());
        existente.setMarca(producto.getMarca());
        existente.setCategoria(producto.getCategoria());
        return productoRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    @Override
    public List<Producto> buscarPorMarca(String marca) {
        return productoRepository.findByMarca(marca);
    }

    @Override
    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrue();
    }

    @Override
    public List<Producto> listarConStock() {
        return productoRepository.findByStockGreaterThan(0);
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

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

