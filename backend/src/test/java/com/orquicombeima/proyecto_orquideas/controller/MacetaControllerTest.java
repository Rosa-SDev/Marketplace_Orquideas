package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.MacetaDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.MacetaService;
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

@WebMvcTest(controllers = MacetaController.class)
@AutoConfigureMockMvc(addFilters = false)
class MacetaControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private MacetaService service;
    @MockitoBean private JwtService jwtService;

    @Test
    void GET_listarMacetas_devuelve200ConLista() throws Exception {
        MacetaDTO dto = MacetaDTO.builder()
                .id(1L)
                .nombre("Maceta Cerámica")
                .precio(25000.0)
                .stock(15)
                .material("Cerámica")
                .diametroCm(15.0)
                .color("Blanco")
                .estilo("Moderno")
                .activo(true)
                .build();
        when(service.obtenerMacetasActivas()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/macetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Maceta Cerámica"))
                .andExpect(jsonPath("$[0].material").value("Cerámica"));
    }

    @Test
    void GET_listarMacetas_sinResultados_devuelveListaVacia() throws Exception {
        when(service.obtenerMacetasActivas()).thenReturn(List.of());

        mockMvc.perform(get("/api/macetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}