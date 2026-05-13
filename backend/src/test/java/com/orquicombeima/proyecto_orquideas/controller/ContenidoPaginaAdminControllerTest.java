package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaDTO;
import com.orquicombeima.proyecto_orquideas.service.ContenidoPaginaAdminService;
import com.orquicombeima.proyecto_orquideas.shared.config.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

// El POST usa @AuthenticationPrincipal String email, por eso usamos standaloneSetup
// + SecurityContextHolder. Los demás endpoints no usan principal pero comparten el setup.
@ExtendWith(MockitoExtension.class)
class ContenidoPaginaAdminControllerTest {

    private static final String EMAIL_ADMIN = "admin@test.com";

    @Mock private ContenidoPaginaAdminService service;

    @InjectMocks private ContenidoPaginaAdminController controller;

    private MockMvc mockMvc;
    private ContenidoPaginaDTO dto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        EMAIL_ADMIN, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));

        dto = ContenidoPaginaDTO.builder()
                .id(10L)
                .tipo("banner")
                .titulo("Promo")
                .contenido("Texto")
                .orden(1)
                .imagenUrl("https://cloudinary.com/v1/contenido/foto.jpg")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void GET_listarTodo_devuelve200ConLista() throws Exception {
        when(service.listarTodo()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/contenido-pagina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].tipo").value("banner"));
    }

    @Test
    void POST_crearContenido_multipart_devuelve200() throws Exception {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "foto.jpg", "image/jpeg", "img".getBytes());

        when(service.crearContenidoPagina(any(), eq(EMAIL_ADMIN))).thenReturn(dto);

        mockMvc.perform(multipart("/api/admin/contenido-pagina")
                        .file(imagen)
                        .param("tipo", "banner")
                        .param("titulo", "Promo")
                        .param("contenido", "Texto")
                        .param("orden", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("banner"));
    }

    @Test
    void PUT_actualizarContenido_multipart_devuelve200() throws Exception {
        when(service.actualizarContenidoPagina(eq(10L), any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/admin/contenido-pagina/10")
                        .file(new MockMultipartFile("imagen", "", "image/jpeg", new byte[0]))
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .param("tipo", "banner")
                        .param("titulo", "Promo Actualizada")
                        .param("contenido", "Texto nuevo")
                        .param("orden", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void DELETE_eliminarContenido_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/admin/contenido-pagina/10"))
                .andExpect(status().isNoContent());

        verify(service).eliminarContenidoPagina(10L);
    }
}