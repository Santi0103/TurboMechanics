package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.QualityCheckRequestDTO;
import com.proyecto.TurboMechanics.dto.QualityCheckResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.QualityCheckService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/control-calidad")
@RequiredArgsConstructor
public class QualityCheckController {

    private final QualityCheckService qualityCheckService;

    /**
     * iniciar un control de calidad para una orden de trabajo. Crea un nuevo control de calidad con estado PENDIENTE
     * @param ordenId id de la orden de trabajo para la cual se inicia el control de calidad
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que inicia el control de calidad
     * @return datos del control de calidad creado o error si ya existe un control de calidad para esa orden 
     * o si la orden no existe
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @PostMapping
    public ResponseEntity<?> startQualityCheck(
            @RequestParam Long ordenId,
            HttpServletRequest httpRequest) {
        try {
            QualityCheckResponseDTO response =
                    qualityCheckService.startQualityCheck(ordenId, extractUsername(httpRequest));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * obtiene los datos de un control de calidad por el id de la orden de trabajo asociada.
     * @param ordenId id de la orden de trabajo asociada al control de calidad
     * @return datos del control de calidad o error si no se encuentra un control de calidad para esa orden
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping
    public ResponseEntity<?> getByWorkOrder(@RequestParam Long ordenId) {
        try {
            return ResponseEntity.ok(qualityCheckService.getByWorkOrder(ordenId));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * actualiza los ítems verificados de un control de calidad.
     * @param id id del control de calidad a actualizar
     * @param request nuevos datos del control de calidad (ítems verificados y observaciones)
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que realiza la actualización
     * @return datos del control de calidad actualizado
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItems(
            @PathVariable Long id,
            @Valid @RequestBody QualityCheckRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            QualityCheckResponseDTO response =
                    qualityCheckService.updateItems(id, request, extractUsername(httpRequest));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * aprueba un control de calidad, lo que implica que el vehículo cumple con los estándares de 
     * calidad y puede ser entregado al cliente.
     * @param id id del control de calidad a aprobar
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que realiza la aprobación
     * @return datos del control de calidad aprobado
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<?> approveQualityCheck(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            QualityCheckResponseDTO response =
                    qualityCheckService.approveQualityCheck(id, extractUsername(httpRequest));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * rechaza un control de calidad, lo que implica que el vehículo no cumple con los estándares 
     * de calidad y no puede ser entregado al cliente.
     * @param id id del control de calidad a rechazar
     * @param request datos de la solicitud (observaciones del rechazo)
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que realiza el rechazo
     * @return datos del control de calidad rechazado
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<?> rejectQualityCheck(
            @PathVariable Long id,
            @RequestBody(required = false) QualityCheckRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            String observations = request != null ? request.getObservations() : null;
            QualityCheckResponseDTO response =
                    qualityCheckService.rejectQualityCheck(
                            id, observations, extractUsername(httpRequest));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    private String extractUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username != null ? username.toString() : "sistema";
    }
}