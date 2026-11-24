package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para actualización de usuario por parte del administrador")
public class UsuarioUpdateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Schema(description = "Nombre del usuario", example = "Juan Carlos", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Schema(description = "Email del usuario", example = "juan.carlos@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Nueva contraseña opcional (si se deja en blanco o nula, no se modifica)",
            example = "nuevaPassword123")
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    @Schema(description = "Rol del usuario",
            example = "EMPLEADO",
            allowableValues = {"ADMIN", "EMPLEADO", "CLIENTE"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String rol;

    @Schema(description = "Estado activo del usuario", example = "true")
    private boolean activo = true;
}
