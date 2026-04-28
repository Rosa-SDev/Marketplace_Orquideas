package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.model.PagoWompi;
import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPago;
import com.orquicombeima.proyecto_orquideas.repository.PagoWompiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Servicio que maneja toda la integración con Wompi (modo sandbox)
//  - Crea el registro de pago en la BD y genera el link de checkout para que el usuario pague
//  - Consulta la API de Wompi para saber en qué quedó la transacción y actualiza el estado en la BD
@Service
@RequiredArgsConstructor
public class PagoWompiService {

    private final PagoWompiRepository pagoWompiRepository;

    // Llaves de Wompi sandbox (las inyecta Spring desde application.properties)
    @Value("${wompi.public-key}")
    private String publicKey;

    @Value("${wompi.private-key}")
    private String privateKey;

    // Secreto de integridad que Wompi exige para firmar la URL del checkout
    // No es la llave privada: es otro valor distinto que sale del dashboard de Wompi
    @Value("${wompi.integrity-secret}")
    private String integritySecret;

    // URL del frontend a donde Wompi redirige al usuario después de pagar
    @Value("${app.frontend-url}")
    private String frontendUrl;

    // URLs base de Wompi sandbox; van hardcoded porque son del proveedor (no cambian con nuestro entorno)
    private static final String CHECKOUT_BASE_URL = "https://checkout.wompi.co/p/";
    private static final String API_BASE_URL = "https://sandbox.wompi.co/v1";

    // RestTemplate es la herramienta clásica de Spring para hacer peticiones HTTP a otras APIs
    private final RestTemplate restTemplate = new RestTemplate();

    // Crea un PagoWompi para el pedido y devuelve la URL del Web Checkout de Wompi
    // El frontend abre esa URL en el navegador del usuario para que complete el pago
    @Transactional
    public String generarEnlacePago(Pedido pedido) {
        // Wompi exige una referencia única por transacción
        // Formato pedido-{id}-{uuidCorto} para que sea fácil de rastrear si toca buscar el pago en la BD
        String referencia = "pedido-" + pedido.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);

        // Wompi maneja los montos en centavos (multiplicar el total por 100)
        // Math.round para evitar problemas de precisión cuando double tiene decimales raros
        long montoEnCentavos = Math.round(pedido.getTotal() * 100);

        // Calculamos la firma de integridad que Wompi exige para validar que la URL no fue manipulada
        String firmaIntegridad = calcularFirmaIntegridad(referencia, montoEnCentavos, "COP");

        // Creamos el registro de pago en la BD; el @PrePersist le pone estado PENDIENTE y fechaCreacion
        PagoWompi pago = new PagoWompi();
        pago.setPedido(pedido);
        pago.setReferenciaPago(referencia);
        pago.setMonto(pedido.getTotal());
        pagoWompiRepository.save(pago);

        // Asociamos el pago al pedido en memoria para que el controller pueda devolverlo en el DTO
        pedido.setPago(pago);

        // Construimos la URL del Web Checkout sandbox de Wompi
        // El usuario entra ahí, ve el portal de Wompi, paga, y al final Wompi lo redirige al frontend
        return String.format(
                "%s?public-key=%s&currency=COP&amount-in-cents=%d&reference=%s&signature:integrity=%s&redirect-url=%s/pago/resultado",
                CHECKOUT_BASE_URL,
                publicKey,
                montoEnCentavos,
                referencia,
                firmaIntegridad,
                frontendUrl
        );
    }

    // Calcula la firma SHA-256 que Wompi exige en el link del checkout
    // Formato: SHA256 (referencia + monto + moneda + integritySecret)
    // Esto evita que alguien edite el HTML del frontend para cambiar el monto y pagar menos
    private String calcularFirmaIntegridad(String referencia, long montoEnCentavos, String moneda) {
        try {
            String texto = referencia + montoEnCentavos + moneda + integritySecret;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));

            // Convertimos los bytes a hexadecimal (que es como Wompi espera la firma)
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular firma de integridad de Wompi", e);
        }
    }

    // Consulta la API de Wompi para saber el estado real de una transacción
    //  - Busca el pago en la BD por referencia
    //  - Llama a Wompi sandbox con la llave privada
    //  - Si el estado cambió, lo actualiza en la BD
    //  - Devuelve el estado nuevo para que el caller (controller o webhook) decida qué hacer
    @Transactional
    public EstadoPago verificarTransaccion(String referenciaPago) {
        PagoWompi pago = pagoWompiRepository.findByReferenciaPago(referenciaPago)
                .orElseThrow(() -> new RuntimeException("No se encontró pago con referencia: " + referenciaPago));

        // Wompi pide autenticación Bearer con la llave privada para hacer consultas
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + privateKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // GET /v1/transactions?reference=... devuelve un array con las transacciones de esa referencia
        String url = API_BASE_URL + "/transactions?reference=" + referenciaPago;
        ResponseEntity<Map> respuesta = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        Map<String, Object> body = respuesta.getBody();
        if (body == null || body.get("data") == null) {
            return pago.getEstado();
        }

        // El JSON viene así: { "data": [ { "id": "...", "status": "APPROVED", ... } ], "meta": {...} }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");

        // Si data está vacío todavía no hay transacción (el usuario no terminó el flujo de pago)
        if (data.isEmpty()) {
            return pago.getEstado();
        }

        // Tomamos la primera transacción del array (asumimos una transacción por referencia)
        Map<String, Object> transaccion = data.get(0);
        String statusWompi = (String) transaccion.get("status");
        String idTransaccion = (String) transaccion.get("id");

        // Pasamos del estado que devuelve Wompi al de nuestro enum interno
        EstadoPago nuevoEstado = mapearEstadoWompi(statusWompi);

        // Actualizamos el pago con el ID real de Wompi y el estado nuevo
        pago.setTransaccionId(idTransaccion);
        pago.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoPago.APROBADO && pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }
        pagoWompiRepository.save(pago);

        return nuevoEstado;
    }

    // Wompi maneja: APPROVED, DECLINED, VOIDED, ERROR, PENDING
    // Los traducimos a los estados de nuestro enum (que también incluye CANCELADO)
    private EstadoPago mapearEstadoWompi(String statusWompi) {
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
                return EstadoPago.PENDIENTE;
        }
    }
}