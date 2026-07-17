package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para login de usuario")
public class LoginRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Schema(description = "Email del usuario",
            example = "usuario@email.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña del usuario",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}