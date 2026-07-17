package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoria(String categoria);
    List<Producto> findByMarca(String marca);
    List<Producto> findByActivoTrue();
    List<Producto> findByStockGreaterThan(int stock);
    //####### Aca agrego un metodo para traer las imagenes
    List<Producto> findByNombreContaining(String nombre);


    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();
}
