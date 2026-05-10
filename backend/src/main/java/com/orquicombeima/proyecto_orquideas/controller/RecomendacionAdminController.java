package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.RecomendacionAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.RecomendacionDTO;
import com.orquicombeima.proyecto_orquideas.service.RecomendacionAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/recomendaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')") // Solo los usuarios con rol ADMIN pueden acceder a este controlador
public class RecomendacionAdminController {

    private final RecomendacionAdminService recomendacionAdminService;

    @GetMapping
    public ResponseEntity<List<RecomendacionDTO>> listarTodas() {
        return ResponseEntity.ok(recomendacionAdminService.listarTodas());
    }

    @PostMapping
    public ResponseEntity<RecomendacionDTO> crearRecomendacion(@Valid @RequestBody RecomendacionAdminDTO recomendacion) {
        return ResponseEntity.ok(recomendacionAdminService.crearRecomendacion(recomendacion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecomendacionDTO> actualizarRecomendacion(@PathVariable Long id, @Valid @RequestBody RecomendacionAdminDTO recomendacion) {
        return ResponseEntity.ok(recomendacionAdminService.actualizarRecomendacion(id, recomendacion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RecomendacionDTO> eliminarRecomendacion(@PathVariable Long id) {
        recomendacionAdminService.eliminarRecomendacion(id);
        return ResponseEntity.noContent().build();
    }
}
