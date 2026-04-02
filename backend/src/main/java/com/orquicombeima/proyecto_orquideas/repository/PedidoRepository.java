package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    //Metodo para encontrar un pedido por el id del cliente
    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);
}
