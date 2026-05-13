package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.ContactoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ContactoServiceTest {

    private ContactoService service;

    @BeforeEach
    void setUp() {
        service = new ContactoService();
        // Inyectamos los @Value manualmente (no levantamos Spring)
        ReflectionTestUtils.setField(service, "numero", "573001234567");
        ReflectionTestUtils.setField(service, "mensaje", "Hola, quiero más información sobre orquídeas");
    }

    @Test
    void obtenerContacto_devuelveDTOConNumeroYMensaje() {
        ContactoDTO dto = service.obtenerContacto();

        assertThat(dto.getNumero()).isEqualTo("573001234567");
        assertThat(dto.getMensaje()).isEqualTo("Hola, quiero más información sobre orquídeas");
    }

    @Test
    void obtenerContacto_construyeUrlConBaseDeWhatsAppYNumero() {
        ContactoDTO dto = service.obtenerContacto();

        assertThat(dto.getUrlWhatsapp()).startsWith("https://wa.me/573001234567?text=");
    }

    @Test
    void obtenerContacto_codificaElMensajeParaUrl() {
        ContactoDTO dto = service.obtenerContacto();

        // Espacios → "+", tildes y acentos → %XX
        assertThat(dto.getUrlWhatsapp()).contains("Hola%2C+quiero+m%C3%A1s+informaci%C3%B3n");
    }

    @Test
    void obtenerContacto_mensajeSimple_codificacionEsperada() {
        ReflectionTestUtils.setField(service, "mensaje", "Hola mundo");

        ContactoDTO dto = service.obtenerContacto();

        assertThat(dto.getUrlWhatsapp()).endsWith("?text=Hola+mundo");
    }
}