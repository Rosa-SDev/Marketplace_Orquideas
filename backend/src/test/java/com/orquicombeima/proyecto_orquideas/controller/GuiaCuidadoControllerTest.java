package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.GuiaCuidadoDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.GuiaCuidadoService;
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

@WebMvcTest(controllers = GuiaCuidadoController.class)
@AutoConfigureMockMvc(addFilters = false)
class GuiaCuidadoControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GuiaCuidadoService service;
    @MockitoBean private JwtService jwtService;

    private GuiaCuidadoDTO dto;

    @BeforeEach
    void setUp() {
        dto = GuiaCuidadoDTO.builder()
                .id(10L)
                .titulo("Cuidado de la Cattleya")
                .variedad("Cattleya")
                .contenido("Texto")
                .frecuenciaRiego("Cada 5-7 días")
                .luzRequerida("Indirecta")
                .temperaturaIdeal("18-26 °C")
                .fertilizacion("Mensual")
                .imageUrl("https://cdn/guia.jpg")
                .idOrquidea(1L)
                .nombreOrquidea("Cattleya Trianae")
                .build();
    }

    @Test
    void GET_guia_devuelveTodasLasGuias() throws Exception {
        when(service.obtenerTodasLasGuias()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/guia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].titulo").value("Cuidado de la Cattleya"))
                .andExpect(jsonPath("$[0].nombreOrquidea").value("Cattleya Trianae"));
    }

    @Test
    void GET_guia_listaVacia_devuelveArrayVacio() throws Exception {
        when(service.obtenerTodasLasGuias()).thenReturn(List.of());

        mockMvc.perform(get("/api/guia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void GET_guia_porVariedad_devuelveDTO() throws Exception {
        when(service.obtenerGuiaPorVariedad("Cattleya")).thenReturn(dto);

        mockMvc.perform(get("/api/guia/Cattleya"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variedad").value("Cattleya"))
                .andExpect(jsonPath("$.frecuenciaRiego").value("Cada 5-7 días"));
    }
}