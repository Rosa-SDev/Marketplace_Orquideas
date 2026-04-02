package com.orquicombeima.proyecto_orquideas.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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
                // Las rutas que no esten en el listado deben ser autenticadas
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
