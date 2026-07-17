package com.adrian.sintaxis.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Celular extends Producto
{
    private String modelo;
    private int almacenamientoGB;
    private int ramGB;
    private String color;
    private String procesador;
    private double pantallaPulgadas;
    private int bateriaMAh;
    private String sistemaOperativo;
    private boolean esLibre;


    @Override
    public String mostrarDetalles() {
        return String.format("%s - %s (%s) | %d GB RAM, %d GB Storage | %s | %d mAh | %s",
                nombre, marca, sistemaOperativo, ramGB, almacenamientoGB, procesador, bateriaMAh,
                color);
    }
}
