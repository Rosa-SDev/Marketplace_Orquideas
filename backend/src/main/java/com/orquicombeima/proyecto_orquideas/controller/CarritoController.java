package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.CarritoDTO;
import com.orquicombeima.proyecto_orquideas.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public ResponseEntity<CarritoDTO> obtenerCarrito(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(email));
    }
}
