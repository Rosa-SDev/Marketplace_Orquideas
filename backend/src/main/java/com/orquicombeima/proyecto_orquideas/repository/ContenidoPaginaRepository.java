package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.ContenidoPagina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenidoPaginaRepository extends JpaRepository<ContenidoPagina, Long> {
    // Metodo para buscar un cotenido de pagina por el tipo de contenido
    List<ContenidoPagina> findByTipoOrderByOrdenAsc(String tipo);

    // Metodo para buscar un contenido de pagina por el id del administrador
    List<ContenidoPagina> findByAdministradorId(Long administradorId);
}
