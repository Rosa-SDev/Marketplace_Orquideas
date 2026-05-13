package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.GuiaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.GuiaCuidadoDTO;
import com.orquicombeima.proyecto_orquideas.service.GuiaAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/guia")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")  // Solo los administradores entran a este controlador
public class GuiaAdminController {

    private final GuiaAdminService guiaAdminService;

    // GET /api/admin/guia - listar todas las guías
    @GetMapping
    public ResponseEntity<List<GuiaCuidadoDTO>> listarGuias() {
        return ResponseEntity.ok(guiaAdminService.listarGuias());
    }

    // POST /api/admin/guia - crear una guía (multipart por la imagen)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<GuiaCuidadoDTO> crearGuia(@ModelAttribute GuiaAdminDTO dto) throws IOException {
        return ResponseEntity.ok(guiaAdminService.crearGuia(dto));
    }

    // PUT /api/admin/guia/{id} - actualizar una guía
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<GuiaCuidadoDTO> actualizarGuia(@PathVariable Long id,
                                                         @ModelAttribute GuiaAdminDTO dto) throws IOException {
        return ResponseEntity.ok(guiaAdminService.actualizarGuia(id, dto));
    }

    // DELETE /api/admin/guia/{id} - eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGuia(@PathVariable Long id) throws IOException {
        guiaAdminService.eliminarGuia(id);
        return ResponseEntity.noContent().build();
    }
}