package com.orquicombeima.proyecto_orquideas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orquicombeima.proyecto_orquideas.dto.AgregarItemRequestDTO;
import com.orquicombeima.proyecto_orquideas.dto.CarritoDTO;
import com.orquicombeima.proyecto_orquideas.dto.ItemCarritoDTO;
import com.orquicombeima.proyecto_orquideas.service.CarritoService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// CarritoController usa @AuthenticationPrincipal String email.
// El AuthenticationPrincipalArgumentResolver lee el principal del SecurityContextHolder,
// NO del request.principal — por eso configuramos manualmente el SecurityContext antes
// de cada test. Esta es la forma estándar de probar @AuthenticationPrincipal en standaloneSetup.
@ExtendWith(MockitoExtension.class)
class CarritoControllerTest {

    private static final String EMAIL_USUARIO = "rosa@test.com";

    @Mock private CarritoService service;

    @InjectMocks private CarritoController controller;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private CarritoDTO carritoDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Configuramos el SecurityContextHolder para que el resolver encuentre el usuario
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        EMAIL_USUARIO, null,
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));

        ItemCarritoDTO item = ItemCarritoDTO.builder()
                .id(50L)
                .idProducto(10L)
                .nombreProducto("Cattleya")
                .precioUnitario(50000.0)
                .cantidad(2)
                .subtotal(100000.0)
                .imagenUrl("https://cdn/cattleya.jpg")
                .build();

        carritoDTO = CarritoDTO.builder()
                .id(100L)
                .idUsuario(1L)
                .items(List.of(item))
                .total(100000.0)
                .build();
    }

    @AfterEach
    void tearDown() {
        // Importante: limpiar el contexto entre tests para no contaminar otros
        SecurityContextHolder.clearContext();
    }

    @Test
    void GET_carrito_devuelve200ConCarrito() throws Exception {
        when(service.obtenerCarrito(EMAIL_USUARIO)).thenReturn(carritoDTO);

        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.total").value(100000.0))
                .andExpect(jsonPath("$.items[0].nombreProducto").value("Cattleya"));
    }

    @Test
    void POST_agregar_devuelve200ConCarritoActualizado() throws Exception {
        AgregarItemRequestDTO request = new AgregarItemRequestDTO(10L, 2);
        when(service.agregarItem(any(AgregarItemRequestDTO.class), eq(EMAIL_USUARIO)))
                .thenReturn(carritoDTO);

        mockMvc.perform(post("/api/carrito/agregar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cantidad").value(2));
    }

    @Test
    void PUT_actualizarCantidad_devuelve200() throws Exception {
        when(service.actualizarCantidad(eq(50L), eq(5), eq(EMAIL_USUARIO))).thenReturn(carritoDTO);

        mockMvc.perform(put("/api/carrito/50/cantidad")
                        .param("cantidad", "5"))
                .andExpect(status().isOk());

        verify(service).actualizarCantidad(50L, 5, EMAIL_USUARIO);
    }

    @Test
    void DELETE_eliminarItem_devuelve200() throws Exception {
        when(service.eliminarItem(eq(50L), eq(EMAIL_USUARIO))).thenReturn(carritoDTO);

        mockMvc.perform(delete("/api/carrito/50"))
                .andExpect(status().isOk());

        verify(service).eliminarItem(50L, EMAIL_USUARIO);
    }

    @Test
    void DELETE_vaciar_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/carrito/vaciar"))
                .andExpect(status().isNoContent());

        verify(service).vaciarCarrito(EMAIL_USUARIO);
    }
}