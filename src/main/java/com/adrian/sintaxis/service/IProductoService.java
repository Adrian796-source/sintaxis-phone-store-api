package com.adrian.sintaxis.service;

import com.adrian.sintaxis.model.Producto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IProductoService {

    Producto guardar(Producto producto);
    Optional<Producto> buscarPorId(Long id);
    List<Producto> listarTodos();
    Producto actualizar(Long id, Producto producto);
    void eliminar(Long id);

    List<Producto> buscarPorCategoria(String categoria);
    List<Producto> buscarPorMarca(String marca);
    List<Producto> listarActivos();
    List<Producto> listarConStock();
    void reducirStock(Long id, int cantidad);

    //####### Aca agrego un metodo para guardar imagenes del producto
    String guardarImagen(MultipartFile imagen);

    //####### Aca agrego un metodo para obtener y servir la imagen del producto
    ResponseEntity<Resource> obtenerImagen(String nombreArchivo);
}
