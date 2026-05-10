package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.OrquideaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.service.OrquideaAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orquideas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")  // Solo los administradores entran a este controlador
public class OrquideaAdminController {

    private final OrquideaAdminService orquideaAdminService;

    // GET /api/admin/orquideas - listar todas (incluye inactivas)
    @GetMapping
    public ResponseEntity<List<OrquideaDTO>> listarOrquideas() {
        return ResponseEntity.ok(orquideaAdminService.listarOrquideas());
    }

    // POST /api/admin/orquideas - crear una orquídea (multipart por la imagen)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<OrquideaDTO> crearOrquidea(@ModelAttribute OrquideaAdminDTO dto) throws IOException {
        return ResponseEntity.ok(orquideaAdminService.crearOrquidea(dto));
    }

    // PUT /api/admin/orquideas/{id} - actualizar una orquídea
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<OrquideaDTO> actualizarOrquidea(@PathVariable Long id,
                                                          @ModelAttribute OrquideaAdminDTO dto) throws IOException {
        return ResponseEntity.ok(orquideaAdminService.actualizarOrquidea(id, dto));
    }

    // DELETE /api/admin/orquideas/{id} - eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOrquidea(@PathVariable Long id) throws IOException {
        orquideaAdminService.eliminarOrquidea(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/admin/orquideas/{id}/activo - alternar activo/inactivo
    @PatchMapping("/{id}/activo")
    public ResponseEntity<OrquideaDTO> actualizarActivo(@PathVariable Long id) {
        return ResponseEntity.ok(orquideaAdminService.establecerActivo(id));
    }
}