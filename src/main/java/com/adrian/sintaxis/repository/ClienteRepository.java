package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    Optional<Cliente> findByEmail(String email);
    List<Cliente> findByEsVipTrue();
}
