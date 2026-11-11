package com.adrian.sintaxis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubcategoriaDTO {
    private Long idSubcategoria;
    @NotBlank(message = "El nombre de la subcategoría es obligatorio")
    private String nombre;
}
