package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaDTO;
import com.orquicombeima.proyecto_orquideas.service.ContenidoPaginaAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/contenido-pagina")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')") // Solo los usuarios con rol ADMIN pueden acceder a estos endpoints
public class ContenidoPaginaAdminController {

    private final ContenidoPaginaAdminService contenidoPaginaAdminService;

    @GetMapping
    public ResponseEntity<List<ContenidoPaginaDTO>> listarTodo() {
        return  ResponseEntity.ok(contenidoPaginaAdminService.listarTodo());
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ContenidoPaginaDTO> crearContenidoPagina(@ModelAttribute ContenidoPaginaAdminDTO dto, @AuthenticationPrincipal String email) throws IOException {
        return ResponseEntity.ok(contenidoPaginaAdminService.crearContenidoPagina(dto, email));
    }

    @PutMapping(path = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ContenidoPaginaDTO> actualizarContenidoPagina(@PathVariable long id, @ModelAttribute ContenidoPaginaAdminDTO dto) throws IOException {
        return ResponseEntity.ok(contenidoPaginaAdminService.actualizarContenidoPagina(id, dto));
    }
}
