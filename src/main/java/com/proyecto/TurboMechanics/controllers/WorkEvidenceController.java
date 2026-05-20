package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.WorkEvidenceResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.WorkEvidenceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.proyecto.TurboMechanics.enums.EvidenceType;
import java.util.List;

/**
 * Controlador para evidencias de trabajo (HU 8.5).
 * Endpoint base: /evidencias
 */
@RestController
@RequestMapping("/evidencias")
@RequiredArgsConstructor
public class WorkEvidenceController {

    private final WorkEvidenceService workEvidenceService;

    /**
     * Adjunta una evidencia (imagen, video o documento) a una orden de trabajo.
     * @param workOrderId id de la orden de trabajo
     * @param file archivo adjunto
     * @param description descripción opcional de la evidencia
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que adjunta la evidencia
     * @return datos de la evidencia guardada
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> uploadEvidence(
            @RequestParam("ordenId") Long workOrderId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "descripcion", required = false) String description,
            HttpServletRequest httpRequest) {
        try {
            String uploadedBy = extractUsername(httpRequest);
            WorkEvidenceResponseDTO response =
                    workEvidenceService.uploadEvidence(workOrderId, file, description, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Obtiene las evidencias asociadas a una orden de trabajo, opcionalmente filtradas por tipo (IMAGEN, VIDEO, DOCUMENTO).
     * @param workOrderId id de la orden de trabajo
     * @param tipo filtro opcional por tipo de evidencia (IMAGEN, VIDEO, DOCUMENTO)
     * @return lista de evidencias que coinciden con el filtro, ordenadas por fecha de carga (más recientes primero)
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping
    public ResponseEntity<?> getEvidences(
            @RequestParam("ordenId") Long workOrderId,
            @RequestParam(value = "tipo", required = false) EvidenceType tipo) {
        try {
            List<WorkEvidenceResponseDTO> evidences =
                    workEvidenceService.getEvidences(workOrderId, tipo);
            return ResponseEntity.ok(evidences);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error al consultar evidencias"));
        }
    }

    /**
     * elimina una evidencia por su id. Solo ADMIN y MECANICO pueden eliminar evidencias.
     * @param id id de la evidencia a eliminar
     * @return mensaje de éxito o error
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteEvidence(@PathVariable Long id) {
        try {
            workEvidenceService.deleteEvidence(id);
            return ResponseEntity.ok(new MessageResponseDTO("Evidencia eliminada correctamente"));
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