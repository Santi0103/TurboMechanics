package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.config.WhatsAppConfig;
import com.proyecto.TurboMechanics.dto.WhatsAppIncomingDTO;
import com.proyecto.TurboMechanics.dto.WhatsAppSessionResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {

    private final RestTemplate   restTemplate;

    private final WhatsAppConfig config;

    /**
     * construye los header http del microservicio
     * @return retorna el token y el contentType
     */
    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("x-service-token", config.getServiceToken());
        return h;
    }

    /**
     * Construye la URL completa
     * @param path ruta del endpoint
     * @return retorna la url completa
     */
    private String url(String path) {
        return config.getServiceUrl() + "/api" + path;
    }


    /**
     * inicia o restaura una sesion de whatsapp
     * @param sessionId id de la session de whatsapp
     * @return retorna la sessiom iniciada o restaurada
     */
    @PostMapping("/sessions/{sessionId}/start")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO })
    public ResponseEntity<WhatsAppSessionResponseDTO> start(@PathVariable String sessionId) {
        try {
            ResponseEntity<WhatsAppSessionResponseDTO> res = restTemplate.exchange(
                url("/sessions/" + sessionId + "/start"),
                HttpMethod.POST,
                new HttpEntity<>(headers()),
                WhatsAppSessionResponseDTO.class
            );
            return ResponseEntity.ok(res.getBody());
        } catch (Exception e) {
            log.error("Error iniciando sesión WA: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * obtiene el codigo qr
     * @param sessionId id de la session
     * @return retorna el qr
     */
    @GetMapping("/sessions/{sessionId}/qr")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO })
    public ResponseEntity<WhatsAppSessionResponseDTO> qr(@PathVariable String sessionId) {
        try {
            ResponseEntity<WhatsAppSessionResponseDTO> res = restTemplate.exchange(
                url("/sessions/" + sessionId + "/qr"),
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                WhatsAppSessionResponseDTO.class
            );
            return ResponseEntity.ok(res.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * consulta del estado de una sesion en whatsapp
     * @param sessionId id de la session
     * @return retorna el estado actual
     */
    @GetMapping("/sessions/{sessionId}/status")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO })
    public ResponseEntity<Map> status(@PathVariable String sessionId) {
        try {
            ResponseEntity<Map> res = restTemplate.exchange(
                url("/sessions/" + sessionId + "/status"),
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                Map.class
            );
            return ResponseEntity.ok(res.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * cierre o elimina la sesion de whatsapp
     * @param sessionId id de la sesion de whatsapp
     * @return retorna el cierre
     */
    @DeleteMapping("/sessions/{sessionId}")
    @RequiresRole({ RolEnum.ADMIN })
    public ResponseEntity<Void> logout(@PathVariable String sessionId) {
        restTemplate.exchange(
            url("/sessions/" + sessionId),
            HttpMethod.DELETE,
            new HttpEntity<>(headers()),
            Void.class
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * Recibe los mensajes entrantes de whatsapp
     * @param dto dto para los mensajes entrantes
     * @return retorna los mensajes entrantes
     */
    @PostMapping("/incoming")
    public ResponseEntity<Void> incoming(@RequestBody WhatsAppIncomingDTO dto) {
        log.info("[WA-ENTRANTE] Sesión: {} | De: {} | Msg: {}",
                 dto.getSessionId(), dto.getFrom(), dto.getBody());
        return ResponseEntity.ok().build();
    }
}