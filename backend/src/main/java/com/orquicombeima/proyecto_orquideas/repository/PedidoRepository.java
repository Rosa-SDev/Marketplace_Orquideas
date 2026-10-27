package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    //Metodo para encontrar un pedido por el id del usuario
    List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Long usuarioId);

    // Metodo para encontrar los pedidos del mes actual
    List<Pedido> findByFechaPedidoBetween(LocalDateTime inicio, LocalDateTime fin);

    // Metodo para encontrar los pedidos por estado
    List<Pedido> findByEstado(EstadoPedido estado);

    // Metodo para encontrar el total de ventas por mes de los ultimos 6 meses
    // Se usa EXTRACT (estándar HQL) en lugar de MONTH()/YEAR() nativos de MySQL para que funcione en PostgreSQL
    @Query("SELECT EXTRACT(MONTH FROM p.fechaPedido) as mes, " +
            "EXTRACT(YEAR FROM p.fechaPedido) as anio, " +
            "SUM(p.total) as total " +
            "FROM Pedido p " +
            "WHERE p.fechaPedido >= :fechaInicio AND p.estado = 'PAGADO' " +
            "GROUP BY EXTRACT(YEAR FROM p.fechaPedido), EXTRACT(MONTH FROM p.fechaPedido) " +
            "ORDER BY EXTRACT(YEAR FROM p.fechaPedido) ASC, EXTRACT(MONTH FROM p.fechaPedido) ASC")
    List<Object[]> findVentasPorMes(@Param("fechaInicio") LocalDateTime fechaInicio);

    // Metodo para encontrar los ultimmos 5 pedidos
    List<Pedido> findTop5ByOrderByFechaPedidoDesc();

    // Metodo para listar todos los pedidos
    List<Pedido> findAllByOrderByFechaPedidoAsc();
}
