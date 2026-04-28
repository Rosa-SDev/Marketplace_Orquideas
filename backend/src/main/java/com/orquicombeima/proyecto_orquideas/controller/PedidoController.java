package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.CrearPedidoDTO;
import com.orquicombeima.proyecto_orquideas.dto.PedidoDTO;
import com.orquicombeima.proyecto_orquideas.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controlador de los endpoints relacionados con pedidos del usuario autenticado
//  - POST /api/pedidos          → crea un pedido a partir del carrito y devuelve el link de Wompi
//  - GET  /api/pedidos/historial → lista los pedidos pasados del usuario
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    // Crea un pedido nuevo a partir del carrito del usuario autenticado
    // Body: { "direccionEnvio": { "nombreDestinatario": "...", "telefonoDestinatario": "...", ... } }
    // Respuesta: PedidoDTO con linkPago (URL de Wompi) y referenciaPago (para consultar después)
    // @Valid dispara las validaciones del DTO (incluidas las de DireccionEnvioDTO por el @Valid anidado)
    @PostMapping
    public ResponseEntity<PedidoDTO> crearPedido(@Valid @RequestBody CrearPedidoDTO request,
                                                 @AuthenticationPrincipal String email) {
        PedidoDTO pedido = pedidoService.crearPedido(email, request);
        return ResponseEntity.ok(pedido);
    }

    // Devuelve los pedidos del usuario autenticado, los más recientes primero
    // Sirve para la pantalla "Mi cuenta" del frontend (HU015)
    @GetMapping("/historial")
    public ResponseEntity<List<PedidoDTO>> obtenerHistorial(@AuthenticationPrincipal String email) {
        List<PedidoDTO> historial = pedidoService.obtenerHistorial(email);
        return ResponseEntity.ok(historial);
    }
}