package com.orquicombeima.proyecto_orquideas.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtService jwtService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtService);
        // Aseguramos que el contexto esté limpio antes de cada test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sinHeaderAuthorization_dejaPasarSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).esTokenValido(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void headerSinPrefijoBearer_dejaPasarSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic xyz");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).esTokenValido(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenInvalido_dejaPasarSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-malo");
        when(jwtService.esTokenValido("token-malo")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenValido_seteaAutenticacionEnElContexto() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("rosa@test.com");
        when(claims.get("rol", String.class)).thenReturn("CLIENTE");

        when(request.getHeader("Authorization")).thenReturn("Bearer token-bueno");
        when(jwtService.esTokenValido("token-bueno")).thenReturn(true);
        when(jwtService.extraerClaims("token-bueno")).thenReturn(claims);

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("rosa@test.com");
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CLIENTE");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenValido_rolAdministrador_authoritySePrefijaConROLE() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin@test.com");
        when(claims.get("rol", String.class)).thenReturn("ADMINISTRADOR");

        when(request.getHeader("Authorization")).thenReturn("Bearer admin-token");
        when(jwtService.esTokenValido("admin-token")).thenReturn(true);
        when(jwtService.extraerClaims("admin-token")).thenReturn(claims);

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMINISTRADOR");
    }
}