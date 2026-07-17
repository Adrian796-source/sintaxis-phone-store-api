package com.adrian.sintaxis.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Accesorio extends Producto{

    private String tipoAccesorio; // "Funda", "Cargador", "Audifonos", etc.
    private String color;
    private String material;
    private boolean esOriginal;
    @ElementCollection
    private List<String> marcasCompatibles;

    @Override
    public String mostrarDetalles() {
        String compatibilidad = (marcasCompatibles != null && !marcasCompatibles.isEmpty()) 
            ? String.join(", ", marcasCompatibles) 
            : "No especificada";
        return String.format("%s - %s (%s) | %s | %s | Compatible con: %s",
                nombre, tipoAccesorio, material, color,
                esOriginal ? "Original" : "Compatible",
                compatibilidad);
    }
}
