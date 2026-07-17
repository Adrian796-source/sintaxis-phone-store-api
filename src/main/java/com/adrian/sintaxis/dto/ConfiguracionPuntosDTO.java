package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para la configuración de puntos de fidelización")
public class ConfiguracionPuntosDTO {

    @Positive(message = "Los puntos para VIP deben ser mayor a 0")
    @Schema(description = "Puntos necesarios para que un cliente sea VIP",
            example = "100",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private int puntosParaVip;

    @Positive(message = "Los pesos por punto deben ser mayor a 0")
    @Schema(description = "Valor en pesos de cada punto acumulado",
            example = "10.0",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private double pesosPorPunto;
}