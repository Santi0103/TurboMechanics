package com.proyecto.TurboMechanics.service;


import com.proyecto.TurboMechanics.dto.ChatbotRequestDTO;
import com.proyecto.TurboMechanics.dto.ChatbotResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> BLOCKED_WORDS = Set.of(
        "puta", "puto", "mierda", "coño", "culo", "joder", "hostia", "verga",
        "hijueputa", "gonorrea", "malparido", "hp", "idiota", "imbecil",
        "estupido", "pendejo", "marica", "maricon", "hdp", "culero",
        "pinche", "cabrón", "cabron", "chingada", "chingar"
    );

    public ChatbotResponseDTO chat(ChatbotRequestDTO request, Long rolId) {

        String userMessage = request.getMessage().trim();

        if (containsBlockedWord(userMessage)) {
            return new ChatbotResponseDTO(
                "Por favor usa un lenguaje respetuoso. Estoy aquí para ayudarte con consultas relacionadas al taller.",
                resolveRoleName(rolId)
            );
        }

        String systemPrompt = buildSystemPrompt(rolId);
        Map<String, Object> body = buildGeminiRequest(systemPrompt, userMessage);

        String apiUrl = GEMINI_URL + "?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            String reply = extractTextFromResponse(response.getBody());
            return new ChatbotResponseDTO(reply, resolveRoleName(rolId));
        } catch (Exception e) {
            return new ChatbotResponseDTO(
                "Lo siento, hubo un problema al procesar tu consulta. Por favor intenta de nuevo.",
                resolveRoleName(rolId)
            );
        }
    }

    private String buildSystemPrompt(Long rolId) {

        String base = """
            Eres Turbo Bot, el asistente virtual del taller automotriz TurboMechanics.
            Responde siempre en español, de forma clara y concisa.
            No tienes acceso a la base de datos ni a información en tiempo real del sistema.
            No inventes datos de clientes, vehículos, órdenes ni precios específicos.
            Si te preguntan algo que requiere datos del sistema, indica amablemente que deben
            consultar directamente en la sección correspondiente de la plataforma.
            Nunca uses lenguaje ofensivo ni inapropiado.
            """;

        return switch (getRolEnum(rolId)) {
            case ADMIN -> base + """
                El usuario es un ADMINISTRADOR del taller.
                Puedes ayudarle con:
                - Orientación sobre gestión de talleres, inventario, mecánicos y clientes
                - Buenas prácticas de administración de talleres automotrices
                - Interpretación de métricas y reportes generales
                - Resolución de dudas sobre módulos del sistema (citas, órdenes, facturación, repuestos)
                - Gestión de garantías y control de calidad
                Usa un tono profesional y directo.
                """;
            case MECANICO -> base + """
                El usuario es un MECÁNICO del taller.
                Puedes ayudarle con:
                - Consultas técnicas sobre reparación y mantenimiento de vehículos
                - Orientación sobre diagnóstico de fallas comunes
                - Buenas prácticas mecánicas y de seguridad en el taller
                - Dudas sobre el uso del módulo de órdenes de trabajo y evidencias
                - Información general sobre repuestos y su uso
                No proporciones datos de clientes ni información administrativa.
                Usa un tono técnico y amigable.
                """;
            case CLIENTE -> base + """
                El usuario es un CLIENTE del taller.
                Puedes ayudarle con:
                - Información general sobre servicios que ofrece un taller automotriz
                - Consejos de mantenimiento preventivo para vehículos
                - Orientación sobre cómo agendar citas o hacer seguimiento de su vehículo en la plataforma
                - Preguntas frecuentes sobre garantías y procesos del taller
                - Dudas sobre cómo funciona el sistema de reseñas
                No proporciones información de otros clientes ni datos internos del taller.
                Usa un tono amable, sencillo y cercano.
                """;
        };
    }

    private Map<String, Object> buildGeminiRequest(String systemPrompt, String userMessage) {
        Map<String, Object> body = new HashMap<>();

        Map<String, Object> systemInstruction = new HashMap<>();
        Map<String, Object> systemPart = new HashMap<>();
        systemPart.put("text", systemPrompt);
        systemInstruction.put("parts", List.of(systemPart));
        body.put("systemInstruction", systemInstruction);

        Map<String, Object> userPart = new HashMap<>();
        userPart.put("text", userMessage);
        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        userContent.put("parts", List.of(userPart));
        body.put("contents", List.of(userContent));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 512);
        body.put("generationConfig", generationConfig);

        return body;
    }

    private String extractTextFromResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        return root
            .path("candidates").get(0)
            .path("content")
            .path("parts").get(0)
            .path("text")
            .asText("Lo siento, no pude generar una respuesta en este momento.");
    }

    private boolean containsBlockedWord(String text) {
        String normalized = text.toLowerCase()
            .replaceAll("[áàä]", "a")
            .replaceAll("[éèë]", "e")
            .replaceAll("[íìï]", "i")
            .replaceAll("[óòö]", "o")
            .replaceAll("[úùü]", "u")
            .replaceAll("[ñ]", "n");

        String[] words = normalized.split("[\\s.,!?;:\"'()]+");
        for (String word : words) {
            if (BLOCKED_WORDS.contains(word)) return true;
        }
        return false;
    }

    private RolEnum getRolEnum(Long rolId) {
        if (rolId == null) return RolEnum.CLIENTE;
        for (RolEnum rol : RolEnum.values()) {
            if (rol.getId().equals(rolId)) return rol;
        }
        return RolEnum.CLIENTE;
    }

    private String resolveRoleName(Long rolId) {
        return getRolEnum(rolId).name();
    }
}