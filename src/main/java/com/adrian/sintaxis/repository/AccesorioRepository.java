package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Accesorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface AccesorioRepository extends JpaRepository<Accesorio, Long>, JpaSpecificationExecutor<Accesorio> {
    List<Accesorio> findByTipoAccesorio(String tipoAccesorio);
    List<Accesorio> findByEsOriginalTrue();
    List<Accesorio> findByMarcasCompatiblesContaining(String marca);

}

