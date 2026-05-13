package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.MacetaDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.MacetaAdminService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MacetaAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class MacetaAdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private MacetaAdminService service;
    @MockitoBean private JwtService jwtService;

    private MacetaDTO dto;

    @BeforeEach
    void setUp() {
        dto = MacetaDTO.builder()
                .id(1L)
                .nombre("Maceta Cerámica")
                .precio(35000.0)
                .stock(20)
                .material("Cerámica")
                .diametroCm(15)
                .color("Blanco")
                .estilo("Moderno")
                .activo(true)
                .build();
    }

    @Test
    void GET_listarMacetas_devuelve200ConLista() throws Exception {
        when(service.listarMacetas()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/macetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Maceta Cerámica"));
    }

    @Test
    void POST_crearMaceta_multipart_devuelve200() throws Exception {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "foto.jpg", "image/jpeg", "img".getBytes());

        when(service.crearMaceta(any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/admin/macetas")
                        .file(imagen)
                        .param("nombre", "Maceta Cerámica")
                        .param("descripcion", "Para orquídeas medianas")
                        .param("precio", "35000")
                        .param("stock", "20")
                        .param("material", "Cerámica")
                        .param("diametroCm", "15")
                        .param("color", "Blanco")
                        .param("estilo", "Moderno")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Maceta Cerámica"));
    }

    @Test
    void PUT_actualizarMaceta_multipart_devuelve200() throws Exception {
        when(service.actualizarMaceta(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/admin/macetas/1")
                        .file(new MockMultipartFile("imagen", "", "image/jpeg", new byte[0]))
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .param("nombre", "Maceta Cerámica")
                        .param("descripcion", "Actualizada")
                        .param("precio", "35000")
                        .param("stock", "20")
                        .param("material", "Cerámica")
                        .param("diametroCm", "15")
                        .param("color", "Blanco")
                        .param("estilo", "Moderno")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void DELETE_eliminarMaceta_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/admin/macetas/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminarMaceta(1L);
    }

    @Test
    void PATCH_actualizarActivo_devuelve200() throws Exception {
        when(service.establecerActivo(anyLong())).thenReturn(dto);

        mockMvc.perform(patch("/api/admin/macetas/1/activo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}