package com.adrian.sintaxis.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subcategorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subcategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSubcategoria;

    @NotBlank(message = "El nombre de la subcategoría es obligatorio")
    @Column(unique = true, nullable = false)
    private String nombre;
}
