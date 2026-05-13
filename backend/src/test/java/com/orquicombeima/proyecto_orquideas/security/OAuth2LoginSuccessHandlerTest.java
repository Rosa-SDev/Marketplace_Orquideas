package com.orquicombeima.proyecto_orquideas.security;

import com.orquicombeima.proyecto_orquideas.model.Usuario;
import com.orquicombeima.proyecto_orquideas.model.enums.Rol;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JwtService jwtService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private Authentication authentication;
    @Mock private OAuth2User oAuth2User;
    @Mock private RedirectStrategy redirectStrategy;

    private OAuth2LoginSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2LoginSuccessHandler(usuarioRepository, jwtService);
        // Inyectamos los @Value manualmente
        ReflectionTestUtils.setField(handler, "adminEmail", "admin@test.com");
        ReflectionTestUtils.setField(handler, "frontendUrl", "https://front.local");

        // Reemplazamos la estrategia de redirect para verificar a dónde manda al usuario
        handler.setRedirectStrategy(redirectStrategy);

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
    }

    @Test
    void usuarioNuevoNoEsAdmin_creaConRolCLIENTE() throws Exception {
        when(oAuth2User.getAttribute("email")).thenReturn("rosa@test.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Rosa");
        when(usuarioRepository.findByEmail("rosa@test.com")).thenReturn(Optional.empty());
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("jwt-cliente");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertThat(guardado.getEmail()).isEqualTo("rosa@test.com");
        assertThat(guardado.getNombre()).isEqualTo("Rosa");
        assertThat(guardado.getRol()).isEqualTo(Rol.CLIENTE);
    }

    @Test
    void usuarioNuevoEsAdmin_creaConRolADMINISTRADOR() throws Exception {
        when(oAuth2User.getAttribute("email")).thenReturn("admin@test.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Admin User");
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("jwt-admin");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(Rol.ADMINISTRADOR);
    }

    @Test
    void usuarioExistente_actualizaNombreYNoCambiaRol() throws Exception {
        Usuario existente = new Usuario();
        existente.setId(7L);
        existente.setEmail("rosa@test.com");
        existente.setNombre("Rosa Vieja");
        existente.setRol(Rol.CLIENTE);

        when(oAuth2User.getAttribute("email")).thenReturn("rosa@test.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Rosa Nueva");
        when(usuarioRepository.findByEmail("rosa@test.com")).thenReturn(Optional.of(existente));
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("jwt");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertThat(guardado.getId()).isEqualTo(7L);
        assertThat(guardado.getNombre()).isEqualTo("Rosa Nueva");
        assertThat(guardado.getRol()).isEqualTo(Rol.CLIENTE); // no cambia
    }

    @Test
    void redirigeAlFrontendConElTokenEnLaUrl() throws Exception {
        when(oAuth2User.getAttribute("email")).thenReturn("rosa@test.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Rosa");
        when(usuarioRepository.findByEmail("rosa@test.com")).thenReturn(Optional.empty());
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("jwt-token-123");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), urlCaptor.capture());
        assertThat(urlCaptor.getValue())
                .isEqualTo("https://front.local/auth/callback?token=jwt-token-123");
    }
}