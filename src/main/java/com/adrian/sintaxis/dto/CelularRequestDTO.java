package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para crear o actualizar un celular")
public class CelularRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre del celular",
            example = "Samsung Galaxy S24 Ultra",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Schema(description = "Descripción detallada del celular",
            example = "Smartphone de alta gama con pantalla AMOLED de 6.8 pulgadas")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    @Schema(description = "Precio del celular",
            example = "1499.99",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Double precio;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    @Schema(description = "Cantidad disponible en stock",
            example = "50",
            defaultValue = "0")
    private int stock;

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    @Schema(description = "Stock mínimo para alertas de reposición",
            example = "5",
            defaultValue = "5")
    private int stockMinimo;

    @NotBlank(message = "La marca es obligatoria")
    @Schema(description = "Marca del celular",
            example = "Samsung",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Schema(description = "Modelo del celular",
            example = "Galaxy S24 Ultra",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String modelo;

    @Positive(message = "El almacenamiento debe ser mayor a 0")
    @Schema(description = "Capacidad de almacenamiento en GB",
            example = "256",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private int almacenamientoGB;

    @Positive(message = "La RAM debe ser mayor a 0")
    @Schema(description = "Memoria RAM en GB",
            example = "12",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private int ramGB;

    @Schema(description = "Color del celular", example = "Negro")
    private String color;

    @NotBlank(message = "El procesador es obligatorio")
    @Schema(description = "Procesador del celular",
            example = "Snapdragon 8 Gen 3",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String procesador;

    @Positive(message = "El tamaño de pantalla debe ser mayor a 0")
    @Schema(description = "Tamaño de la pantalla en pulgadas",
            example = "6.8",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private double pantallaPulgadas;

    @Positive(message = "La batería debe ser mayor a 0")
    @Schema(description = "Capacidad de la batería en mAh",
            example = "5000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private int bateriaMAh;

    @NotBlank(message = "El sistema operativo es obligatorio")
    @Schema(description = "Sistema operativo del celular",
            example = "Android 14",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String sistemaOperativo;

    @Schema(description = "Indica si el celular está liberado (sin operador)",
            example = "true",
            defaultValue = "false")
    private boolean esLibre;

    @Schema(description = "URL de la imagen del celular",
            example = "https://ejemplo.com/imagenes/samsung-s24-ultra.jpg")
    private String imagenUrl;
}
