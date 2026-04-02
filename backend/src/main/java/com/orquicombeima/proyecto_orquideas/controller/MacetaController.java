package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.MacetaDTO;
import com.orquicombeima.proyecto_orquideas.service.MacetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/macetas")
@RequiredArgsConstructor
public class MacetaController {

    private final MacetaService macetaService;

    @GetMapping
    public ResponseEntity<List<MacetaDTO>> obtenerMacetasActivas() {
        return ResponseEntity.ok(macetaService.obtenerMacetasActivas());
    }

}
