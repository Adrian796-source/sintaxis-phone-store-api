package com.adrian.sintaxis.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionPuntos {

    @Id
    private Long id;

    private int puntosParaVip;
    private double pesosPorPunto;
}
