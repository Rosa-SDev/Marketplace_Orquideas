package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.model.PagoWompi;
import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPago;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPedido;
import com.orquicombeima.proyecto_orquideas.repository.PagoWompiRepository;
import com.orquicombeima.proyecto_orquideas.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WompiWebhookService {

    private final PedidoRepository pedidoRepository;
    private final PagoWompiRepository pagoWompiRepository;

    @Transactional
    public void procesarEvento(Map<String, Object> payload) {
        String evento = (String) payload.get("event");

        // En caso de que no se presenten actualizaciones del pago salir
        if (!"transaction.updated".equals(evento)) {
            return;
        }

        // Extraer la transacción del payload
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        Map<String, Object> transaccion = (Map<String, Object>) data.get("transaction");

        String referencia = (String) transaccion.get("reference");
        String statusWompi = (String) transaccion.get("status");
        String idTransaccion = (String) transaccion.get("id");

        // Buscar el pago en la base de datos teniendo en cuenta la referencia
        PagoWompi pago = pagoWompiRepository.findByReferenciaPago(referencia)
                .orElse(null);

        // En caso de no encontrar el pago se ignora directamente el evento
        if (pago == null) {
            return;
        }

        // Mapear el estado de Wompi al de nosotros
        EstadoPago nuevoEstadoPago = mapearEstado(statusWompi);

        // Actualizar el pago
        pago.setTransaccionId(idTransaccion);
        pago.setEstado(nuevoEstadoPago);

        if (nuevoEstadoPago == EstadoPago.APROBADO && pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }

        pagoWompiRepository.save(pago);

        // Actualizar el estado del pedido segun el pago
        Pedido pedido = pago.getPedido();

        if (nuevoEstadoPago == EstadoPago.APROBADO) {
            pedido.setEstado(EstadoPedido.PAGADO);
        } else if (nuevoEstadoPago == EstadoPago.RECHAZADO || nuevoEstadoPago == EstadoPago.CANCELADO) {
            pedido.setEstado(EstadoPedido.CANCELADO);
        }

        pedidoRepository.save(pedido);
    }

    // Funcion para mapear el estado
    private EstadoPago mapearEstado(String statusWompi) {
        if (statusWompi == null) {
            return EstadoPago.PENDIENTE;
        }

        switch (statusWompi) {
            case "APPROVED":
                return EstadoPago.APROBADO;

            case "DECLINED":

            case "ERROR":
                return EstadoPago.RECHAZADO;
                
            case "VOIDED": 
                return EstadoPago.CANCELADO;
                
            default:
                return  EstadoPago.PENDIENTE;
        }
    }
}
