package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para crear o actualizar un cliente")
public class ClienteRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre del cliente",
            example = "Juan",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Schema(description = "Apellido del cliente",
            example = "Pérez",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Schema(description = "Email del cliente",
            example = "juan.perez@email.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 7, max = 15, message = "El teléfono debe tener entre 7 y 15 caracteres")
    @Schema(description = "Teléfono del cliente (entre 7 y 15 caracteres)",
            example = "+54 9 11 1234-5678",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Schema(description = "Dirección del cliente",
            example = "Av. Corrientes 1234, CABA",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String direccion;
}