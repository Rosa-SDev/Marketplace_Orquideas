package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.ChatbotRequestDTO;
import com.orquicombeima.proyecto_orquideas.dto.ChatbotResponseDTO;
import com.orquicombeima.proyecto_orquideas.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<ChatbotResponseDTO> chat(@RequestBody ChatbotRequestDTO peticion) {
        String respuesta = chatbotService.procesarMensaje(peticion.getMensaje());
        return  ResponseEntity.ok(new ChatbotResponseDTO(respuesta));
    }
}
