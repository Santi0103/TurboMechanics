package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.ChatbotRequestDTO;
import com.proyecto.TurboMechanics.dto.ChatbotResponseDTO;
import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody ChatbotRequestDTO request, HttpServletRequest httpRequest) {
        try {
            Long rolId = (Long) httpRequest.getAttribute("rolId");
            ChatbotResponseDTO response = chatbotService.chat(request, rolId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new MessageResponseDTO("Error inesperado en el chatbot."));
        }
    }
}