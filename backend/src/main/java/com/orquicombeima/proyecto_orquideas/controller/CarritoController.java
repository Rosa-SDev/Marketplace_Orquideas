package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.CarritoDTO;
import com.orquicombeima.proyecto_orquideas.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    // Endpoint para obtener un carrito por el email del usuario
    @GetMapping
    public ResponseEntity<CarritoDTO> obtenerCarrito(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(email));
    }

    // Endpoint para actualizar la cantidad de un item
    @PutMapping("/{idItem}/cantidad")
    public ResponseEntity<CarritoDTO> actualizarCantidad(@PathVariable Long idItem, @RequestParam int cantidad, @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(idItem, cantidad, email));
    }

    // Endpoint para eliminar el item de un carrito
    @DeleteMapping("{idItem}")
    public ResponseEntity<CarritoDTO> eliminarItem(@PathVariable Long idItem, @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(carritoService.eliminarItem(idItem, email));
    }

    // Endpoint para vaciar el carrito
    @DeleteMapping("/vaciar")
    public ResponseEntity<Void> vaciarCarrito(@AuthenticationPrincipal String email) {
        carritoService.vaciarCarrito(email);
        return ResponseEntity.noContent().build();
    }
}
