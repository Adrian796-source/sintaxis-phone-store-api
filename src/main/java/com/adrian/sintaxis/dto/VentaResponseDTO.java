package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "DTO de respuesta para una venta con todos sus datos")
public class VentaResponseDTO {

    @Schema(description = "ID único de la venta", example = "1")
    private Long idVenta;

    @Schema(description = "Fecha y hora de la venta", example = "2026-07-11T19:30:00")
    private LocalDateTime fecha;

    @Schema(description = "Estado de la venta",
            example = "CONFIRMADO",
            allowableValues = {"PENDIENTE", "CONFIRMADO", "ENVIADO", "ENTREGADO", "CANCELADO"})
    private String estado;

    @Schema(description = "Método de pago utilizado",
            example = "TARJETA_CREDITO",
            allowableValues = {"TARJETA_CREDITO", "TARJETA_DEBITO", "EFECTIVO", "TRANSFERENCIA", "MERCADO_PAGO"})
    private String metodoPago;

    @Schema(description = "Subtotal de la venta (suma de precios sin descuento)",
            example = "3000.00")
    private Double subtotal;

    @Schema(description = "Descuento aplicado a la venta",
            example = "10.0",
            defaultValue = "0.0")
    private Double descuento;

    @Schema(description = "Total de la venta (subtotal - descuento)",
            example = "2990.00")
    private Double total;

    @Schema(description = "ID del cliente que realizó la compra", example = "1")
    private Long idCliente;

    @Schema(description = "Nombre del cliente", example = "Juan")
    private String nombreCliente;

    @Schema(description = "Apellido del cliente", example = "Pérez")
    private String apellidoCliente;

    @Schema(description = "Lista de detalles de los productos vendidos")
    private List<DetalleVentaResponseDTO> detalles;
}