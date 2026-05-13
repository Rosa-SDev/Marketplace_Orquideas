package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.PedidoRecienteDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.PedidoAdminService;
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

@WebMvcTest(controllers = PedidoAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoAdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PedidoAdminService service;
    @MockitoBean private JwtService jwtService;

    @Test
    void GET_pedidosRecientes_devuelve200ConLista() throws Exception {
        PedidoRecienteDTO dto = PedidoRecienteDTO.builder()
                .id(1L)
                .nombreCliente("Rosa")
                .total(100000.0)
                .estado("PAGADO")
                .tiempoTranscurrido("Hace 30 minutos")
                .build();

        when(service.obtenerPedidosRecientes()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/pedidos/recientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombreCliente").value("Rosa"))
                .andExpect(jsonPath("$[0].total").value(100000.0))
                .andExpect(jsonPath("$[0].estado").value("PAGADO"))
                .andExpect(jsonPath("$[0].tiempoTranscurrido").value("Hace 30 minutos"));
    }

    @Test
    void GET_pedidosRecientes_listaVacia_devuelveArrayVacio() throws Exception {
        when(service.obtenerPedidosRecientes()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/pedidos/recientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}