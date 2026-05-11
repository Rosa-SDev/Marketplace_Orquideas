package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.UsuarioAuthDTO;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/clientes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioAdminController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<UsuarioAuthDTO>> listarClientes() {
        List<UsuarioAuthDTO> clientes = usuarioRepository
                .findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(usuario -> new UsuarioAuthDTO(
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getEmail(),
                        usuario.getRol()
                ))
                .toList();

        return ResponseEntity.ok(clientes);
    }
}
