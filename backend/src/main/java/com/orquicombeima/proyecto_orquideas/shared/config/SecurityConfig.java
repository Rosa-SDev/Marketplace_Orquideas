package com.orquicombeima.proyecto_orquideas.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Deshabilitar CSRF para APIs REST stateless
        http.csrf(csrf -> csrf.disable())
            // Configurar sin estado (stateless) para APIs REST
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Configurar autorización de peticiones
            .authorizeHttpRequests(auth -> auth
                // Rutas publicas
                .requestMatchers(
                    "/api/macetas/**",
                    "/api/guia/**",
                    "/api/contenido/**",
                    "/api/contacto/**",
                    "/api/orquideas/**"
                ).permitAll()
                // Rutas protegidas para clientes y administradores
                .requestMatchers(
                        "/api/carrito/**",
                        "/api/pedidos/**"
                ).hasAnyRole("CLIENTE", "ADMINISTRADOR")
                // Rutas protegidas solo para administradores
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                // Las rutas que no esten en el listado deben ser autenticadas
                .anyRequest().authenticated()
            );
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
