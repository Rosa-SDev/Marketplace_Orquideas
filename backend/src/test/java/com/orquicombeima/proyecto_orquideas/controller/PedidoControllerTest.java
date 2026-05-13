package com.orquicombeima.proyecto_orquideas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orquicombeima.proyecto_orquideas.dto.CrearPedidoDTO;
import com.orquicombeima.proyecto_orquideas.dto.DireccionEnvioDTO;
import com.orquicombeima.proyecto_orquideas.dto.PedidoDTO;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPago;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPedido;
import com.orquicombeima.proyecto_orquideas.service.PedidoService;
import com.orquicombeima.proyecto_orquideas.shared.config.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Mismo patrón que CarritoControllerTest: standaloneSetup + SecurityContextHolder porque
// PedidoController usa @AuthenticationPrincipal String email.
@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    private static final String EMAIL = "rosa@test.com";

    @Mock private PedidoService service;

    @InjectMocks private PedidoController controller;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private PedidoDTO pedidoDTO;
    private CrearPedidoDTO request;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        EMAIL, null,
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));

        DireccionEnvioDTO direccion = DireccionEnvioDTO.builder()
                .nombreDestinatario("Rosa")
                .telefonoDestinatario("3001234567")
                .departamento("Tolima")
                .ciudad("Ibagué")
                .direccion("Calle 10 #5-20")
                .build();
        request = new CrearPedidoDTO(direccion);

        pedidoDTO = PedidoDTO.builder()
                .id(500L)
                .estado(EstadoPedido.PENDIENTE)
                .estadoPago(EstadoPago.PENDIENTE)
                .items(List.of())
                .direccionEnvio(direccion)
                .subtotal(100000.0)
                .costoEnvio(10000.0)
                .total(110000.0)
                .linkPago("https://checkout.wompi.co/p/?ref=abc")
                .referenciaPago("pedido-500-abc")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void POST_crearPedido_devuelve200ConLinkDePago() throws Exception {
        when(service.crearPedido(eq(EMAIL), any(CrearPedidoDTO.class))).thenReturn(pedidoDTO);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.estadoPago").value("PENDIENTE"))
                .andExpect(jsonPath("$.linkPago").value("https://checkout.wompi.co/p/?ref=abc"))
                .andExpect(jsonPath("$.referenciaPago").value("pedido-500-abc"))
                .andExpect(jsonPath("$.total").value(110000.0));
    }

    @Test
    void POST_crearPedido_carritoVacio_devuelve404() throws Exception {
        // Cuando el service lanza RuntimeException, el GlobalExceptionHandler lo convierte en 404
        when(service.crearPedido(eq(EMAIL), any(CrearPedidoDTO.class)))
                .thenThrow(new RuntimeException("No se puede crear un pedido con el carrito vacío"));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No se puede crear un pedido con el carrito vacío"));
    }

    @Test
    void GET_historial_devuelveListaDePedidos() throws Exception {
        when(service.obtenerHistorial(EMAIL)).thenReturn(List.of(pedidoDTO));

        mockMvc.perform(get("/api/pedidos/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(500))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].total").value(110000.0));
    }

    @Test
    void GET_historial_sinPedidos_devuelveArrayVacio() throws Exception {
        when(service.obtenerHistorial(EMAIL)).thenReturn(List.of());

        mockMvc.perform(get("/api/pedidos/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}