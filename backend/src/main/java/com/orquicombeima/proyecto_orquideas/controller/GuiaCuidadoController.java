package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.GuiaCuidadoDTO;
import com.orquicombeima.proyecto_orquideas.service.GuiaCuidadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/guia")
@RequiredArgsConstructor
public class GuiaCuidadoController {

    private final GuiaCuidadoService guiaCuidadoService;

    // Endpoint para listar las guías de cuidado
    @GetMapping
    public ResponseEntity<List<GuiaCuidadoDTO>> obtenerTodasLasGuias() {
        return ResponseEntity.ok(guiaCuidadoService.obtenerTodasLasGuias());
    }

    @GetMapping("/{variedad}")
    public ResponseEntity<GuiaCuidadoDTO> obtenerGuiaPorVariedad(@PathVariable String variedad) {
        return ResponseEntity.ok(guiaCuidadoService.obtenerGuiaPorVariedad(variedad));
    }
}
