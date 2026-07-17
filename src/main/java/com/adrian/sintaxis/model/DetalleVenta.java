package com.adrian.sintaxis.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalleVenta;

    private int cantidad;
    private Double precioUnitario;

    @ManyToOne
    @JoinColumn(name = "venta_id_venta")
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "producto_id_producto")
    private Producto producto;
}
