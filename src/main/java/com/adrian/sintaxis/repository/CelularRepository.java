package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Celular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface CelularRepository extends JpaRepository<Celular, Long>, JpaSpecificationExecutor<Celular> {
    List<Celular> findByModeloContainingIgnoreCase(String modelo);
    List<Celular> findBySistemaOperativo(String sistemaOperativo);
    List<Celular> findByEsLibreTrue();
    List<Celular> findByAlmacenamientoGBBetween(int min, int max);

    //##### Agrego un metodo para buscar por marca imagenes
    List<Celular> findByMarca(String marca);
}
