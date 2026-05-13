package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.model.PagoWompi;
import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.model.Producto;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPago;
import com.orquicombeima.proyecto_orquideas.repository.PagoWompiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoWompiServiceTest {

    @Mock private PagoWompiRepository pagoWompiRepository;
    @Mock private RestTemplate restTemplate;

    @InjectMocks private PagoWompiService service;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        // Los @Value no se inyectan en unit tests; los setteamos manualmente
        ReflectionTestUtils.setField(service, "publicKey", "pub_test_xxx");
        ReflectionTestUtils.setField(service, "privateKey", "prv_test_yyy");
        ReflectionTestUtils.setField(service, "integritySecret", "integ_secret_zzz");
        ReflectionTestUtils.setField(service, "frontendUrl", "https://front.local");

        // RestTemplate está como `private final` inicializado en el sitio (no inyectado por constructor).
        // Lo reemplazamos por el mock con reflection
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);

        Producto producto = new Orquidea();
        producto.setId(1L);
        producto.setNombre("Cattleya");
        producto.setPrecio(50000.0);

        pedido = new Pedido();
        pedido.setId(500L);
        pedido.setTotal(110000.0);
    }

    // -----------------------------------------------------------------
    // generarEnlacePago
    // -----------------------------------------------------------------

    @Test
    void generarEnlacePago_guardaPagoWompiConReferenciaYMonto() {
        service.generarEnlacePago(pedido);

        ArgumentCaptor<PagoWompi> captor = ArgumentCaptor.forClass(PagoWompi.class);
        verify(pagoWompiRepository).save(captor.capture());
        PagoWompi pago = captor.getValue();

        assertThat(pago.getPedido()).isEqualTo(pedido);
        assertThat(pago.getMonto()).isEqualTo(110000.0);
        assertThat(pago.getReferenciaPago()).startsWith("pedido-500-");
        assertThat(pago.getReferenciaPago().length()).isEqualTo("pedido-500-".length() + 8);
    }

    @Test
    void generarEnlacePago_asociaPagoAlPedido() {
        service.generarEnlacePago(pedido);

        assertThat(pedido.getPago()).isNotNull();
        assertThat(pedido.getPago().getReferenciaPago()).startsWith("pedido-500-");
    }

    @Test
    void generarEnlacePago_construyeUrlConPublicKeyYMontoEnCentavos() {
        Map<String, String> resultado = service.generarEnlacePago(pedido);  // ← FIX
        String url = resultado.get("linkPago");

        assertThat(url).startsWith("https://checkout.wompi.co/p/");
        assertThat(url).contains("public-key=pub_test_xxx");
        assertThat(url).contains("currency=COP");
        assertThat(url).contains("amount-in-cents=11000000");  // 110000 * 100
        assertThat(url).contains("reference=pedido-500-");
        assertThat(url).contains("signature:integrity=");
        assertThat(url).contains("redirect-url=https://front.local/pago/resultado");

        // Nuevos asserts: aprovechamos que ahora el Map expone firma y referencia por separado
        assertThat(resultado.get("referencia")).startsWith("pedido-500-");
        assertThat(resultado.get("firmaIntegridad")).isNotBlank();
        assertThat(resultado.get("firmaIntegridad")).hasSize(64);  // SHA-256 hex = 64 chars
    }

    @Test
    void generarEnlacePago_montoConDecimales_redondea() {
        pedido.setTotal(99.99);

        String url = service.generarEnlacePago(pedido).get("linkPago");  // ← FIX

        // Math.round(99.99 * 100) = Math.round(9999.0) = 9999
        assertThat(url).contains("amount-in-cents=9999");
    }

    // -----------------------------------------------------------------
    // verificarTransaccion
    // -----------------------------------------------------------------

    @Test
    void verificarTransaccion_pagoNoExiste_lanzaExcepcion() {
        when(pagoWompiRepository.findByReferenciaPago("ref-fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarTransaccion("ref-fantasma"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró pago");
    }

    @Test
    void verificarTransaccion_dataEsNullEnRespuesta_devuelveEstadoActualDelPago() {
        PagoWompi pago = pagoExistente("ref-100", EstadoPago.PENDIENTE);
        when(pagoWompiRepository.findByReferenciaPago("ref-100")).thenReturn(Optional.of(pago));

        Map<String, Object> bodyVacio = new HashMap<>();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(bodyVacio));

        EstadoPago resultado = service.verificarTransaccion("ref-100");

        assertThat(resultado).isEqualTo(EstadoPago.PENDIENTE);
    }

    @Test
    void verificarTransaccion_dataVacia_devuelveEstadoActualDelPago() {
        PagoWompi pago = pagoExistente("ref-100", EstadoPago.PENDIENTE);
        when(pagoWompiRepository.findByReferenciaPago("ref-100")).thenReturn(Optional.of(pago));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("data", List.of())));

        EstadoPago resultado = service.verificarTransaccion("ref-100");

        assertThat(resultado).isEqualTo(EstadoPago.PENDIENTE);
    }

    @Test
    void verificarTransaccion_APPROVED_mapeaAAprobadoYActualizaPago() {
        PagoWompi pago = pagoExistente("ref-100", EstadoPago.PENDIENTE);
        when(pagoWompiRepository.findByReferenciaPago("ref-100")).thenReturn(Optional.of(pago));
        Map<String, Object> body = Map.of(
                "data", List.of(Map.of("id", "trans-abc-123", "status", "APPROVED"))
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        EstadoPago resultado = service.verificarTransaccion("ref-100");

        assertThat(resultado).isEqualTo(EstadoPago.APROBADO);
        assertThat(pago.getEstado()).isEqualTo(EstadoPago.APROBADO);
        assertThat(pago.getTransaccionId()).isEqualTo("trans-abc-123");
        assertThat(pago.getFechaPago()).isNotNull();
        verify(pagoWompiRepository).save(pago);
    }

    @Test
    void verificarTransaccion_DECLINED_mapeaARechazado() {
        verificarTransaccion_mapeaEstado("DECLINED", EstadoPago.RECHAZADO);
    }

    @Test
    void verificarTransaccion_ERROR_mapeaARechazado() {
        verificarTransaccion_mapeaEstado("ERROR", EstadoPago.RECHAZADO);
    }

    @Test
    void verificarTransaccion_VOIDED_mapeaACancelado() {
        verificarTransaccion_mapeaEstado("VOIDED", EstadoPago.CANCELADO);
    }

    @Test
    void verificarTransaccion_PENDING_mapeaAPendiente() {
        verificarTransaccion_mapeaEstado("PENDING", EstadoPago.PENDIENTE);
    }

    @Test
    void verificarTransaccion_statusDesconocido_mapeaAPendiente() {
        verificarTransaccion_mapeaEstado("EUNKNOWN_STATUS", EstadoPago.PENDIENTE);
    }

    // -----------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------

    private PagoWompi pagoExistente(String referencia, EstadoPago estado) {
        PagoWompi pago = new PagoWompi();
        pago.setId(1L);
        pago.setReferenciaPago(referencia);
        pago.setEstado(estado);
        pago.setMonto(110000.0);
        return pago;
    }

    private void verificarTransaccion_mapeaEstado(String statusWompi, EstadoPago esperado) {
        PagoWompi pago = pagoExistente("ref-100", EstadoPago.PENDIENTE);
        when(pagoWompiRepository.findByReferenciaPago("ref-100")).thenReturn(Optional.of(pago));
        Map<String, Object> body = Map.of(
                "data", List.of(Map.of("id", "trans-xyz", "status", statusWompi))
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        EstadoPago resultado = service.verificarTransaccion("ref-100");

        assertThat(resultado).isEqualTo(esperado);
        assertThat(pago.getEstado()).isEqualTo(esperado);
    }
}