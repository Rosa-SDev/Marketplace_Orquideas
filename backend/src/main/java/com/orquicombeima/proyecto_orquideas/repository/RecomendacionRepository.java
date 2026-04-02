package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.RecomendacionMaceta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecomendacionRepository extends JpaRepository<RecomendacionMaceta, Long> {
    // Metodo para listar las recomendaciones de maceta por el id de la orquidea
    List<RecomendacionMaceta> findByOrquideaId(Long orquideaId);

    // Metodo para listar las recomendaciones de maceta por el id de la maceta
    List<RecomendacionMaceta> findByMacetaId(Long macetaId);
}
