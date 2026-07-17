package com.adrian.sintaxis.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Producto {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    protected Long idProducto;
    protected String nombre;
    protected String descripcion;
    protected Double precio;
    protected int stock;
    protected String marca;
    protected String categoria; // "Celular" o "Accesorio"
    protected LocalDateTime fechaAlta;
    protected boolean activo;
    protected int stockMinimo;
    // ####### Esto es lo que he agregado para
    private String imagenUrl;

    // Método abstracto
    public abstract String mostrarDetalles(); // Nombre en minúscula (convención Java)

    // Métodos concretos
    public boolean tieneStock() {
        return stock > 0;
    }

    public void reducirStock(int cantidad) {
        if (stock >= cantidad) {
            stock -= cantidad;
        } else {
            throw new RuntimeException("Stock insuficiente");
        }
    }

    public void devolverStock(int cantidad) {
        stock += cantidad;
    }
}


