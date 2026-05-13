package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.ContenidoPaginaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContenidoPaginaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContenidoPaginaControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ContenidoPaginaService service;
    @MockitoBean private JwtService jwtService;

    private ContenidoPaginaDTO banner;

    @BeforeEach
    void setUp() {
        banner = ContenidoPaginaDTO.builder()
                .id(1L)
                .tipo("banner")
                .titulo("Promo de mayo")
                .contenido("20% de descuento")
                .imagenUrl("https://cdn/banner.jpg")
                .orden(1)
                .build();
    }

    @Test
    void GET_contenido_devuelveTodo() throws Exception {
        when(service.obtenerTodoElContenido()).thenReturn(List.of(banner));

        mockMvc.perform(get("/api/contenido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tipo").value("banner"))
                .andExpect(jsonPath("$[0].titulo").value("Promo de mayo"));
    }

    @Test
    void GET_contenido_porTipo_devuelveLista() throws Exception {
        when(service.obtenerContenidoPorTipo("banner")).thenReturn(List.of(banner));

        mockMvc.perform(get("/api/contenido/banner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("banner"))
                .andExpect(jsonPath("$[0].orden").value(1));
    }

    @Test
    void GET_contenido_porTipoInexistente_devuelveArrayVacio() throws Exception {
        when(service.obtenerContenidoPorTipo("inexistente")).thenReturn(List.of());

        mockMvc.perform(get("/api/contenido/inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}