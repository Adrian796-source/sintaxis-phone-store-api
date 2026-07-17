package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "DTO de respuesta para logout")
public class LogoutResponseDTO {

    @Schema(description = "Mensaje de confirmación",
            example = "Sesión cerrada exitosamente")
    private String mensaje;

    @Schema(description = "Indica si el logout fue exitoso",
            example = "true")
    private boolean success;
}
