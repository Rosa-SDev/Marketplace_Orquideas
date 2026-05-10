package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.PedidoRecienteDTO;
import com.orquicombeima.proyecto_orquideas.service.PedidoAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pedidos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class PedidoAdminController {

    private final PedidoAdminService pedidoAdminService;

    @GetMapping("/recientes")
    public ResponseEntity<List<PedidoRecienteDTO>> obtenerPedidosRecientes() {
        return  ResponseEntity.ok(pedidoAdminService.obtenerPedidosRecientes());
    }
}
