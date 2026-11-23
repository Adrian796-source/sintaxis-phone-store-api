package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para solicitar recuperación de contraseña")
public class ForgotPasswordRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe proporcionar un email válido")
    @Schema(description = "Email del usuario", example = "admin@sintaxis.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
