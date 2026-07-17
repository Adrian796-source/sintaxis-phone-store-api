package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para el perfil completo del usuario con datos del cliente")
public class PerfilResponseDTO {

    @Schema(description = "ID del usuario", example = "1")
    private Long idUsuario;

    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @Schema(description = "Email del usuario", example = "juan@email.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "CLIENTE")
    private String rol;

    @Schema(description = "ID del cliente asociado (null si no es CLIENTE)", example = "1")
    private Long idCliente;

    @Schema(description = "Nombre del cliente asociado", example = "Juan")
    private String nombreCliente;

    @Schema(description = "Apellido del cliente asociado", example = "Pérez")
    private String apellidoCliente;

    @Schema(description = "Teléfono del cliente", example = "+54 9 11 1234-5678")
    private String telefono;

    @Schema(description = "Dirección del cliente", example = "Av. Corrientes 1234, CABA")
    private String direccion;

    @Schema(description = "Indica si el cliente es VIP", example = "false")
    private Boolean esVip;

    @Schema(description = "Puntos acumulados del cliente", example = "0")
    private Integer puntosAcumulados;
}