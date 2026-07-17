package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para asociar un cliente existente a un usuario")
public class AsociarClienteDTO {

    @Schema(description = "ID del cliente a asociar",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCliente;
}
