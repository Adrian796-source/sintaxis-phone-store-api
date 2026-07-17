package com.adrian.sintaxis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "DTO para reportes de ventas con etiqueta y valor")
public class ReporteVentaDTO {

    @Schema(description = "Etiqueta o nombre del dato en el reporte",
            example = "Total de ventas",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String etiqueta;

    @Schema(description = "Valor del dato en el reporte (puede ser numérico, texto o estadístico)",
            example = "15000.50",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Object valor;
}