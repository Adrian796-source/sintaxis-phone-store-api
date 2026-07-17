package com.adrian.sintaxis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long idCliente;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private LocalDateTime fechaRegistro;

    // Propiedades para fidelización
    private boolean esVip;
    private int puntosAcumulados;
    private boolean activo;

    //Asigno cliente a usuario, un cliente puede tener un usuario y un usuario puede tener un cliente
    @OneToOne(mappedBy = "cliente")
    private Usuario usuario;


}