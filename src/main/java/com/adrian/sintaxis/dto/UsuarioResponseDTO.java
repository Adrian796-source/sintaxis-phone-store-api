package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para gestión de usuarios")
public class UsuarioResponseDTO {

    @Schema(description = "ID del usuario", example = "1")
    private Long idUsuario;

    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @Schema(description = "Email del usuario", example = "juan@email.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "EMPLEADO")
    private String rol;

    @Schema(description = "Estado activo del usuario", example = "true")
    private boolean activo;
}
