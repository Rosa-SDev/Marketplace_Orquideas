package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.model.Maceta;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.repository.MacetaRepository;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final OrquideaRepository orquideaRepository;
    private final MacetaRepository macetaRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Función para procesar el mesaje que recibira Gemini
    public String procesarMensaje (String mensajeUsuario) {
        String contexto = construirContexto();
        String prompt = construirPrompt(contexto, mensajeUsuario);
        return llamarGemini(prompt);
    }


    // Función para darle el contexto a Gemini
    private String construirContexto() {
        StringBuilder sb = new StringBuilder();

        // Orquideas disponibles
        sb.append("CATALOGO DE ORQUIDEAS DISPONIBLES: \n");
        List<Orquidea> orquideas = orquideaRepository.findByActivoTrue();

        for (Orquidea orquidea : orquideas) {
            sb.append(String.format("- %s | Variedad: %s | Color: %s | Precio: $%.0f | Stock: %d unidades\n", orquidea.getNombre(),orquidea.getVariedad(), orquidea.getColorFlor(), orquidea.getPrecio(), orquidea.getStock()));
        }

        // Macetas disponibles
        sb.append("\nCATALOGO DE MACETAS DISPONIBLES: \n");
        List<Maceta> macetas = macetaRepository.findByActivoTrue();

        for (Maceta maceta : macetas) {
            sb.append(String.format("- %s | Material: %s | Color: %s | Precio: $%.0f | Stock: %d unidades\n", maceta.getNombre(), maceta.getMaterial(), maceta.getColor(), maceta.getPrecio(), maceta.getStock()));
        }

        return sb.toString();
    }

    // Función para construir el prompt con la estructura necesaria que sera enviada a Gemini
    private String construirPrompt (String contexto, String mensajeUsuario) {
        return """
                Eres el asistente virtual de Orquídeas del Combeima, un emprendimiento colombiano especializado en la venta de orquídeas y macetas ubicado en Ibagué, Tolima.

                Tu función es ÚNICAMENTE responder preguntas relacionadas con:
                - El catálogo de productos del negocio
                - Cuidados y guías de orquídeas
                - Precios y disponibilidad
                - Proceso de compra y pedidos
                - Información del negocio

                Si el usuario pregunta algo que NO está relacionado con el negocio, responde amablemente:
                "Solo puedo ayudarte con información sobre Orquídeas del Combeima. ¿Tienes alguna pregunta sobre nuestros productos o servicios?"

                Responde siempre en español, de forma amable y profesional.
                Usa el catálogo actualizado para dar información precisa sobre precios y disponibilidad.

                """ + contexto + """

                Pregunta del cliente: """ + mensajeUsuario;
    }

    // Función para hacer la llamada a Gemini con el prompt construido y procesar la respuesta para devolver solo el texto de la respuesta de Gemini
    private String llamarGemini (String prompt) {
        try {
            String urlConKey = apiUrl + "?key=" + apiKey;

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> body = Map.of("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(urlConKey, request, Map.class);

            // Extraer la respuesta de Gemini
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> contentResponse = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResponse.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            e.printStackTrace();
            return "Lo siento, en este momento no puedo procesar tu pregunta. Por favor intenta más tarde";
        }
    }
}
