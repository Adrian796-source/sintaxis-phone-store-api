package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO de respuesta de autenticación con datos del usuario y cliente")
public class AuthResponseDTO {
    // ========== DATOS DEL USUARIO ==========

    @Schema(description = "Token JWT para autenticación", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Email del usuario", example = "juan.perez@email.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "CLIENTE")
    private String rol;

    // ========== DATOS DEL CLIENTE ASOCIADO ==========

    @Schema(description = "ID del cliente asociado", example = "1")
    private Long idCliente;

    @Schema(description = "Nombre del cliente", example = "Juan")
    private String nombreCliente;

    @Schema(description = "Apellido del cliente", example = "Pérez")
    private String apellidoCliente;

    @Schema(description = "Teléfono del cliente", example = "+54 9 11 1234-5678")
    private String telefono;

    @Schema(description = "Dirección del cliente", example = "Av. Corrientes 1234, CABA")
    private String direccion;

    @Schema(description = "Indica si el cliente es VIP", example = "false")
    private Boolean esVip;

    @Schema(description = "Puntos acumulados del cliente", example = "0")
    private Integer puntosAcumulados;

    // Constructor original (para compatibilidad)
    public AuthResponseDTO(String token, String email, String rol) {
        this.token = token;
        this.email = email;
        this.rol = rol;
    }
}
