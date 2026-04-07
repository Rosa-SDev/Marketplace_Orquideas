package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.UsuarioAuthDTO;
import com.orquicombeima.proyecto_orquideas.model.Usuario;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador para todo lo relacionado con autenticación
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // El JwtAuthFilter ya validó el token y puso el email en el Authentication
    @GetMapping("/me")
    public ResponseEntity<UsuarioAuthDTO> obtenerUsuarioActual(Authentication authentication) {
        // Sacamos el email que guardó el filtro
        String email = authentication.getName();

        // Buscamos el usuario completo en la BD
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Armamos el DTO con los campos que pide la HU001
        UsuarioAuthDTO dto = new UsuarioAuthDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol()
        );

        return ResponseEntity.ok(dto);
    }
}