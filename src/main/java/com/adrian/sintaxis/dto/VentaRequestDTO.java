package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "DTO para crear una nueva venta")
public class VentaRequestDTO {

    @NotNull(message = "El cliente es obligatorio")
    @Schema(description = "ID del cliente que realiza la compra",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCliente;

    @NotBlank(message = "El estado es obligatorio")
    @Schema(description = "Estado de la venta",
            example = "PENDIENTE",
            allowableValues = {"PENDIENTE", "CONFIRMADO", "ENVIADO", "ENTREGADO", "CANCELADO"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String estado;

    @NotBlank(message = "El método de pago es obligatorio")
    @Schema(description = "Método de pago utilizado",
            example = "TARJETA_CREDITO",
            allowableValues = {"TARJETA_CREDITO", "TARJETA_DEBITO", "EFECTIVO", "TRANSFERENCIA", "MERCADO_PAGO"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String metodoPago;

    @Schema(description = "Descuento aplicado a la venta (opcional)",
            example = "10.0",
            defaultValue = "0.0")
    private Double descuento;

    @NotEmpty(message = "La venta debe tener al menos un producto")
    @Valid
    @Schema(description = "Lista de productos incluidos en la venta",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<DetalleVentaRequestDTO> detalles;
}