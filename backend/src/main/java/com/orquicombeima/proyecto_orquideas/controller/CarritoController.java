package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.AgregarItemRequestDTO;
import com.orquicombeima.proyecto_orquideas.dto.CarritoDTO;
import com.orquicombeima.proyecto_orquideas.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    // Endpoint para obtener el carrito del usuario autenticado
    @GetMapping
    public ResponseEntity<CarritoDTO> obtenerCarrito(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(email));
    }

    // Endpoint para agregar un producto al carrito
    // El body del request debe traer { "idProducto": 1, "cantidad": 2 }
    @PostMapping("/agregar")
    public ResponseEntity<CarritoDTO> agregarItem(@Valid @RequestBody AgregarItemRequestDTO request,
                                                  @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(carritoService.agregarItem(request, email));
    }

    // Endpoint para actualizar la cantidad de un item del carrito
    @PutMapping("/{idItem}/cantidad")
    public ResponseEntity<CarritoDTO> actualizarCantidad(@PathVariable Long idItem,
                                                         @RequestParam int cantidad,
                                                         @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(idItem, cantidad, email));
    }

    // Endpoint para eliminar un item del carrito
    @DeleteMapping("{idItem}")
    public ResponseEntity<CarritoDTO> eliminarItem(@PathVariable Long idItem,
                                                   @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(carritoService.eliminarItem(idItem, email));
    }

    // Endpoint para vaciar el carrito completo
    @DeleteMapping("/vaciar")
    public ResponseEntity<Void> vaciarCarrito(@AuthenticationPrincipal String email) {
        carritoService.vaciarCarrito(email);
        return ResponseEntity.noContent().build();
    }
}