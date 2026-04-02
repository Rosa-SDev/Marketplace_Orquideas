package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repositorio para acceder a los datos de orquídeas en la BD
// JpaRepository<Orquidea, Long> nos da métodos predefinidos para hacer CRUD (findAll, findById, save, deleteById...) sin tener que escribir código SQL

@Repository
public interface OrquideaRepository extends JpaRepository<Orquidea, Long> {

}