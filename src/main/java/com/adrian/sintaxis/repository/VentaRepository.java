package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByClienteIdCliente(Long idCliente);
    List<Venta> findByEstado(String estado);
    List<Venta> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha BETWEEN :desde AND :hasta AND v.estado = 'Pagada'")
    Double totalRecaudadoEntreFechas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT v.estado, COUNT(v) FROM Venta v GROUP BY v.estado")
    List<Object[]> contarPorEstado();

    @Query("SELECT v.cliente.idCliente, v.cliente.nombre, v.cliente.apellido, COUNT(v), SUM(v.total) FROM Venta v WHERE v.estado = 'Pagada' GROUP BY v.cliente.idCliente, v.cliente.nombre, v.cliente.apellido ORDER BY SUM(v.total) DESC")
    List<Object[]> topClientes();
}
