package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.Maceta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MacetaRepository extends JpaRepository<Maceta, Long> {
    // Metodo para listar macetas por estado activo
    List<Maceta> findByActivoTrue();
}
