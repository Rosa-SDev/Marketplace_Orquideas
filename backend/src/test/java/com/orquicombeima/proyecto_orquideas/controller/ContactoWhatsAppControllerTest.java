package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.service.ContactoWhatsAppService;
import com.orquicombeima.proyecto_orquideas.shared.config.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Mismo patrón que CarritoControllerTest: standaloneSetup + SecurityContextHolder porque
// ContactoWhatsAppController usa @AuthenticationPrincipal String email
@ExtendWith(MockitoExtension.class)
class ContactoWhatsAppControllerTest {

    private static final String EMAIL = "rosa@test.com";

    @Mock private ContactoWhatsAppService service;

    @InjectMocks private ContactoWhatsAppController controller;

    private MockMvc mockMvc;

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
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void GET_pedido_devuelve200ConUrlDeWhatsApp() throws Exception {
        when(service.generarEnlaceContacto(500L))
                .thenReturn("https://wa.me/573001234567?text=Hola%20pedido%20%23500");

        mockMvc.perform(get("/api/contacto-whatsapp/pedido/500"))
                .andExpect(status().isOk())
                .andExpect(content().string("https://wa.me/573001234567?text=Hola%20pedido%20%23500"));
    }

    @Test
    void GET_pedido_pedidoNoExiste_devuelve404() throws Exception {
        // El service lanza RuntimeException → GlobalExceptionHandler → 404
        when(service.generarEnlaceContacto(99L))
                .thenThrow(new RuntimeException("No se encontró el pedido: 99"));

        mockMvc.perform(get("/api/contacto-whatsapp/pedido/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No se encontró el pedido: 99"));
    }
}