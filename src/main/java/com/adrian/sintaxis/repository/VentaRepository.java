package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto LEFT JOIN FETCH v.cliente")
    List<Venta> findAllWithDetalles();

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto LEFT JOIN FETCH v.cliente WHERE v.cliente.idCliente = :idCliente")
    List<Venta> findByClienteIdCliente(@Param("idCliente") Long idCliente);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto LEFT JOIN FETCH v.cliente WHERE v.estado = :estado")
    List<Venta> findByEstado(@Param("estado") String estado);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto LEFT JOIN FETCH v.cliente WHERE v.fecha BETWEEN :desde AND :hasta")
    List<Venta> findByFechaBetween(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha BETWEEN :desde AND :hasta AND v.estado = 'Pagada'")
    Double totalRecaudadoEntreFechas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT v.estado, COUNT(v) FROM Venta v GROUP BY v.estado")
    List<Object[]> contarPorEstado();

    @Query("SELECT v.cliente.idCliente, v.cliente.nombre, v.cliente.apellido, COUNT(v), SUM(v.total) FROM Venta v WHERE v.estado = 'Pagada' GROUP BY v.cliente.idCliente, v.cliente.nombre, v.cliente.apellido ORDER BY SUM(v.total) DESC")
    List<Object[]> topClientes();
}
