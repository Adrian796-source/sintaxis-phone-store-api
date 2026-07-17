package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para registro de usuario con creación automática de cliente")
public class RegisterRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Schema(description = "Nombre del usuario", example = "Juan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Schema(description = "Email del usuario (será el mismo para el cliente)",
            example = "juan.perez@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Schema(description = "Contraseña del usuario (mínimo 6 caracteres)",
            example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    @Schema(description = "Rol del usuario",
            example = "CLIENTE",
            allowableValues = {"ADMIN", "EMPLEADO", "CLIENTE"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String rol;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Schema(description = "Apellido del cliente", example = "Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apellido;

    @Schema(description = "Teléfono del cliente", example = "+54 9 11 1234-5678")
    private String telefono;

    @Schema(description = "Dirección del cliente", example = "Av. Corrientes 1234, CABA")
    private String direccion;

    @Schema(description = "ID de cliente existente para asociar (opcional). Si se envía, no se crea cliente nuevo",
            example = "1")
    private Long idCliente;
}