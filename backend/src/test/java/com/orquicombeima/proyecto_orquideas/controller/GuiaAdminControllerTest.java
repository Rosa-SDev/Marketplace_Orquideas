package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.GuiaCuidadoDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.GuiaAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GuiaAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class GuiaAdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GuiaAdminService service;
    @MockitoBean private JwtService jwtService;

    private GuiaCuidadoDTO dto;

    @BeforeEach
    void setUp() {
        dto = GuiaCuidadoDTO.builder()
                .id(10L)
                .titulo("Cuidado de la Cattleya")
                .variedad("Cattleya")
                .idOrquidea(1L)
                .nombreOrquidea("Cattleya Trianae")
                .frecuenciaRiego("Cada 5-7 días")
                .luzRequerida("Indirecta brillante")
                .temperaturaIdeal("18-26 °C")
                .fertilizacion("Mensual")
                .contenido("Texto...")
                .build();
    }

    @Test
    void GET_listarGuias_devuelve200ConLista() throws Exception {
        when(service.listarGuias()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/guia"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].titulo").value("Cuidado de la Cattleya"))
                .andExpect(jsonPath("$[0].nombreOrquidea").value("Cattleya Trianae"));
    }

    @Test
    void POST_crearGuia_multipart_devuelve200() throws Exception {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "guia.jpg", "image/jpeg", "img".getBytes());

        when(service.crearGuia(any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/admin/guia")
                        .file(imagen)
                        .param("idOrquidea", "1")
                        .param("titulo", "Cuidado de la Cattleya")
                        .param("variedad", "Cattleya")
                        .param("contenido", "Texto...")
                        .param("frecuenciaRiego", "Cada 5-7 días")
                        .param("luzRequerida", "Indirecta brillante")
                        .param("temperaturaIdeal", "18-26 °C")
                        .param("fertilizacion", "Mensual")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Cuidado de la Cattleya"));
    }

    @Test
    void PUT_actualizarGuia_multipart_devuelve200() throws Exception {
        when(service.actualizarGuia(eq(10L), any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/admin/guia/10")
                        .file(new MockMultipartFile("imagen", "", "image/jpeg", new byte[0]))
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .param("idOrquidea", "1")
                        .param("titulo", "Cuidado de la Cattleya")
                        .param("variedad", "Cattleya")
                        .param("contenido", "Texto actualizado")
                        .param("frecuenciaRiego", "Cada 5-7 días")
                        .param("luzRequerida", "Indirecta brillante")
                        .param("temperaturaIdeal", "18-26 °C")
                        .param("fertilizacion", "Mensual")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void DELETE_eliminarGuia_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/admin/guia/10"))
                .andExpect(status().isNoContent());

        verify(service).eliminarGuia(10L);
    }
}
