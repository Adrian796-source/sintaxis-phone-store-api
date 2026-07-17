package com.adrian.sintaxis.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "DTO de respuesta para un celular con todos sus datos")
public class CelularResponseDTO {

    @Schema(description = "ID único del producto", example = "1")
    private Long idProducto;

    @Schema(description = "Nombre del celular", example = "Samsung Galaxy S24 Ultra")
    private String nombre;

    @Schema(description = "Descripción detallada del celular",
            example = "Smartphone de alta gama con pantalla AMOLED de 6.8 pulgadas")
    private String descripcion;

    @Schema(description = "Precio del celular", example = "1499.99")
    private Double precio;

    @Schema(description = "Cantidad disponible en stock", example = "50")
    private int stock;

    @Schema(description = "Stock mínimo para alertas de reposición", example = "5")
    private int stockMinimo;

    @Schema(description = "Marca del celular", example = "Samsung")
    private String marca;

    @Schema(description = "Categoría del producto", example = "Celulares")
    private String categoria;

    @Schema(description = "Fecha de alta del producto", example = "2026-07-11T19:30:00")
    private LocalDateTime fechaAlta;

    @Schema(description = "Indica si el producto está activo", example = "true")
    private boolean activo;

    @Schema(description = "Modelo del celular", example = "Galaxy S24 Ultra")
    private String modelo;

    @Schema(description = "Capacidad de almacenamiento en GB", example = "256")
    private int almacenamientoGB;

    @Schema(description = "Memoria RAM en GB", example = "12")
    private int ramGB;

    @Schema(description = "Color del celular", example = "Negro")
    private String color;

    @Schema(description = "Procesador del celular", example = "Snapdragon 8 Gen 3")
    private String procesador;

    @Schema(description = "Tamaño de la pantalla en pulgadas", example = "6.8")
    private double pantallaPulgadas;

    @Schema(description = "Capacidad de la batería en mAh", example = "5000")
    private int bateriaMAh;

    @Schema(description = "Sistema operativo del celular", example = "Android 14")
    private String sistemaOperativo;

    @Schema(description = "Indica si el celular está liberado (sin operador)", example = "true")
    private boolean esLibre;

    @Schema(description = "URL de la imagen del celular",
            example = "https://ejemplo.com/imagenes/samsung-s24-ultra.jpg")
    private String imagenUrl;
}