package com.adrian.sintaxis.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductoResponseDTO {
    private Long idProducto;
    private String nombre;
    private String descripcion;
    private Double precio;
    private int stock;
    private int stockMinimo;
    private String marca;
    private String categoria;
    private LocalDateTime fechaAlta;
    private boolean activo;
    private String imagenUrl;
}