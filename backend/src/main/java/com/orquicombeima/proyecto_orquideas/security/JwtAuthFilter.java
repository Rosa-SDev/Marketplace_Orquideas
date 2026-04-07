package com.orquicombeima.proyecto_orquideas.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Filtro que se ejecuta una vez por petición y revisa si viene un JWT válido
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Buscamos el header "Authorization: Bearer <token>"
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No tiene token → dejamos pasar, Spring Security decidirá si necesita auth
            filterChain.doFilter(request, response);
            return;
        }

        // Quitamos el prefijo "Bearer " y nos quedamos solo con el token
        String token = authHeader.substring(7);

        if (jwtService.esTokenValido(token)) {
            // Sacamos los datos del token sin consultar la BD (más rápido)
            Claims claims = jwtService.extraerClaims(token);
            String email = claims.getSubject();
            String rol = claims.get("rol", String.class);

            // Le avisamos a Spring Security que este usuario ya está autenticado
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + rol)
            );
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}