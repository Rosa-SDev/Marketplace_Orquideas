package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.MacetaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.MacetaDTO;
import com.orquicombeima.proyecto_orquideas.service.MacetaAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/macetas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")  // Solo los usuarios con rol ADMIN pueden acceder a este controlador
public class MacetaAdminController {

    private final MacetaAdminService macetaAdminService;

    // Endpoint para realizar listar todas las macetas
    @GetMapping
    public ResponseEntity<List<MacetaDTO>> listarMacetas() {
        return ResponseEntity.ok(macetaAdminService.listarMacetas());
    }

    // Endpoint para crear una maceta
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<MacetaDTO> crearMaceta(@ModelAttribute MacetaAdminDTO dto) throws IOException {
        return ResponseEntity.ok(macetaAdminService.crearMaceta(dto));
    }

    // Endpoint para actualizar una maceta
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<MacetaDTO> actualizarMaceta(@PathVariable Long id, @ModelAttribute MacetaAdminDTO dto) throws IOException {
        return ResponseEntity.ok(macetaAdminService.actualizarMaceta(id, dto));
    }

    // Endpoint para eliminar una maceta
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMaceta(@PathVariable Long id) throws IOException {
        macetaAdminService.eliminarMaceta(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para establecer el estado de la maceta
    @PatchMapping("/{id}/activo")
    public ResponseEntity<MacetaDTO> actualizarActivo(@PathVariable Long id) {
        return ResponseEntity.ok(macetaAdminService.establecerActivo(id));
    }

}
