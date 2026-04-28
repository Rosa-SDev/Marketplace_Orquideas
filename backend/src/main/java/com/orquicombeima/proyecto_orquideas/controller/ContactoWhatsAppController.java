package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.service.ContactoWhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacto-whatsapp")
@RequiredArgsConstructor
public class ContactoWhatsAppController {

    private final ContactoWhatsAppService contactoWhatsAppService;

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<String> generarEnlacePedido(@PathVariable Long idPedido, @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(contactoWhatsAppService.generarEnlaceContacto(idPedido));
    }
}
