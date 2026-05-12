package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.MechanicRequestDTO;
import com.proyecto.TurboMechanics.dto.MechanicResponseDTO;
import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.MechanicService;
import com.proyecto.TurboMechanics.enums.LaborStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/mecanicos")
@RequiredArgsConstructor
public class MechanicController {

    private final MechanicService mechanicService;

    /**
     * Registra un nuevo mecánico en el sistema.
     * @param request datos del mecánico
     * @param httpRequest request HTTP para extraer el usuario del JWT
     * @return 201 Created con los datos del mecánico registrado
     */
    @RequiresRole({RolEnum.ADMIN})
    @PostMapping
    public ResponseEntity<?> registerMechanic(
            @Valid @RequestBody MechanicRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            String createdBy = extractUsername(httpRequest);
            MechanicResponseDTO response = mechanicService.registerMechanic(request, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Retorna el listado de mecánicos con filtros opcionales.
     * @param position     filtrar por cargo (opcional)
     * @param laborStatus  filtrar por estado laboral (opcional)
     * @param fromHireDate filtrar desde una fecha de ingreso (opcional, formato yyyy-MM-dd)
     * @return 200 OK con la lista de mecánicos
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping
    public ResponseEntity<?> getMechanics(
            @RequestParam(required = false) String cargo,
            @RequestParam(required = false) LaborStatus estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIngreso) {
        try {
            List<MechanicResponseDTO> mechanics = mechanicService.getMechanics(cargo, estado, fechaIngreso);
            return ResponseEntity.ok(mechanics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error al consultar mecánicos"));
        }
    }

    /**
     * Busca un mecánico por su número de documento.
     * @param document número de documento del mecánico
     * @return 200 OK con los datos del mecánico, o 404 si no existe
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping("/{document}")
    public ResponseEntity<?> getMechanicByDocument(@PathVariable Long document) {
        try {
            MechanicResponseDTO response = mechanicService.getMechanicByDocument(document);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Actualiza los datos de un mecánico existente.
     * @param document    número de documento del mecánico a actualizar
     * @param request     nuevos datos del mecánico
     * @param httpRequest request HTTP para extraer el usuario del JWT
     * @return 200 OK con los datos actualizados
     */
    @RequiresRole({RolEnum.ADMIN})
    @PutMapping("/{document}")
    public ResponseEntity<?> updateMechanic(
            @PathVariable Long document,
            @Valid @RequestBody MechanicRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            String updatedBy = extractUsername(httpRequest);
            MechanicResponseDTO response = mechanicService.updateMechanic(document, request, updatedBy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Elimina un mecánico del sistema.
     * Valida que no tenga órdenes de trabajo activas ni historiales pendientes.
     * DELETE /mecanicos/{documento}
     *
     * @param document número de documento del mecánico a eliminar
     * @return 200 OK con mensaje de confirmación, o 400 si no se puede eliminar
     */
    @RequiresRole({RolEnum.ADMIN})
    @DeleteMapping("/{document}")
    public ResponseEntity<MessageResponseDTO> deleteMechanic(@PathVariable Long document) {
        try {
            mechanicService.deleteMechanic(document);
            return ResponseEntity.ok(new MessageResponseDTO("Mecánico eliminado correctamente"));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Extrae el nombre de usuario del atributo inyectado por el filtro JWT.
     */
    private String extractUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username != null ? username.toString() : "sistema";
    }
}