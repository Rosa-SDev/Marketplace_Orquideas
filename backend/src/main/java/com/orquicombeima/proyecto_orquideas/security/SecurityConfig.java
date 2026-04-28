package com.orquicombeima.proyecto_orquideas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * @Configuration: Indica que esta clase es una clase de configuración de Spring.
 * @EnableWebSecurity: Habilita la seguridad web en la aplicación.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler, JwtAuthFilter jwtAuthFilter) throws Exception {
        // Deshabilitar CSRF para APIs REST stateless
        http.csrf(csrf -> csrf.disable())
                // Configurar CORS para que no se bloqueen las peticiones desde el frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configurar IF_REQUIRED (no STATELESS) porque el flujo OAuth2 necesita sesión para guardar el "state" de seguridad mientras Google autentica
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // Configurar autorización de peticiones
                .authorizeHttpRequests(auth -> auth
                        // Rutas publicas
                        .requestMatchers(
                                "/api/macetas/**",
                                "/api/guia/**",
                                "/api/contenido/**",
                                "/api/contacto/**",
                                "/api/orquideas/**",
                                "/api/webhook/**"
                        ).permitAll()
                        // Rutas protegidas para clientes y administradores
                        .requestMatchers(
                                "/api/carrito/**",
                                "/api/pedidos/**",
                                "/api/contacto-whatsapp/**"
                        ).hasAnyRole("CLIENTE", "ADMINISTRADOR")
                        // Rutas protegidas solo para administradores
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                        // Las rutas que no esten en el listado deben ser autenticadas
                        .anyRequest().authenticated()
                )
                // Configuramos el login con Google y le decimos qué hacer al terminar
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                // Nuestro filtro JWT va antes del filtro estándar de usuario/contraseña
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Configuración de CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();


        configuration.setAllowedOrigins(List.of("http://localhost:5173"));  // Ruta del frontend
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));    // Métodos HTTP permitidos
        configuration.setAllowedHeaders(List.of("*"));  // Permitir todos los encabezados
        configuration.setAllowCredentials(true);    // Permitir el envío de cookies y credenciales

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Aplicar la configuración a todas las rutas
        return source;
    }
}
