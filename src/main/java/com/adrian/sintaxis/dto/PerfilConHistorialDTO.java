package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "DTO para el perfil completo del cliente con su historial de compras")
public class PerfilConHistorialDTO {

    // ========== DATOS DEL CLIENTE ==========

    @Schema(description = "ID del cliente", example = "1")
    private Long idCliente;

    @Schema(description = "Nombre del cliente", example = "Juan")
    private String nombre;

    @Schema(description = "Apellido del cliente", example = "Pérez")
    private String apellido;

    @Schema(description = "Email del cliente", example = "juan.perez@email.com")
    private String email;

    @Schema(description = "Teléfono del cliente", example = "+54 9 11 1234-5678")
    private String telefono;

    @Schema(description = "Dirección del cliente", example = "Av. Corrientes 1234, CABA")
    private String direccion;

    @Schema(description = "Indica si el cliente es VIP", example = "false")
    private boolean esVip;

    @Schema(description = "Puntos acumulados por el cliente", example = "0")
    private int puntosAcumulados;

    @Schema(description = "Fecha de registro del cliente", example = "2026-07-11T19:30:00")
    private LocalDateTime fechaRegistro;

    // ========== HISTORIAL DE COMPRAS ==========

    @Schema(description = "Lista de todas las compras realizadas por el cliente")
    private List<VentaResponseDTO> historialCompras;
}