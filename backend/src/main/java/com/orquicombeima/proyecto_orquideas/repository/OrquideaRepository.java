package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositorio para acceder a los datos de orquídeas en la BD

// @Repository le dice a Spring que esta interfaz maneja acceso a la BD
// JpaRepository<Orquidea, Long> nos da métodos predefinidos para hacer CRUD (findAll, findById, save, deleteById...) sin tener que escribir código SQL
@Repository
public interface OrquideaRepository extends JpaRepository<Orquidea, Long> {
    // Metodo para buscar orquideas que esten activas
    List<Orquidea> findByActivoTrue();

    // Metodo para buscar orquideas por variedad
    List<Orquidea> findByVariedad(String variedad);
}