package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.dto.OrquideaDetalleDTO;
import com.orquicombeima.proyecto_orquideas.dto.RecomendacionMacetaDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.OrquideaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrquideaController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrquideaControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private OrquideaService service;
    @MockitoBean private JwtService jwtService;

    private OrquideaDTO dto;

    @BeforeEach
    void setUp() {
        dto = new OrquideaDTO();
        dto.setId(1L);
        dto.setNombre("Cattleya Trianae");
        dto.setPrecio(85000.0);
        dto.setVariedad("Cattleya");
    }

    @Test
    void GET_listarOrquideas_sinParametros_devuelve200ConDefaults() throws Exception {
        when(service.listarTodas(isNull(), isNull(), isNull(), isNull(), eq(0), eq(9)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/orquideas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Cattleya Trianae"));

        verify(service).listarTodas(null, null, null, null, 0, 9);
    }

    @Test
    void GET_listarOrquideas_conFiltros_pasaParametrosAlService() throws Exception {
        when(service.listarTodas(eq("Cattleya"), eq("Rosado"), eq(50000.0), eq(100000.0), eq(1), eq(5)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/orquideas")
                        .param("variedad", "Cattleya")
                        .param("colorFlor", "Rosado")
                        .param("precioMin", "50000")
                        .param("precioMax", "100000")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(service).listarTodas("Cattleya", "Rosado", 50000.0, 100000.0, 1, 5);
    }

    @Test
    void GET_obtenerDetalle_devuelve200ConDetalleCompleto() throws Exception {
        OrquideaDetalleDTO detalle = OrquideaDetalleDTO.builder()
                .id(1L)
                .nombre("Cattleya Trianae")
                .precio(85000.0)
                .variedad("Cattleya")
                .recomendaciones(List.of())
                .build();
        when(service.obtenerDetallePorId(1L)).thenReturn(detalle);

        mockMvc.perform(get("/api/orquideas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Cattleya Trianae"));
    }

    @Test
    void GET_obtenerRecomendaciones_devuelve200ConLista() throws Exception {
        RecomendacionMacetaDTO rec = new RecomendacionMacetaDTO();
        rec.setMacetaId(5L);
        rec.setMacetaNombre("Maceta Cerámica");
        rec.setMaterial("Cerámica");
        rec.setMacetaPrecio(25000.0);
        rec.setDescripcion("Ideal");

        when(service.obtenerRecomendaciones(1L)).thenReturn(List.of(rec));

        mockMvc.perform(get("/api/orquideas/1/recomendaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].macetaId").value(5))
                .andExpect(jsonPath("$[0].macetaNombre").value("Maceta Cerámica"));
    }
}