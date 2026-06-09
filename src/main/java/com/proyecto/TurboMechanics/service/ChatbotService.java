package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.ChatbotRequestDTO;
import com.proyecto.TurboMechanics.dto.ChatbotResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private final RestTemplate restTemplate;

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

        // Gemini recibe el system prompt como primer turno del modelo
        Map<String, Object> systemPart = Map.of("text", systemPrompt);
        Map<String, Object> systemContent = Map.of(
            "role", "model",
            "parts", List.of(systemPart)
        );

        Map<String, Object> userPart = Map.of("text", userMessage);
        Map<String, Object> userContent = Map.of(
            "role", "user",
            "parts", List.of(userPart)
        );

        Map<String, Object> body = Map.of(
            "contents", List.of(systemContent, userContent)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GEMINI_URL + geminiApiKey,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) responseBody.get("candidates");

            @SuppressWarnings("unchecked")
            Map<String, Object> content =
                (Map<String, Object>) candidates.get(0).get("content");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts =
                (List<Map<String, Object>>) content.get("parts");

            String reply = (String) parts.get(0).get("text");

            return new ChatbotResponseDTO(
                reply != null ? reply : "Sin respuesta.",
                resolveRoleName(rolId)
            );

        } catch (Exception e) {
            e.printStackTrace();
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