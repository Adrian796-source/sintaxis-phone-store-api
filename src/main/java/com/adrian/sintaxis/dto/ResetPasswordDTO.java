package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para restablecer la contraseña mediante token")
public class ResetPasswordDTO {

    @NotBlank(message = "El código de verificación es obligatorio")
    @Size(min = 6, max = 6, message = "El código de recuperación debe tener exactamente 6 dígitos")
    @Schema(description = "Código OTP de 6 dígitos recibido", example = "482910", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    @Schema(description = "Nueva contraseña (mínimo 6 caracteres)", example = "nuevaClave123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String passwordNueva;
}
