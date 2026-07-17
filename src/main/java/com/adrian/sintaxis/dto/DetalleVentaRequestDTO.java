package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para el detalle de una venta (producto y cantidad)")
public class DetalleVentaRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Schema(description = "ID del producto (celular o accesorio) a vender",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idProducto;

    @Positive(message = "La cantidad debe ser mayor a 0")
    @Schema(description = "Cantidad de productos a vender",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private int cantidad;
}