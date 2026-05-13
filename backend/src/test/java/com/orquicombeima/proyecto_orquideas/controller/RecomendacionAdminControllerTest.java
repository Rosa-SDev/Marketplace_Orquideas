package com.orquicombeima.proyecto_orquideas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orquicombeima.proyecto_orquideas.dto.RecomendacionAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.RecomendacionDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.RecomendacionAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RecomendacionAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecomendacionAdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private RecomendacionAdminService service;
    @MockitoBean private JwtService jwtService;

    private final ObjectMapper mapper = new ObjectMapper();
    private RecomendacionDTO dto;
    private RecomendacionAdminDTO request;

    @BeforeEach
    void setUp() {
        dto = RecomendacionDTO.builder()
                .id(100L)
                .idOrquidea(1L)
                .nombreOrquidea("Cattleya")
                .idMaceta(10L)
                .nombreMaceta("Maceta Cerámica")
                .descripcion("Buena por su tamaño")
                .build();

        request = RecomendacionAdminDTO.builder()
                .idOrquidea(1L)
                .idMaceta(10L)
                .descripcion("Buena por su tamaño")
                .build();
    }

    @Test
    void GET_listarTodas_devuelve200ConLista() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/recomendaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].nombreOrquidea").value("Cattleya"))
                .andExpect(jsonPath("$[0].nombreMaceta").value("Maceta Cerámica"));
    }

    @Test
    void POST_crearRecomendacion_devuelve200() throws Exception {
        when(service.crearRecomendacion(any())).thenReturn(dto);

        mockMvc.perform(post("/api/admin/recomendaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.descripcion").value("Buena por su tamaño"));
    }

    @Test
    void PUT_actualizarRecomendacion_devuelve200() throws Exception {
        when(service.actualizarRecomendacion(eq(100L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/admin/recomendaciones/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void DELETE_eliminarRecomendacion_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/admin/recomendaciones/100"))
                .andExpect(status().isNoContent());

        verify(service).eliminarRecomendacion(100L);
    }
}