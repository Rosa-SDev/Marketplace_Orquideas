package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.MensajeHistorialDTO;
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
    public String procesarMensaje (String mensajeUsuario, List<MensajeHistorialDTO> historial) {
        String contexto = construirContexto();
        String prompt = construirPrompt(contexto, mensajeUsuario, historial);
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
    private String construirPrompt (String contexto, String mensajeUsuario, List<MensajeHistorialDTO> historial) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                    Eres el asistente virtual de Orquídeas del Combeima, un emprendimiento colombiano especializado en la venta de orquídeas y macetas ubicado en Ibagué, Tolima.
    
                    Tu función es ÚNICAMENTE responder preguntas relacionadas con:
                    - El catálogo de productos del negocio
                    - Cuidados y guías de orquídeas
                    - Precios y disponibilidad
                    - Proceso de compra y pedidos
                    - Información del negocio
                    
                    RESTRICCIONES IMPORTANTES:
                    - NUNCA registres, confirmes ni proceses pedidos. Los pedidos SOLO se realizan a través de la página web.
                    - NUNCA solicites datos personales como nombre, dirección, teléfono o información de pago.
                    - Si el usuario quiere hacer un pedido, indícale que debe hacerlo desde el catálogo de la página web.
                    - NUNCA inventes información que no esté en el catálogo.
    
                    Si el usuario pregunta algo que NO está relacionado con el negocio, responde amablemente:
                    "Solo puedo ayudarte con información sobre Orquídeas del Combeima. ¿Tienes alguna pregunta sobre nuestros productos o servicios?"
    
                    Responde siempre en español, de forma amable y profesional.
                    Usa el catálogo actualizado para dar información precisa sobre precios y disponibilidad.
    
                    """);

        sb.append(contexto).append("\n");

        // Condicional que verifica si el usuario ya tiene historial o si este se encuentra vacio para asi mismo incluir el historial en el prompt
        if (historial != null && !historial.isEmpty()) {
            sb.append("HISTORIAL DE CONVERSACIÓN:\n");

            for (MensajeHistorialDTO mensajeHistorial : historial) {
                String rol = mensajeHistorial.getRol().equals("bot") ? "Asistente" : "Cliente";
                sb.append(rol).append(": ").append(mensajeHistorial.getTexto()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Pregunta actual del cliente: ").append(mensajeUsuario);
        return sb.toString();
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
            return "Lo siento, en este momento no puedo procesar tu pregunta. Por favor intenta más tarde";
        }
    }
}
