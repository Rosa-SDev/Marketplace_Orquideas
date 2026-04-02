package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.GuiaCuidado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuiaCuidadoRepository extends JpaRepository<GuiaCuidado, Long> {
    // Metodo para buscar una guia de cuidado con el id de la orquidea
    Optional<GuiaCuidado> findByOrquideaId(Long orquideaId);

    // Metodo para encontrar una guia de cuidado por la variedad
    Optional<GuiaCuidado> findByVariedad(String variedad);
}
