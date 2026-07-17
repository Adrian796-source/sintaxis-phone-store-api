package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO de respuesta para el detalle de una venta")
public class DetalleVentaResponseDTO {

    @Schema(description = "ID único del detalle de venta", example = "1")
    private Long idDetalleVenta;

    @Schema(description = "ID del producto vendido", example = "1")
    private Long idProducto;

    @Schema(description = "Nombre del producto vendido", example = "Samsung Galaxy S24 Ultra")
    private String nombreProducto;

    @Schema(description = "Cantidad vendida", example = "2")
    private int cantidad;

    @Schema(description = "Precio unitario del producto al momento de la venta",
            example = "1499.99")
    private Double precioUnitario;

    @Schema(description = "Subtotal del detalle (cantidad * precioUnitario)",
            example = "2999.98")
    private Double subtotalDetalle;
}