package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para cambiar la contraseña del usuario")
public class CambiarPasswordDTO {

    @NotBlank(message = "La contraseña actual es obligatoria")
    @Schema(description = "Contraseña actual del usuario",
            example = "oldPassword123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String passwordActual;

    @NotBlank(message = "La contraseña nueva es obligatoria")
    @Size(min = 6, message = "La contraseña nueva debe tener al menos 6 caracteres")
    @Schema(description = "Nueva contraseña (mínimo 6 caracteres)",
            example = "newPassword456",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String passwordNueva;
}