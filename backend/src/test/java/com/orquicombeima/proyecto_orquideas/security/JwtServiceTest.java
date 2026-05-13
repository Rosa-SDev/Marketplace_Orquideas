package com.orquicombeima.proyecto_orquideas.security;

import com.orquicombeima.proyecto_orquideas.model.Usuario;
import com.orquicombeima.proyecto_orquideas.model.enums.Rol;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // 32 bytes en Base64 (requisito mínimo de HMAC-SHA256)
    // texto plano: "testtesttesttesttesttesttesttest"
    private static final String SECRET_BASE64 = "dGVzdHRlc3R0ZXN0dGVzdHRlc3R0ZXN0dGVzdHRlc3Q=";
    private static final long EXPIRATION_MS = 3600_000L; // 1 hora

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Inyectamos los @Value manualmente (no levantamos Spring en este test)
        ReflectionTestUtils.setField(jwtService, "secret", SECRET_BASE64);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION_MS);

        usuario = new Usuario();
        usuario.setId(42L);
        usuario.setNombre("Rosa");
        usuario.setEmail("rosa@test.com");
        usuario.setRol(Rol.CLIENTE);
    }

    @Test
    void generarToken_devuelveStringNoVacio() {
        String token = jwtService.generarToken(usuario);

        assertThat(token).isNotBlank();
        // Un JWT siempre tiene 3 partes separadas por puntos
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extraerEmail_devuelveEmailDelUsuario() {
        String token = jwtService.generarToken(usuario);

        String email = jwtService.extraerEmail(token);

        assertThat(email).isEqualTo("rosa@test.com");
    }

    @Test
    void extraerClaims_contieneIdNombreYRol() {
        String token = jwtService.generarToken(usuario);

        Claims claims = jwtService.extraerClaims(token);

        assertThat(claims.getSubject()).isEqualTo("rosa@test.com");
        assertThat(claims.get("id", Integer.class)).isEqualTo(42);
        assertThat(claims.get("nombre", String.class)).isEqualTo("Rosa");
        assertThat(claims.get("rol", String.class)).isEqualTo("CLIENTE");
    }

    @Test
    void extraerClaims_paraAdmin_traeRolADMINISTRADOR() {
        usuario.setRol(Rol.ADMINISTRADOR);
        String token = jwtService.generarToken(usuario);

        Claims claims = jwtService.extraerClaims(token);

        assertThat(claims.get("rol", String.class)).isEqualTo("ADMINISTRADOR");
    }

    @Test
    void esTokenValido_tokenRecienGenerado_devuelveTrue() {
        String token = jwtService.generarToken(usuario);

        assertThat(jwtService.esTokenValido(token)).isTrue();
    }

    @Test
    void esTokenValido_tokenAlterado_devuelveFalse() {
        String token = jwtService.generarToken(usuario);
        // Mutamos un caracter del medio para invalidar la firma
        String tokenAlterado = token.substring(0, token.length() - 5) + "AAAAA";

        assertThat(jwtService.esTokenValido(tokenAlterado)).isFalse();
    }

    @Test
    void esTokenValido_textoBasura_devuelveFalse() {
        assertThat(jwtService.esTokenValido("esto-no-es-un-jwt")).isFalse();
    }

    @Test
    void esTokenValido_tokenExpirado_devuelveFalse() {
        // Reinyectamos expiration negativa para que el token nazca ya expirado
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        String tokenExpirado = jwtService.generarToken(usuario);

        assertThat(jwtService.esTokenValido(tokenExpirado)).isFalse();
    }
}