package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "DTO de respuesta para un accesorio con todos sus datos")
public class AccesorioResponseDTO {

    @Schema(description = "ID único del producto", example = "1")
    private Long idProducto;

    @Schema(description = "Nombre del accesorio", example = "Funda de Silicona")
    private String nombre;

    @Schema(description = "Descripción detallada del accesorio",
            example = "Funda de silicona flexible para iPhone 15 Pro Max, color negro")
    private String descripcion;

    @Schema(description = "Precio del accesorio", example = "25.99")
    private Double precio;

    @Schema(description = "Cantidad disponible en stock", example = "100")
    private int stock;

    @Schema(description = "Stock mínimo para alertas de reposición", example = "10")
    private int stockMinimo;

    @Schema(description = "Marca del accesorio", example = "Samsung")
    private String marca;

    @Schema(description = "Categoría del producto", example = "Accesorios")
    private String categoria;

    @Schema(description = "Fecha de alta del producto", example = "2026-07-11T19:30:00")
    private LocalDateTime fechaAlta;

    @Schema(description = "Indica si el producto está activo", example = "true")
    private boolean activo;

    @Schema(description = "Tipo de accesorio",
            example = "Funda",
            allowableValues = {"Funda", "Protector de Pantalla", "Cargador", "Auriculares", "Cable", "Soporte", "Otro"})
    private String tipoAccesorio;

    @Schema(description = "Color del accesorio", example = "Negro")
    private String color;

    @Schema(description = "Material del accesorio", example = "Silicona")
    private String material;

    @Schema(description = "Indica si el accesorio es original de la marca", example = "true")
    private boolean esOriginal;

    @Schema(description = "Lista de marcas de celulares compatibles con el accesorio",
            example = "[\"iPhone 15 Pro Max\", \"Samsung Galaxy S24 Ultra\", \"Google Pixel 8\"]")
    private List<String> marcasCompatibles;

    @Schema(description = "URL de la imagen del accesorio",
            example = "https://ejemplo.com/imagenes/funda-silicona.jpg")
    private String imagenUrl;
}
