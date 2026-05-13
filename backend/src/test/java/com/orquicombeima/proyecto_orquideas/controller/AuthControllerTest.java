package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.model.Usuario;
import com.orquicombeima.proyecto_orquideas.model.enums.Rol;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
import com.orquicombeima.proyecto_orquideas.shared.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Para AuthController usamos standaloneSetup en vez de @WebMvcTest porque:
// 1. El controller recibe Authentication como parámetro, y la combinación @WebMvcTest + addFilters=false
//    no resuelve bien la inyección del Authentication en la request
// 2. standaloneSetup monta SOLO este controller en un dispatcher mínimo
// 3. Registramos manualmente el GlobalExceptionHandler para que las RuntimeException se conviertan en 404
//    (que es lo que hace la app real)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private AuthController controller;

    private MockMvc mockMvc;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // .setControllerAdvice() hace que el dispatcher de standaloneSetup atrape las RuntimeException
        // y las convierta en HTTP 404 igual que el handler global de la app
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        usuario = new Usuario();
        usuario.setId(42L);
        usuario.setNombre("Rosa");
        usuario.setEmail("rosa@test.com");
        usuario.setRol(Rol.CLIENTE);
    }

    // Helper: arma un Authentication de prueba con email + rol, listo para usar como Principal en la request
    private UsernamePasswordAuthenticationToken authFor(String email, String rol) {
        return new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
    }

    @Test
    void GET_me_devuelveDatosDelUsuarioAutenticado() throws Exception {
        when(usuarioRepository.findByEmail("rosa@test.com")).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/auth/me").principal(authFor("rosa@test.com", "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(42))
                .andExpect(jsonPath("$.nombre").value("Rosa"))
                .andExpect(jsonPath("$.correo").value("rosa@test.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void GET_me_paraAdmin_devuelveRolADMINISTRADOR() throws Exception {
        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setNombre("Admin");
        admin.setEmail("admin@test.com");
        admin.setRol(Rol.ADMINISTRADOR);

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        mockMvc.perform(get("/api/auth/me").principal(authFor("admin@test.com", "ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));
    }

    @Test
    void GET_me_emailNoEnBD_devuelve404() throws Exception {
        // El controller lanza RuntimeException cuando el email del token no aparece en la BD.
        // El GlobalExceptionHandler la atrapa y devuelve 404 con un body { "error": "Usuario no encontrado" }
        when(usuarioRepository.findByEmail("fantasma@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me").principal(authFor("fantasma@test.com", "CLIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }
}