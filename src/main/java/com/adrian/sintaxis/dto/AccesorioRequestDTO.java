package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccesorioRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre del accesorio",
            example = "Funda de Silicona",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Schema(description = "Descripción detallada del accesorio",
            example = "Funda de silicona flexible para iPhone 15 Pro Max, color negro")

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    @Schema(description = "Precio del accesorio",
            example = "25.99",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Double precio;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    @Schema(description = "Cantidad disponible en stock",
            example = "100",
            defaultValue = "0")
    private int stock;


    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    @Schema(description = "Stock mínimo para alertas de reposición",
            example = "10",
            defaultValue = "5")
    private int stockMinimo;

    @NotBlank(message = "La marca es obligatoria")
    @Schema(description = "Marca del accesorio",
            example = "Samsung",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String marca;

    @NotBlank(message = "El tipo de accesorio es obligatorio")
    @Schema(description = "Tipo de accesorio",
            example = "Funda",
            allowableValues = {"Funda", "Protector de Pantalla", "Cargador", "Auriculares", "Cable", "Soporte", "Otro"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String tipoAccesorio;

    @Schema(description = "Color del accesorio",
            example = "Negro")
    private String color;

    @Schema(description = "Material del accesorio",
            example = "Silicona")
    private String material;

    @Schema(description = "Indica si el accesorio es original",
            example = "true")
    private boolean esOriginal;
    @Schema(description = "Lista de marcas de celulares compatibles con el accesorio",
            example = "[\"iPhone 15 Pro Max\", \"Samsung Galaxy S24 Ultra\", \"Google Pixel 8\"]")
    private List<String> marcasCompatibles;
    @Schema(description = "URL de la imagen del accesorio",
            example = "https://ejemplo.com/imagenes/funda-silicona.jpg")
    private String imagenUrl;
}
