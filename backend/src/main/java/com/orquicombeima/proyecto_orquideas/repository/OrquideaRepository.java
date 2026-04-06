package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositorio para acceder a los datos de orquídeas en la BD
@Repository
public interface OrquideaRepository extends JpaRepository<Orquidea, Long> {

    // Metodo para buscar orquideas que esten activas
    List<Orquidea> findByActivoTrue();

    // Metodo para buscar orquideas por variedad
    List<Orquidea> findByVariedad(String variedad);

    // Busca orquideas aplicando filtros opcionales y paginación
    // Si un filtro llega null, se ignora y no afecta la búsqueda
    @Query("SELECT o FROM Orquidea o WHERE " +
            "(:variedad IS NULL OR o.variedad = :variedad) AND " +
            "(:colorFlor IS NULL OR o.colorFlor = :colorFlor) AND " +
            "(:precioMin IS NULL OR o.precio >= :precioMin) AND " +
            "(:precioMax IS NULL OR o.precio <= :precioMax)")
    Page<Orquidea> findWithFilters(
            @Param("variedad") String variedad,
            @Param("colorFlor") String colorFlor,
            @Param("precioMin") Double precioMin,
            @Param("precioMax") Double precioMax,
            Pageable pageable);
}