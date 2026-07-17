package com.adrian.sintaxis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenta;
    private LocalDateTime fecha;
    private String estado; // "Pendiente", "Pagada", "Entregada", "Cancelada"
    private String metodoPago; // "Efectivo", "Tarjeta", "Transferencia"
    private Double subtotal;
    private Double descuento;
    private Double total;
    private boolean activo;
    @ManyToOne
    @JoinColumn(name = "cliente_id_cliente")
    private Cliente cliente;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles;
}
