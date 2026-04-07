package com.orquicombeima.proyecto_orquideas.security;

import com.orquicombeima.proyecto_orquideas.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

// Sabe crear tokens JWT y también leerlos / validarlos
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // en milisegundos → 86400000 = 24 horas

    // Crea un token con los datos básicos del usuario adentro
    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    // Saca el email que guardamos como "subject" del token
    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    // Saca todos los datos extra que pusimos en el token
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Devuelve true si el token tiene firma válida y no está expirado
    public boolean esTokenValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Convierte el texto secreto en una llave criptográfica real
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}