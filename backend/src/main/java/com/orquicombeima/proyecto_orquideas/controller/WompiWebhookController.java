package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.service.WompiWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WompiWebhookController {

    private final WompiWebhookService wompiWebhookService;

    @PostMapping("/wompi")
    public ResponseEntity<Void> recibirEvento(@RequestBody Map<String, Object> payload) {
        wompiWebhookService.procesarEvento(payload);
        return ResponseEntity.ok().build();
    }
}
