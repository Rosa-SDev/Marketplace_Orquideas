package com.orquicombeima.proyecto_orquideas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.OrquideaAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrquideaAdminController.class)
@AutoConfigureMockMvc(addFilters = false)  // desactivamos seguridad para tests de slice; la seguridad va en el Lote 2
class OrquideaAdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private OrquideaAdminService service;
    @MockitoBean private JwtService jwtService;

    private OrquideaDTO dto;

    @BeforeEach
    void setUp() {
        dto = new OrquideaDTO();
        dto.setId(1L);
        dto.setNombre("Cattleya Trianae");
        dto.setPrecio(85000.0);
        dto.setStock(10);
        dto.setVariedad("Cattleya");
        dto.setActivo(true);
    }

    @Test
    void GET_listarOrquideas_devuelve200ConLista() throws Exception {
        when(service.listarOrquideas()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/orquideas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Cattleya Trianae"));
    }

    @Test
    void POST_crearOrquidea_multipart_devuelve200() throws Exception {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "foto.jpg", "image/jpeg", "img".getBytes());

        when(service.crearOrquidea(any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/admin/orquideas")
                        .file(imagen)
                        .param("nombre", "Cattleya Trianae")
                        .param("precio", "85000")
                        .param("stock", "10")
                        .param("variedad", "Cattleya")
                        .param("tamanio", "Mediana")
                        .param("activo", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cattleya Trianae"));
    }

    @Test
    void PUT_actualizarOrquidea_multipart_devuelve200() throws Exception {
        when(service.actualizarOrquidea(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/admin/orquideas/1")
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .param("nombre", "Cattleya Trianae")
                        .param("precio", "85000")
                        .param("stock", "10")
                        .param("variedad", "Cattleya")
                        .param("tamanio", "Mediana")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void DELETE_eliminarOrquidea_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/admin/orquideas/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminarOrquidea(1L);
    }

    @Test
    void PATCH_actualizarActivo_devuelve200() throws Exception {
        when(service.establecerActivo(anyLong())).thenReturn(dto);

        mockMvc.perform(patch("/api/admin/orquideas/1/activo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Helper para usar eq con primitivos en mockito (evita import explícito en cada test)
    private static long eq(long value) { return org.mockito.ArgumentMatchers.eq(value); }
}