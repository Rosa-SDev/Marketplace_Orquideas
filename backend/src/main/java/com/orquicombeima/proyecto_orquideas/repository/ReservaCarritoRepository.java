package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.ReservaCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaCarritoRepository extends JpaRepository<ReservaCarrito, Long> {
    // Metodo para buscar una reserva por el id del carrito
    List<ReservaCarrito> findByCarritoId(Long carritoId);
}
