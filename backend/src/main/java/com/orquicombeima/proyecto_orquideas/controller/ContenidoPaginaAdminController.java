package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaDTO;
import com.orquicombeima.proyecto_orquideas.service.ContenidoPaginaAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
