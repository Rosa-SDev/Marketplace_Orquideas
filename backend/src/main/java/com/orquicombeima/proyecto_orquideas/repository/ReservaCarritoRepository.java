package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.ReservaCarrito;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaCarritoRepository extends JpaRepository<ReservaCarrito, Long> {

    // Metodo para buscar todas las reservas de un carrito (sin importar el estado)
    List<ReservaCarrito> findByCarritoId(Long carritoId);

    // Metodo para buscar las reservas de un carrito filtradas por estado
    // Lo usamos para confirmar o cancelar solo las reservas que están ACTIVAS
    List<ReservaCarrito> findByCarritoIdAndEstado(Long carritoId, EstadoReserva estado);

    // Metodo para buscar reservas que están activas pero cuya fecha de expiración ya pasó
    // La tarea programada usa esto para liberar las reservas vencidas cada 5 minutos
    List<ReservaCarrito> findByEstadoAndFechaExpiracionBefore(EstadoReserva estado, LocalDateTime fecha);
}
