package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.PedidoRecienteDTO;
import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoAdminService {

    private final PedidoRepository pedidoRepository;

    // Metodo GET para obtener los pedidos recientes
    @Transactional(readOnly = true)
    public List<PedidoRecienteDTO> obtenerPedidosRecientes() {
        return pedidoRepository.findTop5ByOrderByFechaPedidoDesc().stream()
                .map(this::convertirAPedidoRecienteDTO)
                .toList();
    }

    // Función que permite convertir un pedido a un pedido reciente DTO
    private PedidoRecienteDTO convertirAPedidoRecienteDTO(Pedido pedido) {
        return PedidoRecienteDTO.builder()
                .id(pedido.getId())
                .nombreCliente(pedido.getUsuario().getNombre())
                .total(pedido.getTotal())
                .estado(pedido.getEstado().name())
                .tiempoTranscurrido(calcularTiempoTranscurrido(pedido.getFechaPedido()))
                .build();
    }

    // Función para calcular el tiempo transcurrido desde que se realizo el pedido
    private String calcularTiempoTranscurrido(LocalDateTime fechaPedido) {
        long minutos = Duration.between(fechaPedido, LocalDateTime.now()).toMinutes();

        if (minutos < 60) {
            return "Hace" + minutos + " minutos";
        } else if (minutos < 1440) {
            long horas = minutos / 60;
            return "Hace " + horas + " horas";
        } else {
            long dias = minutos / 1440;
            return "Hace " + dias + " días";

        }
    }

}
