package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.TurboMechanics.dto.MaintenanceStatusResponseDTO;
import com.proyecto.TurboMechanics.dto.NotificationConsentRequestDTO;
import com.proyecto.TurboMechanics.dto.RegisterProgressRequestDTO;
import com.proyecto.TurboMechanics.dto.ReportIssueRequestDTO;
import com.proyecto.TurboMechanics.dto.UpdateMaintenanceTimeRequestDTO;
import com.proyecto.TurboMechanics.entity.Issue;
import com.proyecto.TurboMechanics.entity.MaintenanceProgress;
import com.proyecto.TurboMechanics.entity.NotificationConsent;
import com.proyecto.TurboMechanics.entity.NotificationLog;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.MaintenanceTrackingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceTrackingController {

    private final MaintenanceTrackingService maintenanceTrackingService;

    /**
     * Estado de mantenimiento por placa
     * @param plate placa del vehiculo
     * @return retorna el estado de mantenimiento
     */
    @GetMapping("/status")
    @RequiresRole({RolEnum.CLIENTE})
    public ResponseEntity<MaintenanceStatusResponseDTO> statusByPlate(@RequestParam String plate) {
        try {
            MaintenanceStatusResponseDTO response = maintenanceTrackingService.getStatusByPlate(plate);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Estado por id de la orden
     * @param workOrderId id de la orden de trabajo
     * @return retorna el estado
     */
    @GetMapping("/status/{workOrderId}")
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<MaintenanceStatusResponseDTO> statusById(@PathVariable Long workOrderId) {
        try {
            MaintenanceStatusResponseDTO response = maintenanceTrackingService.getStatusByWorkOrderId(workOrderId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Historial de todas las ordenes de un vehiculo por placa
     * @param plate placa del vehiculo
     * @return retorna todas las ordenes incluyendo entregadas y canceladas
     */
    @GetMapping("/history")
    @RequiresRole({RolEnum.CLIENTE})
    public ResponseEntity<List<MaintenanceStatusResponseDTO>> history(@RequestParam String plate) {
        try {
            List<MaintenanceStatusResponseDTO> response = maintenanceTrackingService.getHistoryByPlate(plate);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Actualizar tiempo estimado de entrega
     * @param request dto para actualizar la fecha estimada
     * @return retorna la nueva fecha
     */
    @PutMapping("/time")
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<WorkOrder> updateTime(@Valid @RequestBody UpdateMaintenanceTimeRequestDTO request) {
        try {
            WorkOrder workOrder = maintenanceTrackingService.updateEstimatedDelivery(request);
            return ResponseEntity.status(HttpStatus.OK).body(workOrder);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Notificar el cambio del estado al cliente
     * @param workOrderId id de la orden de trabajo
     * @return retorna la notificacion del cambio de estado
     */
    @PostMapping("/{workOrderId}/notify")
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<Void> notifyStateChange(@PathVariable Long workOrderId) {
        try {
            maintenanceTrackingService.notifyStateChange(workOrderId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Registrar inconveniente
     * @param request dto para el registro del inconveniente
     * @return retorna el inconveniente registrado
     */
    @PostMapping("/issues")
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<Issue> reportIssue(@Valid @RequestBody ReportIssueRequestDTO request) {
        try {
            Issue issue = maintenanceTrackingService.reportIssue(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(issue);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Cerrar inconveniente
     * @param issueId id del asunto
     * @return retorna el cierre del asunto
     */
    @PatchMapping("/issues/{issueId}/close")
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<Issue> closeIssue(@PathVariable Long issueId) {
        try {
            Issue issue = maintenanceTrackingService.closeIssue(issueId);
            return ResponseEntity.status(HttpStatus.OK).body(issue);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Consultar inconvenientes de una orden
     * @param workOrderId id de la orden de trabajo
     * @return retorna la consulta de los inconvenientes en esa orden de trabajo
     */
    @GetMapping("/{workOrderId}/issues")
    public ResponseEntity<List<Issue>> getIssues(@PathVariable Long workOrderId) {
        try {
            List<Issue> issues = maintenanceTrackingService.getIssuesByWorkOrder(workOrderId);
            return ResponseEntity.status(HttpStatus.OK).body(issues);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Registrar avances
     * @param request dto para registrar los avances
     * @return retorna el registro creado
     */
    @PostMapping("/progress")
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<MaintenanceProgress> registerProgress(@Valid @RequestBody RegisterProgressRequestDTO request) {
        try {
            MaintenanceProgress maintenanceProgress = maintenanceTrackingService.registerProgress(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceProgress);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Consultar avances de una orden
     * @param workOrderId id de la orden de trabajo
     * @return retorna los avances
     */
    @GetMapping("/{workOrderId}/progress")
    public ResponseEntity<List<MaintenanceProgress>> getProgress(@PathVariable Long workOrderId) {
        try {
            List<MaintenanceProgress> maintenanceProgresses = maintenanceTrackingService.getProgressByWorkOrder(workOrderId);
            return ResponseEntity.status(HttpStatus.OK).body(maintenanceProgresses);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Historial de notificaciones de una orden
     * @param workOrderId id de la orden de trabajo
     * @return retorna el historial de notificaciones enviadas al cliente
     */
    @GetMapping("/{workOrderId}/notifications")
    @RequiresRole({RolEnum.CLIENTE})
    public ResponseEntity<List<NotificationLog>> getNotifications(@PathVariable Long workOrderId) {
        try {
            List<NotificationLog> logs = maintenanceTrackingService.getNotificationsByWorkOrder(workOrderId);
            return ResponseEntity.status(HttpStatus.OK).body(logs);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Guardar o actualizar el consentimiento de la notificacion
     * @param request dto para el consentimiento de la notificacion
     * @return retorna el consentimiento guardado o actualizado
     */
    @PostMapping("/consent")
    @RequiresRole({RolEnum.CLIENTE})
    public ResponseEntity<NotificationConsent> saveConsent(@Valid @RequestBody NotificationConsentRequestDTO request) {
        try {
            NotificationConsent notificationConsent = maintenanceTrackingService.saveConsent(request);
            return ResponseEntity.status(HttpStatus.OK).body(notificationConsent);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Consultar consentimiento de un cliente
     * @param identification identificacion del cliente
     * @return retorna la consulta del consentimiento
     */
    @GetMapping("/consent")
    @RequiresRole({RolEnum.ADMIN, RolEnum.CLIENTE})
    public ResponseEntity<NotificationConsent> getConsent(@RequestParam Integer identification) {
        try {
            NotificationConsent notificationConsent = maintenanceTrackingService.getConsent(identification);
            return ResponseEntity.status(HttpStatus.OK).body(notificationConsent);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}