package com.adrian.sintaxis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    private String nombre;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    private boolean activo;

    @OneToOne
    @JoinColumn(name = "cliente_id_cliente")
    @JsonIgnore
    private Cliente cliente;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;
}
