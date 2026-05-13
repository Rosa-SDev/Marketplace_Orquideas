package com.orquicombeima.proyecto_orquideas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.WompiWebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// El webhook es público (no requiere auth), así que slice simple
// @MockitoBean private JwtService jwtService; es necesario porque el contexto
// del slice intenta cargar JwtAuthFilter que depende de JwtService.
@WebMvcTest(controllers = WompiWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class WompiWebhookControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private WompiWebhookService service;
    @MockitoBean private JwtService jwtService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void POST_webhookWompi_devuelve200YDelegaAlServicio() throws Exception {
        Map<String, Object> payload = Map.of(
                "event", "transaction.updated",
                "data", Map.of("transaction", Map.of(
                        "reference", "pedido-500-abc",
                        "status", "APPROVED",
                        "id", "trans-xyz-1"
                ))
        );

        mockMvc.perform(post("/api/webhook/wompi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(service).procesarEvento(any());
    }

    @Test
    void POST_webhookWompi_payloadCualquiera_siempreDevuelve200() throws Exception {
        // Wompi puede mandar cualquier evento; el service decide qué hacer pero
        // el endpoint siempre debe responder 200 para que Wompi no reintente
        Map<String, Object> payload = Map.of("event", "transaction.created");

        mockMvc.perform(post("/api/webhook/wompi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(service).procesarEvento(any());
    }
}