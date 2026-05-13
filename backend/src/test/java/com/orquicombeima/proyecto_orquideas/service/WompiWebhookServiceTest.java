package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.model.PagoWompi;
import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPago;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPedido;
import com.orquicombeima.proyecto_orquideas.repository.PagoWompiRepository;
import com.orquicombeima.proyecto_orquideas.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WompiWebhookServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private PagoWompiRepository pagoWompiRepository;

    @InjectMocks private WompiWebhookService service;

    private Pedido pedido;
    private PagoWompi pago;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setId(500L);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        pago = new PagoWompi();
        pago.setId(1L);
        pago.setReferenciaPago("ref-500");
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMonto(110000.0);
        pago.setPedido(pedido);
    }

    // Helper: construye un payload Wompi típico
    private Map<String, Object> payloadCon(String evento, String referencia, String status, String transId) {
        Map<String, Object> transaccion = Map.of(
                "reference", referencia,
                "status", status,
                "id", transId);
        Map<String, Object> data = Map.of("transaction", transaccion);
        return Map.of("event", evento, "data", data);
    }

    // -----------------------------------------------------------------
    // casos donde NO debe hacer nada
    // -----------------------------------------------------------------

    @Test
    void procesarEvento_eventoNoEsTransactionUpdated_noHaceNada() {
        Map<String, Object> payload = payloadCon("transaction.created", "ref-500", "APPROVED", "trans-1");

        service.procesarEvento(payload);

        verify(pagoWompiRepository, never()).findByReferenciaPago(any());
        verify(pagoWompiRepository, never()).save(any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void procesarEvento_pagoNoExiste_ignoraSilenciosamente() {
        Map<String, Object> payload = payloadCon("transaction.updated", "ref-fantasma", "APPROVED", "trans-1");
        when(pagoWompiRepository.findByReferenciaPago("ref-fantasma")).thenReturn(Optional.empty());

        service.procesarEvento(payload);

        verify(pagoWompiRepository, never()).save(any());
        verify(pedidoRepository, never()).save(any());
    }

    // -----------------------------------------------------------------
    // APPROVED → pago APROBADO + pedido PAGADO
    // -----------------------------------------------------------------

    @Test
    void procesarEvento_APPROVED_actualizaPagoAAprobadoYPedidoAPagado() {
        Map<String, Object> payload = payloadCon("transaction.updated", "ref-500", "APPROVED", "trans-abc");
        when(pagoWompiRepository.findByReferenciaPago("ref-500")).thenReturn(Optional.of(pago));

        service.procesarEvento(payload);

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.APROBADO);
        assertThat(pago.getTransaccionId()).isEqualTo("trans-abc");
        assertThat(pago.getFechaPago()).isNotNull();
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PAGADO);
        verify(pagoWompiRepository).save(pago);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void procesarEvento_APPROVED_yaTeniaFechaPago_noLaSobrescribe() {
        java.time.LocalDateTime fechaOriginal = java.time.LocalDateTime.now().minusDays(1);
        pago.setEstado(EstadoPago.APROBADO);
        pago.setFechaPago(fechaOriginal);

        Map<String, Object> payload = payloadCon("transaction.updated", "ref-500", "APPROVED", "trans-abc");
        when(pagoWompiRepository.findByReferenciaPago("ref-500")).thenReturn(Optional.of(pago));

        service.procesarEvento(payload);

        assertThat(pago.getFechaPago()).isEqualTo(fechaOriginal);
    }

    // -----------------------------------------------------------------
    // RECHAZADO / CANCELADO → pedido CANCELADO
    // -----------------------------------------------------------------

    @Test
    void procesarEvento_DECLINED_pedidoCancelado() {
        Map<String, Object> payload = payloadCon("transaction.updated", "ref-500", "DECLINED", "trans-x");
        when(pagoWompiRepository.findByReferenciaPago("ref-500")).thenReturn(Optional.of(pago));

        service.procesarEvento(payload);

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
    }

    @Test
    void procesarEvento_ERROR_pedidoCancelado() {
        Map<String, Object> payload = payloadCon("transaction.updated", "ref-500", "ERROR", "trans-x");
        when(pagoWompiRepository.findByReferenciaPago("ref-500")).thenReturn(Optional.of(pago));

        service.procesarEvento(payload);

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
    }

    @Test
    void procesarEvento_VOIDED_pedidoCancelado() {
        Map<String, Object> payload = payloadCon("transaction.updated", "ref-500", "VOIDED", "trans-x");
        when(pagoWompiRepository.findByReferenciaPago("ref-500")).thenReturn(Optional.of(pago));

        service.procesarEvento(payload);

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.CANCELADO);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
    }

    // -----------------------------------------------------------------
    // PENDIENTE → no cambia el estado del pedido
    // -----------------------------------------------------------------

    @Test
    void procesarEvento_PENDING_noCambiaEstadoDelPedido() {
        Map<String, Object> payload = payloadCon("transaction.updated", "ref-500", "PENDING", "trans-x");
        when(pagoWompiRepository.findByReferenciaPago("ref-500")).thenReturn(Optional.of(pago));

        service.procesarEvento(payload);

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.PENDIENTE);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);  // sigue igual
    }
}