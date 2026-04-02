package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.ContactoDTO;
import com.orquicombeima.proyecto_orquideas.service.ContactoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacto")
@RequiredArgsConstructor
public class ContactoController {

    private final ContactoService contactoService;

    // Obtener la información de contacto
    @GetMapping
    public ResponseEntity<ContactoDTO> obtenerContacto() {
        return ResponseEntity.ok(contactoService.obtenerContacto());
    }

}
