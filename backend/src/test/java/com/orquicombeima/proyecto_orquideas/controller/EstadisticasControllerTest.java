package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.EstadisticasDTO;
import com.orquicombeima.proyecto_orquideas.dto.VentasMesDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.EstadisticasService;
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

@WebMvcTest(controllers = EstadisticasController.class)
@AutoConfigureMockMvc(addFilters = false)
class EstadisticasControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private EstadisticasService service;
    @MockitoBean private JwtService jwtService;

    private EstadisticasDTO dto;

    @BeforeEach
    void setUp() {
        dto = EstadisticasDTO.builder()
                .ventasMes(150000.0)
                .pedidosMes(8)
                .pedidosPendientes(2)
                .clientesRegistrados(42)
                .ventasPorMes(List.of(
                        VentasMesDTO.builder().mes("abril").totalVentas(120000.0).build(),
                        VentasMesDTO.builder().mes("mayo").totalVentas(150000.0).build()))
                .build();
    }

    @Test
    void GET_estadisticas_devuelve200ConTodosLosCampos() throws Exception {
        when(service.obtenerEstadisticas()).thenReturn(dto);

        mockMvc.perform(get("/api/admin/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasMes").value(150000.0))
                .andExpect(jsonPath("$.pedidosMes").value(8))
                .andExpect(jsonPath("$.pedidosPendientes").value(2))
                .andExpect(jsonPath("$.clientesRegistrados").value(42))
                .andExpect(jsonPath("$.ventasPorMes[0].mes").value("abril"))
                .andExpect(jsonPath("$.ventasPorMes[0].totalVentas").value(120000.0))
                .andExpect(jsonPath("$.ventasPorMes[1].mes").value("mayo"));
    }
}