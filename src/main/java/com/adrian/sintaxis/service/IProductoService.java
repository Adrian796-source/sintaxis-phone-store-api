package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.ProductoResponseDTO;
import com.adrian.sintaxis.model.Producto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IProductoService {

    Optional<ProductoResponseDTO> buscarPorId(Long id);
    List<ProductoResponseDTO> listarTodos();
    List<ProductoResponseDTO> buscarPorCategoria(String categoria);
    List<ProductoResponseDTO> buscarPorMarca(String marca);
    List<ProductoResponseDTO> listarActivos();
    List<ProductoResponseDTO> listarConStock();
    void reducirStock(Long id, int cantidad);

    //####### Aca agrego un metodo para guardar imagenes del producto
    String guardarImagen(MultipartFile imagen);

    //####### Aca agrego un metodo para obtener y servir la imagen del producto
    ResponseEntity<Resource> obtenerImagen(String nombreArchivo);
}