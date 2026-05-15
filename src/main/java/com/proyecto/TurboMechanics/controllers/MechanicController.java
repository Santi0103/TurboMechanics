package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.*;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.MechanicService;
import com.proyecto.TurboMechanics.enums.AbsenceType;
import com.proyecto.TurboMechanics.enums.LaborStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.TurboMechanics.dto.MechanicAbsenceResponseDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * Cambia el estado laboral de un mecánico (ACTIVO, INACTIVO, SUSPENDIDO).
     * @param document número de documento del mecánico
     * @param request nuevo estado laboral
     * @param httpRequest request HTTP para extraer el usuario del JWT
     * @return 200 OK con los datos del mecánico actualizado, o 400 si no se pudo cambiar el estado
     */
    @RequiresRole({RolEnum.ADMIN})
    @PatchMapping("/{document}/estado")
    public ResponseEntity<?> changeLaborStatus(
            @PathVariable Long document,
            @Valid @RequestBody LaborStatusRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            MechanicResponseDTO response = mechanicService.changeLaborStatus(
                    document, request.getLaborStatus(), extractUsername(httpRequest));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * obtiene el historial de órdenes de trabajo realizadas por un mecánico, con filtros opcionales por estado y rango de fechas.
     * @param document número de documento del mecánico
     * @param estado filtro opcional por estado de la orden (RECIBIDO, EN_PROCESO, FINALIZADO, CANCELADO)
     * @param desde filtro opcional por fecha de creación desde (formato ISO 8601: yyyy-MM-ddTHH:mm:ss)
     * @param hasta filtro opcional por fecha de creación hasta (formato ISO 8601: yyyy-MM-ddTHH:mm:ss)
     * @return 200 OK con la lista de órdenes de trabajo, o 404 si el mecánico no existe
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping("/{document}/historial")
    public ResponseEntity<?> getWorkHistory(
            @PathVariable Long document,
            @RequestParam(required = false) WorkOrder.StateOrder estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        try {
            List<WorkOrderResponseDTO> history =
                    mechanicService.getMechanicWorkHistory(document, estado, desde, hasta);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }
 
    /**
     * asigna una orden de trabajo a un mecánico
     * @param orderId id de la orden a asignar
     * @param request documento del mecánico al que se asignará la orden
     * @param httpRequest request HTTP para extraer el usuario del JWT
     * @return 200 OK con los datos de la orden actualizada, o 400 si no se pudo asignar
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @PostMapping("/ordenes/{orderId}/asignar")
    public ResponseEntity<?> assignOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody AssignOrderRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            WorkOrderResponseDTO response = mechanicService.assignOrderToMechanic(
                    orderId, request.getMechanicDocument(), extractUsername(httpRequest));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }
 
    /**
     * reasigna una orden de trabajo a otro mecánico, validando que el nuevo mecánico esté ACTIVO y tenga capacidad disponible.
     * @param orderId id de la orden a reasignar
     * @param request documento del nuevo mecánico al que se reasignará la orden
     * @param httpRequest request HTTP para extraer el usuario del JWT
     * @return 200 OK con los datos de la orden actualizada, o 400 si no se pudo reasignar (por ejemplo, si el nuevo mecánico
     * no existe, no está ACTIVO, no tiene capacidad disponible, o si la orden no se puede reasignar por su estado)
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @PutMapping("/ordenes/{orderId}/reasignar")
    public ResponseEntity<?> reassignOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody AssignOrderRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            WorkOrderResponseDTO response = mechanicService.reassignOrderToMechanic(
                    orderId, request.getMechanicDocument(), extractUsername(httpRequest));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }
 
    /**
     * Registra una ausencia para un mecánico, indicando el tipo (VACACIONES, INCAPACIDAD, PERMISO), las fechas de inicio y fin, y el motivo.
     * @param document número de documento del mecánico
     * @param request datos de la ausencia a registrar
     * @param httpRequest request HTTP para extraer el usuario del JWT
     * @return 201 Created con los datos de la ausencia registrada, o 400 si no se pudo registrar (por ejemplo, si el mecánico no existe o si hay solapamientos con otras ausencias)
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @PostMapping("/{document}/ausencias")
    public ResponseEntity<?> registerAbsence(
            @PathVariable Long document,
            @Valid @RequestBody MechanicAbsenceRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            MechanicAbsenceResponseDTO response = mechanicService.registerAbsence(
                    document, request, extractUsername(httpRequest));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }
 
    /**
     * obtiene el historial de ausencias de un mecánico, con filtros opcionales por tipo de ausencia y rango de fechas.
     * @param document número de documento del mecánico
     * @param tipo filtro opcional por tipo de ausencia (VACACIONES, INCAPACIDAD, PERMISO)
     * @param desde filtro opcional por fecha de inicio desde (formato ISO 8601: yyyy-MM-ddTHH:mm:ss)
     * @param hasta filtro opcional por fecha de fin hasta (formato ISO 8601: yyyy-MM-ddTHH:mm:ss)
     * @return 200 OK con la lista de ausencias, o 404 si el mecánico no existe
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping("/{document}/ausencias")
    public ResponseEntity<?> getAbsenceHistory(
            @PathVariable Long document,
            @RequestParam(required = false) AbsenceType tipo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        try {
            List<MechanicAbsenceResponseDTO> history =
                    mechanicService.getAbsenceHistory(document, tipo, desde, hasta);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }
 
    /** Extrae el username inyectado por el filtro JWT. */
    private String extractUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username != null ? username.toString() : "sistema";
    }
}