package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.DiagnosisRequestDTO;
import com.proyecto.TurboMechanics.dto.DiagnosisResponseDTO;
import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.DiagnosisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    /**
     * Registra un nuevo diagnóstico técnico asociado a una orden de trabajo. 
     * @param request DTO con los datos del diagnóstico, todos los campos obligatorios validados
     * @return 201 CREATED con el diagnóstico creado
     */
    @PostMapping
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<?> create(@Valid @RequestBody DiagnosisRequestDTO request) {
        try {
            DiagnosisResponseDTO response = diagnosisService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponseDTO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Actualiza un diagnóstico técnico existente antes de generar la orden de trabajo.
     * @param id      ID del diagnóstico a actualizar
     * @param request DTO con los nuevos datos del diagnóstico
     * @return 200 OK con el diagnóstico actualizado,
     *         409 CONFLICT si ya se generó una orden desde este diagnóstico,
     *         404 NOT FOUND si no existe el diagnóstico
     */
    @PutMapping("/{id}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody DiagnosisRequestDTO request) {
        try {
            DiagnosisResponseDTO response = diagnosisService.update(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponseDTO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Obtiene un diagnóstico por su ID.
     * @param id ID del diagnóstico
     * @return 200 OK con el diagnóstico, 404 NOT FOUND si no existe
     */
    @GetMapping("/{id}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            DiagnosisResponseDTO response = diagnosisService.getById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Lista todos los diagnósticos asociados a una orden de trabajo.
     * Devuelve la lista ordenada del más reciente al más antiguo.
     * @param workOrderId ID de la orden de trabajo
     * @return 200 OK con la lista de diagnósticos (puede estar vacía)
     */
    @GetMapping("/order/{workOrderId}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<DiagnosisResponseDTO>> listByWorkOrder(
            @PathVariable Long workOrderId) {
        try {
            List<DiagnosisResponseDTO> response = diagnosisService.listByWorkOrder(workOrderId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Genera una nueva orden de trabajo a partir de un diagnóstico registrado.
     * Marca el diagnóstico como utilizado para evitar duplicados.
     * @param id          ID del diagnóstico
     * @param body        Mapa opcional con "createdBy" para indicar quién genera la orden
     * @return 201 CREATED con la nueva orden generada.
     */
    @PostMapping("/{id}/generate-order")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<?> generateWorkOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String createdBy = (body != null) ? body.get("createdBy") : null;
            WorkOrderResponseDTO newOrder = diagnosisService.generateWorkOrderFromDiagnosis(id, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Orden de trabajo generada correctamente desde el diagnóstico.",
                            "order", newOrder
                    ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponseDTO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }
}
